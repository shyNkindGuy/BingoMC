package com.shyNkindguy.bingo.game;


import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;
import com.shyNkindguy.bingo.core.bus.CellCompleted;
import com.shyNkindguy.bingo.core.bus.DomainEvents;
import com.shyNkindguy.bingo.core.bus.GameEnded;
import com.shyNkindguy.bingo.core.event.EventContext;
import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.core.game.GameSession;
import com.shyNkindguy.bingo.core.game.GameSummary;
import com.shyNkindguy.bingo.core.game.GameSettings;
import com.shyNkindguy.bingo.core.game.Phase;
import com.shyNkindguy.bingo.core.progress.ProgressResult;
import com.shyNkindguy.bingo.core.progress.ProgressTracker;
import com.shyNkindguy.bingo.core.task.Task;
import com.shyNkindguy.bingo.core.team.Team;
import com.shyNkindguy.bingo.core.team.TeamRegistry;
import com.shyNkindguy.bingo.core.win.TimeLimitWinners;
import com.shyNkindguy.bingo.ui.GameBossBar;
import com.shyNkindguy.bingo.ui.TeamColors;
import com.shyNkindguy.bingo.ui.Texts;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.logging.Level;

// La partida en el servidor: una cartilla por equipo (quien no tiene equipo juega solo, en un equipo de 1)
// y un reloj de un segundo que hace avanzar la GameSession del núcleo (cuenta regresiva → en curso →
// tiempo agotado). Lo que pasa en la partida se publica en `events()` (casillas completadas, fin) para las
// estadísticas y lo que venga.
public class BingoGame {
    private static final long TICKS_PER_SECOND = 20;

    private final Plugin plugin;
    private final GameSession session = new GameSession();
    private final TeamRegistry<UUID> teams = new TeamRegistry<>();
    private final Map<String, BingoCard> cards = new HashMap<>(); // id del equipo → cartilla
    private final Map<UUID, String> participants = new LinkedHashMap<>(); // quién jugó → su nombre
    private final DomainEvents events;
    private List<Team> lastWinners = List.of();
    private Set<UUID> lastParticipants = Set.of();
    private final List<Runnable> startListeners = new ArrayList<>();
    private final List<Runnable> endListeners = new ArrayList<>();
    private final List<Runnable> tickListeners = new ArrayList<>();
    private final List<Runnable> dealListeners = new ArrayList<>();
    private List<Task> tasks = List.of();
    private GameSettings settings = GameSettings.DEFAULT;
    private BukkitTask clock;

    public BingoGame(Plugin plugin){
        this.plugin = plugin;
        this.events = new DomainEvents((event, e) -> plugin.getLogger().log(Level.SEVERE,
                "Falló algo que escucha " + event.getClass().getSimpleName(), e));
    }

    public DomainEvents events() {
        return events;
    }

    // Se ejecuta cuando termina la cuenta regresiva y la partida queda en curso.
    public void onStart(Runnable listener) {
        startListeners.add(listener);
    }

    // Se ejecuta cada vez que termina una partida (bingo, tiempo agotado o /bingo stop).
    public void onEnd(Runnable listener) {
        endListeners.add(listener);
    }

    // Se ejecuta al repartir las cartillas, antes de la cuenta regresiva (y antes de que se den los mapas).
    public void onDeal(Runnable listener) {
        dealListeners.add(listener);
    }

    // Se ejecuta cada segundo mientras hay partida (también en la cuenta regresiva y en pausa), y al empezar.
    public void onTick(Runnable listener) {
        tickListeners.add(listener);
    }

    // Reparte las cartillas y empieza la cuenta regresiva: las cartillas se ven desde ya, pero nada cuenta
    // hasta que arranca. `tasks` son las N² tareas de la partida (Catalog.generate), las mismas para todos;
    // cada cartilla las baraja en su propia copia.
    public boolean start(Collection<? extends Player> players, List<Task> tasks, GameSettings settings) {
        if (session.inGame() || players.isEmpty()) return false;

        this.tasks = List.copyOf(tasks);
        this.settings = settings;
        cards.clear();
        participants.clear();
        for (Player p : players) {
            participants.put(p.getUniqueId(), p.getName());
            Team team = teams.soloIfAlone(p.getUniqueId(), p.getName());
            cards.computeIfAbsent(team.id(), this::newCard);
        }

        GameSession.Signal first = session.begin(settings.countdownSeconds(), settings.durationSeconds());
        dealListeners.forEach(Runnable::run);
        clock = Bukkit.getScheduler().runTaskTimer(plugin, () -> handle(session.tick()),
                TICKS_PER_SECOND, TICKS_PER_SECOND);
        handle(first);
        return true;
    }

