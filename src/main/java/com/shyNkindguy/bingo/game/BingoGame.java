package com.shyNkindguy.bingo.game;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class BingoGame {
    private final Plugin plugin;
    private GameState state = GameState.WAITING;
    private long startTime;
    private static final long MAX_TIME = 20 * 60 * 1000;
    private static final long MILLIS_PER_TICK = 50;
    private final Map<UUID, BingoCard> cards = new HashMap<>();
    private final List<Runnable> endListeners = new ArrayList<>();
    private BukkitTask timeLimitTask;

    public BingoGame(Plugin plugin){
        this.plugin = plugin;
    }

    // Se ejecuta cada vez que termina una partida (bingo, tiempo agotado o /bingo stop).
    public void onEnd(Runnable listener) {
        endListeners.add(listener);
    }

    public boolean start(Collection<? extends Player> players) {
        if (state == GameState.RUNNING || players.isEmpty()) return false;

        state = GameState.RUNNING;
        startTime = System.currentTimeMillis();

        cards.clear();

        for (Player p : players) {
            cards.put(
                    p.getUniqueId(),
                    new BingoCard(ObjectiveFactory.createObjectives())
            );
        }

        timeLimitTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (state == GameState.RUNNING) {
                endByPoints();
            }
        }, MAX_TIME / MILLIS_PER_TICK);

        return true;
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
        if (card == null) return 0;

        int completed = 0;
        for (BingoCell[] row : card.getGrid()) {
            for (BingoCell cell : row) {
                if (cell.isCompleted()) completed++;
            }
        }
        return completed;
    }

    public boolean isRunning(){
        return state == GameState.RUNNING;
    }
    public void end(Player winner) {
        Bukkit.broadcastMessage("§6§lBINGO!");
        Bukkit.broadcastMessage(winner.getName() + " §ese los papeó :v§a");
        Bukkit.broadcastMessage("§7Tiempo: §f" + getElapsedTime());

        finish();
    }

    public boolean stop(String stoppedBy) {
        if (state != GameState.RUNNING) return false;

        Bukkit.broadcastMessage("§c§lBINGO DETENIDO §7por " + stoppedBy);
        finish();
        return true;
    }

    private void finish() {
        if (timeLimitTask != null) {
            timeLimitTask.cancel();
            timeLimitTask = null;
        }
        state = GameState.ENDED;
        endListeners.forEach(Runnable::run);
        cards.clear();
    }
    public boolean completePickup(Player player, Material material) {
        BingoCard card = cards.get(player.getUniqueId());
        if (card == null) return false;

        return card.completeByMaterial(material);
    }

    public BingoCard getCard(Player player){
        return cards.get(player.getUniqueId());
    }
    public String getElapsedTime() {
        long seconds = (System.currentTimeMillis() - startTime) / 1000;
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }

    public GameState getState(){
        return state;
    }
}
