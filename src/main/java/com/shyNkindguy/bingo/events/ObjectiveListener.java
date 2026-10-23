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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// Una casilla cuenta solo si el ítem está de verdad en el inventario del jugador: cada evento pide
// revisar su inventario un tick después (cuando el ítem ya llegó), no mira el ítem del evento.
public class ObjectiveListener implements Listener {

    private final Plugin plugin;
    private final BingoGame game;
    private final BingoScoreBoard scoreboard;
    private final BingoMapManager mapManager;
    private final ScanQueue<UUID> scanQueue = new ScanQueue<>();


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
        mapManager.invalidate(player);

        if (game.checkWin(player)) {
            game.end(player);
        }
    }
    private void requestScan(Player player) {
        if (!game.isRunning()) return;
        if (!scanQueue.request(player.getUniqueId())) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            scanQueue.done(player.getUniqueId());
            if (game.isRunning() && player.isOnline()) {
                scanInventory(player);
            }
        });
    }
    private void scanInventory(Player player) {
        Set<Material> seen = new HashSet<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir() || mapManager.isBingoMap(item)) continue;
            if (!seen.add(item.getType())) continue;

            handleProgress(player, item.getType());
            if (!game.isRunning()) return; // ganó con este ítem
        }
    }
    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event){
        //cubos lava, leche, agua
        requestScan(event.getPlayer());
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        requestScan(player);
    }
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        requestScan(player);
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // lo que quedó en el cursor vuelve al inventario al cerrar
        if (!(event.getPlayer() instanceof Player player)) return;
        requestScan(player);
    }
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        requestScan(player);
    }
    @EventHandler
    public void onSmelt(FurnaceExtractEvent event) {
        requestScan(event.getPlayer());
    }
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event){
        if (!(event.getEntity() instanceof Player player)) return;
        requestScan(player);
    }
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        // la cartilla no se cae: se devuelve al reaparecer
        event.getDrops().removeIf(mapManager::isBingoMap);
    }
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (mapManager.isBingoMap(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
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
