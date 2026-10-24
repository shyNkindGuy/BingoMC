package com.shyNkindguy.bingo.game;


import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;
import com.shyNkindguy.bingo.core.event.EventContext;
import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.core.game.GameSession;
import com.shyNkindguy.bingo.core.game.GameSettings;
import com.shyNkindguy.bingo.core.game.Phase;
import com.shyNkindguy.bingo.core.progress.ProgressResult;
import com.shyNkindguy.bingo.core.progress.ProgressTracker;
import com.shyNkindguy.bingo.core.task.Task;
import com.shyNkindguy.bingo.core.team.Team;
import com.shyNkindguy.bingo.core.team.TeamRegistry;
import com.shyNkindguy.bingo.core.win.TimeLimitWinners;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

// La partida en el servidor: una cartilla por equipo (quien no tiene equipo juega solo, en un equipo de 1)
// y un reloj de un segundo que hace avanzar la GameSession del núcleo (cuenta regresiva → en curso →
// tiempo agotado).
public class BingoGame {
    private static final long TICKS_PER_SECOND = 20;

    private final Plugin plugin;
    private final GameSession session = new GameSession();
    private final TeamRegistry<UUID> teams = new TeamRegistry<>();
    private final Map<String, BingoCard> cards = new HashMap<>(); // id del equipo → cartilla
    private final Set<UUID> participants = new HashSet<>();
    private final List<Runnable> startListeners = new ArrayList<>();
    private final List<Runnable> endListeners = new ArrayList<>();
    private List<Task> tasks = List.of();
    private GameSettings settings = GameSettings.DEFAULT;
    private BukkitTask clock;

    public BingoGame(Plugin plugin){
        this.plugin = plugin;
    }

    // Se ejecuta cuando termina la cuenta regresiva y la partida queda en curso.
    public void onStart(Runnable listener) {
        startListeners.add(listener);
    }

    // Se ejecuta cada vez que termina una partida (bingo, tiempo agotado o /bingo stop).
    public void onEnd(Runnable listener) {
        endListeners.add(listener);
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
            participants.add(p.getUniqueId());
            Team team = teams.soloIfAlone(p.getUniqueId(), p.getName());
            cards.computeIfAbsent(team.id(), this::newCard);
        }

        GameSession.Signal first = session.begin(settings.countdownSeconds(), settings.durationSeconds());
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
        participants.add(player.getUniqueId());
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
        return participants.contains(player.getUniqueId());
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
                    player.sendTitle("§e" + left, "§7El bingo empieza en…", 0, 25, 5);
                }
            }
            case STARTED -> {
                for (Player player : playersInGame()) {
                    player.sendTitle("§a¡YA!", "§7Gana: " + settings.mode().description(), 0, 30, 10);
                }
                Bukkit.broadcastMessage("§a§l¡A JUGAR!");
                startListeners.forEach(Runnable::run);
            }
            case TIME_UP -> endByPoints("§c⏱ Tiempo agotado");
            case NONE -> {
            }
        }
    }

    private List<Player> playersInGame() {
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

        Bukkit.broadcastMessage(reason);
        if (leaders.isEmpty()) {
            Bukkit.broadcastMessage("§7Nadie completó casillas.");
        } else {
            if (leaders.size() == 1) {
                Bukkit.broadcastMessage("§eGanador: §a" + leaders.get(0));
            } else {
                Bukkit.broadcastMessage("§eEmpate entre: §a" + String.join("§e, §a", leaders));
            }
            Bukkit.broadcastMessage("§7Objetivos: §f" + completedByTeam.get(leaders.get(0)));
        }

        finish();
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

    public GameSettings getSettings() {
        return settings;
    }

    public void end(Player winner) {
        String who = teams.teamOf(winner.getUniqueId()).map(this::describe).orElse(winner.getName());
        Bukkit.broadcastMessage("§6§lBINGO!");
        Bukkit.broadcastMessage(who + " §ese los papeó :v§a");
        Bukkit.broadcastMessage("§7Tiempo: §f" + getElapsedTime());

        finish();
    }

    // También durante la cuenta regresiva.
    public boolean stop(String stoppedBy) {
        if (!session.inGame()) return false;

        Bukkit.broadcastMessage("§c§lBINGO DETENIDO §7por " + stoppedBy);
        finish();
        return true;
    }

    private void finish() {
        if (clock != null) {
            clock.cancel();
            clock = null;
        }
        session.finish();
        endListeners.forEach(Runnable::run);
        cards.clear();
        participants.clear();
        teams.removeSolos();
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
            cell.markCompletedAt(session.elapsedSeconds());
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
