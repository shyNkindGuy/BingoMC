package com.shyNkindguy.bingo.events;

import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class ObjectiveListener implements Listener {

    private final Plugin plugin;
    private final BingoGame game;
    private final BingoScoreBoard scoreboard;
    private final BingoMapManager mapManager;


    public ObjectiveListener(Plugin plugin, BingoGame game, BingoScoreBoard scoreboard , BingoMapManager mapManager) {
        this.plugin = plugin;
        this.game = game;
        this.scoreboard = scoreboard;
        this.mapManager = mapManager;
    }
    private void handleProgress(Player player, Material mat) {
        boolean completed = game.completePickup(player, mat);

        if (!completed) return;

        player.sendMessage("§a✔ Objetivo completado: §f" + mat.name());
        scoreboard.update(player);

        if (game.checkWin(player)) {
            game.end(player);
        }
    }
    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event){
        if (!game.isRunning()) return;
        Player player = event.getPlayer();
        Material result = event.getItemStack().getType();
        //cubos lava, leche, agua
        boolean completed = game.completePickup(player, result);
        if (completed){
            player.sendMessage("§a✔ Objetivo completado: §f" + result.name());
            scoreboard.update(player);
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!game.isRunning()) return;

        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        handleProgress(player, item.getType());
    }
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!game.isRunning()) return;

        handleProgress(player, event.getRecipe().getResult().getType());
    }
    @EventHandler
    public void onSmelt(FurnaceExtractEvent event) {
        Player player = event.getPlayer();
        if (!game.isRunning()) return;

        handleProgress(event.getPlayer(), event.getItemType());
    }
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event){
            if(!(event.getEntity() instanceof Player player)) return;
            if (!game.isRunning()) return;

        handleProgress(player, event.getItem().getItemStack().getType());

    }
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();

        if (game.isRunning()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                mapManager.giveMap(player);
            }, 1L);
        }
    }
}
