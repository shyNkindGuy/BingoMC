package com.shyNkindguy.bingo.game;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class BingoGame {
    private final Plugin plugin;
    private GameState state = GameState.WAITING;
    private long startTime;
    private static final long MAX_TIME = 20 * 60 * 1000;
    private final Map<UUID, BingoCard> cards = new HashMap<>();

    public BingoGame(Plugin plugin){
        this.plugin = plugin;
    }

    public boolean start(Collection<? extends Player> players) {
        if (state == GameState.RUNNING) return false;

        state = GameState.RUNNING;
        startTime = System.currentTimeMillis();

        cards.clear();

        for (Player p : players) {
            cards.put(
                    p.getUniqueId(),
                    new BingoCard(ObjectiveFactory.createObjectives())
            );
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (state == GameState.RUNNING) {
                endByPoints();
            }
        }, 20L * 60 * 20);

        return true;
    }
    private void endByPoints() {
        Player winner = null;
        int max = -1;

        for (UUID id : cards.keySet()) {
            Player p = Bukkit.getPlayer(id);
            if (p == null) continue;

            int progress = getProgress(id);
            if (progress > max) {
                max = progress;
                winner = p;
            }
        }

        state = GameState.ENDED;

        Bukkit.broadcastMessage("§c⏱ Tiempo agotado");
        if (winner != null) {
            Bukkit.broadcastMessage("§eGanador: §a" + winner.getName());
            Bukkit.broadcastMessage("§7Objetivos: §f" + max);
        }

        cards.clear();
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
        state = GameState.ENDED;

        Bukkit.broadcastMessage("§6§lBINGO!");
        Bukkit.broadcastMessage(winner.getName() + " §ese los papeó :v§a");
        Bukkit.broadcastMessage("§7Tiempo: §f" + getElapsedTime());

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