    // Quien entra con la partida empezada (o en la cuenta regresiva) juega con la cartilla de su equipo; si
    // no tiene, solo, con una cartilla de las mismas tareas. Si se desconectó y volvió, sigue con la suya.
    // null si no hay partida.
    public BingoCard join(Player player) {
        if (!session.inGame()) return null;
        participants.put(player.getUniqueId(), player.getName());
        Team team = teams.soloIfAlone(player.getUniqueId(), player.getName());
        return cards.computeIfAbsent(team.id(), this::newCard);
    }

    // Una cartilla nueva con las tareas de la partida; en lockout, con las que ya tienen dueño bloqueadas.
    private BingoCard newCard(String teamId) {
        BingoCard card = new BingoCard(tasks);
        if (settings.mode().lockout()) {
            for (Map.Entry<String, BingoCard> other : cards.entrySet()) {
                for (BingoCell[] row : other.getValue().getGrid()) {
                    for (BingoCell cell : row) {
                        if (cell.isCompleted()) lock(card, cell.getTask().id(), other.getKey());
                    }
                }
            }
        }
        return card;
    }

    private static void lock(BingoCard card, String taskId, String owner) {
        for (BingoCell[] row : card.getGrid()) {
            for (BingoCell cell : row) {
                if (cell.getTask().id().equals(taskId)) cell.lock(owner);
            }
        }
    }

    // Lockout: ya no queda ninguna tarea sin dueño (todas las cartillas tienen las mismas tareas).
    public boolean allClaimed() {
        if (!settings.mode().lockout() || cards.isEmpty()) return false;
        for (BingoCell[] row : cards.values().iterator().next().getGrid()) {
            for (BingoCell cell : row) {
                if (!cell.isCompleted() && !cell.isLocked()) return false;
            }
        }
        return true;
    }

    // Si ya jugó esta partida (aunque se haya desconectado).
    public boolean isParticipant(Player player) {
        return participants.containsKey(player.getUniqueId());
    }

    public TeamRegistry<UUID> getTeams() {
        return teams;
    }

    // El equipo del jugador si está jugando esta partida.
    public Optional<Team> teamInGame(Player player) {
        return teams.teamOf(player.getUniqueId()).filter(team -> cards.containsKey(team.id()));
    }

    // Los compañeros conectados (él incluido); solo él si no tiene equipo.
    public List<Player> teammates(Player player) {
        Optional<Team> team = teams.teamOf(player.getUniqueId());
        if (team.isEmpty()) return List.of(player);
        return online(teams.members(team.get()));
    }

    public Optional<Team> teamById(String id) {
        return teamCards().keySet().stream().filter(team -> team.id().equals(id)).findFirst();
    }

    // Los equipos que juegan esta partida y su cartilla, en el orden en que se armaron.
    public Map<Team, BingoCard> teamCards() {
        Map<Team, BingoCard> result = new LinkedHashMap<>();
        for (Team team : teams.teams()) {
            BingoCard card = cards.get(team.id());
            if (card != null) result.put(team, card);
        }
        return result;
    }

