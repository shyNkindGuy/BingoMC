package com.shyNkindguy.bingo.game;


import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.event.EventContext;
import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.core.game.GameSession;
import com.shyNkindguy.bingo.core.game.GameSettings;
import com.shyNkindguy.bingo.core.game.Phase;
import com.shyNkindguy.bingo.core.progress.ProgressResult;
import com.shyNkindguy.bingo.core.progress.ProgressTracker;
import com.shyNkindguy.bingo.core.task.Task;
import com.shyNkindguy.bingo.core.win.TimeLimitWinners;
import com.shyNkindguy.bingo.core.win.WinConditionChecker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

// La partida en el servidor: las cartillas por jugador y un reloj de un segundo que hace avanzar la
// GameSession del núcleo (cuenta regresiva → en curso → tiempo agotado).
public class BingoGame {
    private static final long TICKS_PER_SECOND = 20;

    private final Plugin plugin;
    private final GameSession session = new GameSession();
    private final Map<UUID, BingoCard> cards = new HashMap<>();
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
        for (Player p : players) {
            cards.put(p.getUniqueId(), new BingoCard(this.tasks));
        }

        GameSession.Signal first = session.begin(settings.countdownSeconds(), settings.durationSeconds());
        clock = Bukkit.getScheduler().runTaskTimer(plugin, () -> handle(session.tick()),
                TICKS_PER_SECOND, TICKS_PER_SECOND);
        handle(first);
        return true;
    }

    // Quien entra con la partida empezada (o en la cuenta regresiva) recibe una cartilla con las mismas
    // tareas; si ya tenía una (se desconectó y volvió), sigue con la suya. null si no hay partida.
    public BingoCard join(Player player) {
        if (!session.inGame()) return null;
        return cards.computeIfAbsent(player.getUniqueId(), id -> new BingoCard(tasks));
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
                    player.sendTitle("§a¡YA!", "§7Completa una línea", 0, 30, 10);
                }
                Bukkit.broadcastMessage("§a§l¡A JUGAR!");
                startListeners.forEach(Runnable::run);
            }
            case TIME_UP -> endByPoints();
            case NONE -> {
            }
        }
    }

    private List<Player> playersInGame() {
        List<Player> players = new ArrayList<>();
        for (UUID id : cards.keySet()) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) players.add(player);
        }
        return players;
    }

    private void endByPoints() {
        Map<String, Integer> completedByPlayer = new LinkedHashMap<>();
        for (UUID id : cards.keySet()) {
            Player p = Bukkit.getPlayer(id);
            if (p == null) continue;
            completedByPlayer.put(p.getName(), getProgress(id));
        }
        List<String> leaders = TimeLimitWinners.leaders(completedByPlayer);

        Bukkit.broadcastMessage("§c⏱ Tiempo agotado");
        if (leaders.isEmpty()) {
            Bukkit.broadcastMessage("§7Nadie completó casillas.");
        } else {
            if (leaders.size() == 1) {
                Bukkit.broadcastMessage("§eGanador: §a" + leaders.get(0));
            } else {
                Bukkit.broadcastMessage("§eEmpate entre: §a" + String.join("§e, §a", leaders));
            }
            Bukkit.broadcastMessage("§7Objetivos: §f" + completedByPlayer.get(leaders.get(0)));
        }

        finish();
    }


    public boolean checkWin(Player player) {
        BingoCard card = cards.get(player.getUniqueId());
        return card != null && WinConditionChecker.hasBingo(card);
    }

    public int getProgress(UUID uuid) {
        BingoCard card = cards.get(uuid);
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
        Bukkit.broadcastMessage("§6§lBINGO!");
        Bukkit.broadcastMessage(winner.getName() + " §ese los papeó :v§a");
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
    }
    // Aplica un evento a la cartilla del jugador, que estaba en `context`: qué casillas avanzaron y cuáles
    // se completaron.
    public ProgressResult applyEvent(Player player, TaskEvent event, EventContext context) {
        BingoCard card = cards.get(player.getUniqueId());
        if (card == null) return ProgressResult.NONE;

        return ProgressTracker.apply(card, event, context);
    }

    public BingoCard getCard(Player player){
        return cards.get(player.getUniqueId());
    }
    public String getElapsedTime() {
        int seconds = session.elapsedSeconds();
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }

    public Phase getPhase(){
        return session.phase();
    }
}