    private static List<Player> online(List<UUID> ids) {
        List<Player> players = new ArrayList<>();
        for (UUID id : ids) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) players.add(player);
        }
        return players;
    }

    private void handle(GameSession.Signal signal) {
        switch (signal) {
            case COUNTDOWN -> {
                int left = session.countdownLeft();
                for (Player player : playersInGame()) {
                    player.sendTitle(Texts.get("cuenta.titulo", "segundos", left), Texts.get("cuenta.subtitulo"), 0, 25, 5);
                }
            }
            case STARTED -> {
                for (Player player : playersInGame()) {
                    player.sendTitle(Texts.get("cuenta.ya_titulo"),
                            Texts.get("cuenta.ya_subtitulo", "modo", settings.mode().description()), 0, 30, 10);
                }
                Bukkit.broadcastMessage(Texts.get("cuenta.a_jugar"));
                startListeners.forEach(Runnable::run);
            }
            case TIME_UP -> endByPoints(Texts.get("puntos.tiempo_agotado"));
            case NONE -> {
            }
        }
        if (session.inGame()) tickListeners.forEach(Runnable::run);
    }

    public int countdownLeft() {
        return session.countdownLeft();
    }

    public int elapsedSeconds() {
        return session.elapsedSeconds();
    }

    // 0 = sin límite.
    public int durationSeconds() {
        return session.durationSeconds();
    }

    // Los jugadores conectados que están jugando (con cartilla).
    public List<Player> playersInGame() {
        List<Player> players = new ArrayList<>();
        for (Team team : teamCards().keySet()) players.addAll(online(teams.members(team)));
        return players;
    }

    // "Equipo Rojo (Ana, Beto)" o "Ana" si juega solo.
    public String describe(Team team) {
        if (team.solo()) return team.name();
        List<String> names = new ArrayList<>();
        for (UUID id : teams.members(team)) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) names.add(player.getName());
        }
        return team.label() + (names.isEmpty() ? "" : " (" + String.join(", ", names) + ")");
    }

    // Termina por puntos (se acabó el tiempo o, en lockout, no quedan casillas libres): gana quien tenga
    // más casillas; si empatan, quien llegó primero a esa cantidad.
    public void endByPoints(String reason) {
        Map<String, Integer> completedByTeam = new LinkedHashMap<>();
        List<TimeLimitWinners.Score> scores = new ArrayList<>();
        for (Map.Entry<Team, BingoCard> entry : teamCards().entrySet()) {
            String name = describe(entry.getKey());
            BingoCard card = entry.getValue();
            completedByTeam.put(name, card.countCompleted());
            scores.add(new TimeLimitWinners.Score(name, card.countCompleted(), lastCompletedAt(card)));
        }
        List<String> leaders = TimeLimitWinners.winners(scores);
        List<Team> winners = new ArrayList<>();
        for (Team team : teamCards().keySet()) {
            if (leaders.contains(describe(team))) winners.add(team);
        }

        Bukkit.broadcastMessage(reason);
        if (leaders.isEmpty()) {
            Bukkit.broadcastMessage(Texts.get("puntos.nadie"));
        } else {
            if (leaders.size() == 1) {
                Bukkit.broadcastMessage(Texts.get("puntos.ganador", "ganador", leaders.get(0)));
            } else {
                Bukkit.broadcastMessage(Texts.get("puntos.empate", "ganadores", String.join(", ", leaders)));
            }
            Bukkit.broadcastMessage(Texts.get("puntos.casillas", "casillas", completedByTeam.get(leaders.get(0))));
        }
        finish(winners, false);
    }

    // Los que ganaron la última partida (vacío si se detuvo o nadie completó nada) y quiénes jugaron: para el
    // festejo y lo que pasa al terminar (EndActions).
    public List<Team> lastWinners() {
        return lastWinners;
    }

    public Set<UUID> lastParticipants() {
        return lastParticipants;
    }

    // En el chat, al terminar: cada equipo con sus casillas (las de los ganadores, una por una con el tiempo
    // y quién) y el MVP.
    private void broadcastSummary(List<Team> winners) {
        Map<String, BingoCard> byTeam = new LinkedHashMap<>();
        Map<String, Team> teamOfLabel = new HashMap<>();
        for (Map.Entry<Team, BingoCard> entry : teamCards().entrySet()) {
            String label = TeamColors.chat(entry.getKey().color()) + describe(entry.getKey());
            byTeam.put(label, entry.getValue());
            teamOfLabel.put(label, entry.getKey());
        }
        GameSummary summary = GameSummary.of(byTeam);
        if (summary.teams().stream().allMatch(result -> result.completed() == 0)) return;

        Bukkit.broadcastMessage(Texts.get("resumen.titulo"));
        for (GameSummary.TeamResult result : summary.teams()) {
            Bukkit.broadcastMessage(Texts.get("resumen.equipo", "equipo", result.team(), "casillas", result.completed()));
            if (!winners.contains(teamOfLabel.get(result.team()))) continue;
            for (GameSummary.Completion cell : result.cells()) {
                Bukkit.broadcastMessage(Texts.get("resumen.casilla", "tiempo", GameBossBar.clock(Math.max(0, cell.second())),
                        "tarea", cell.task(), "jugador", cell.by() == null ? "—" : cell.by()));
            }
        }
        if (summary.mvp() != null) {
            Bukkit.broadcastMessage(Texts.get("resumen.mvp", "jugador", summary.mvp(), "casillas", summary.mvpCells()));
        }
    }

    private static int lastCompletedAt(BingoCard card) {
        int last = -1;
        for (BingoCell[] row : card.getGrid()) {
            for (BingoCell cell : row) last = Math.max(last, cell.completedAt());
        }
        return last;
    }

    // Según el modo de config.yml (una línea, carta llena, …).
    public boolean checkWin(Player player) {
        BingoCard card = getCard(player);
        return card != null && settings.mode().wins(card);
    }

    // Casillas completas de la cartilla de su equipo.
    public int getProgress(UUID uuid) {
        BingoCard card = teams.teamOf(uuid).map(team -> cards.get(team.id())).orElse(null);
        return card == null ? 0 : card.countCompleted();
    }

    // En curso: las tareas cuentan.
    public boolean isRunning(){
        return session.phase() == Phase.RUNNING;
    }

    // En cuenta regresiva o en curso: hay cartillas repartidas.
    public boolean inGame() {
        return session.inGame();
    }

    public boolean inCountdown() {
        return session.phase() == Phase.COUNTDOWN;
    }

    public boolean isPaused() {
        return session.phase() == Phase.PAUSED;
    }

    // En la cuenta regresiva o en pausa nadie se mueve (SessionListener).
    public boolean isFrozen() {
        return inCountdown() || isPaused();
    }

    // /bingo pause: en curso → en pausa (el tiempo no corre, nada cuenta, nadie se mueve) y al revés.
    // false si no hay una partida en curso ni en pausa.
    public boolean togglePause(String by) {
        boolean pausing = session.pause();
        if (!pausing && !session.resume()) return false;

        String title = Texts.get(pausing ? "pausa.titulo" : "pausa.sigue_titulo");
        for (Player player : playersInGame()) player.sendTitle(title, Texts.get("pausa.subtitulo", "quien", by), 0, 30, 10);
        Bukkit.broadcastMessage(Texts.get(pausing ? "pausa.pausada" : "pausa.sigue", "quien", by));
        tickListeners.forEach(Runnable::run);
        return true;
    }

    public GameSettings getSettings() {
        return settings;
    }

    public void end(Player winner) {
        String who = teams.teamOf(winner.getUniqueId()).map(this::describe).orElse(winner.getName());
        Bukkit.broadcastMessage(Texts.get("partida.bingo"));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), "minecraft:ui.toast.challenge_complete", 1f, 1f);
        }
        Bukkit.broadcastMessage(Texts.get("partida.ganador", "ganador", who));
        Bukkit.broadcastMessage(Texts.get("partida.tiempo", "tiempo", getElapsedTime()));

        finish(teams.teamOf(winner.getUniqueId()).map(List::of).orElse(List.of()), false);
    }

    // También durante la cuenta regresiva.
    public boolean stop(String stoppedBy) {
        if (!session.inGame()) return false;

        Bukkit.broadcastMessage(Texts.get("partida.detenida", "quien", stoppedBy));
        finish(List.of(), true);
        return true;
    }

    // `aborted`: con /bingo stop (no cuenta para las estadísticas).
    private void finish(List<Team> winners, boolean aborted) {
        if (clock != null) {
            clock.cancel();
            clock = null;
        }
        session.finish();
        broadcastSummary(winners);
        lastWinners = List.copyOf(winners);
        lastParticipants = Set.copyOf(participants.keySet());
        events.publish(gameEnded(winners, aborted));
        endListeners.forEach(Runnable::run);
        cards.clear();
        participants.clear();
        teams.removeSolos();
    }

    private GameEnded gameEnded(List<Team> winners, boolean aborted) {
        Set<String> winnerIds = new HashSet<>();
        for (Team team : winners) winnerIds.add(team.id());
        List<GameEnded.Participant> players = new ArrayList<>();
        for (Map.Entry<UUID, String> entry : participants.entrySet()) {
            teams.teamOf(entry.getKey()).ifPresent(team -> players.add(
                    new GameEnded.Participant(entry.getKey().toString(), entry.getValue(), team.id())));
        }
        return new GameEnded(session.elapsedSeconds(), aborted, winnerIds, players);
    }

    // Aplica un evento a la cartilla del jugador, que estaba en `context`: qué casillas avanzaron y cuáles
    // se completaron.
    // Las casillas que se completan anotan el segundo (para desempatar) y, en lockout, se bloquean en las
    // cartillas de los demás equipos.
    public ProgressResult applyEvent(Player player, TaskEvent event, EventContext context) {
        Optional<Team> team = teamInGame(player);
        if (team.isEmpty()) return ProgressResult.NONE;

        ProgressResult result = ProgressTracker.apply(cards.get(team.get().id()), event, context);
        for (BingoCell cell : result.advanced()) {
            if (!cell.isCompleted()) continue;
            cell.markCompleted(session.elapsedSeconds(), player.getName());
            events.publish(new CellCompleted(team.get().id(), player.getUniqueId().toString(), player.getName(),
                    cell.getTask().id(), cell.getTask().displayName(), session.elapsedSeconds()));
            if (!settings.mode().lockout()) continue;
            for (Map.Entry<String, BingoCard> other : cards.entrySet()) {
                if (!other.getKey().equals(team.get().id())) lock(other.getValue(), cell.getTask().id(), team.get().id());
            }
        }
        return result;
    }

    // La cartilla de su equipo; null si no está jugando.
    public BingoCard getCard(Player player){
        return teams.teamOf(player.getUniqueId()).map(team -> cards.get(team.id())).orElse(null);
    }
    public String getElapsedTime() {
        int seconds = session.elapsedSeconds();
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }

    public Phase getPhase(){
        return session.phase();
    }
}
