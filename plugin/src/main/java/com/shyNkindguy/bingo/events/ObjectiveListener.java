package com.shyNkindguy.bingo.events;

import com.shyNkindguy.bingo.adapter.Keys;
import com.shyNkindguy.bingo.core.event.AdvancementDone;
import com.shyNkindguy.bingo.core.event.EntityKilled;
import com.shyNkindguy.bingo.core.event.InventoryScanned;
import com.shyNkindguy.bingo.core.event.ItemObtained;
import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.items.SpecialItems;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Traduce eventos de Paper a eventos del núcleo. Una casilla de ítem cuenta solo si el ítem está de verdad
// en el inventario del jugador: cada evento pide revisar su inventario un tick después (cuando el ítem ya
// llegó), no mira el ítem del evento. Las muertes de mobs y los logros pasan directo.
public class ObjectiveListener implements Listener {

    private final Plugin plugin;
    private final BingoGame game;
    private final BingoMapManager mapManager;
    private final CardUpdater updater;
    private final SpecialItems specialItems;
    private final ScanQueue<UUID> scanQueue = new ScanQueue<>();


    public ObjectiveListener(Plugin plugin, BingoGame game, BingoMapManager mapManager, CardUpdater updater,
                             SpecialItems specialItems) {
        this.plugin = plugin;
        this.game = game;
        this.mapManager = mapManager;
        this.updater = updater;
        this.specialItems = specialItems;
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
    // Cuenta cuántos tiene de cada ítem y se lo pasa a la cartilla como ItemObtained(ítem, total). La cartilla y
    // los ítems especiales no cuentan.
    private void scanInventory(Player player) {
        Map<Material, Integer> totals = new HashMap<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType().isAir() || mapManager.isBingoMap(item) || specialItems.isSpecial(item)) continue;
            totals.merge(item.getType(), item.getAmount(), Integer::sum);
        }

        List<TaskEvent> events = new ArrayList<>();
        Map<String, Integer> byKey = new HashMap<>();
        for (Map.Entry<Material, Integer> total : totals.entrySet()) {
            String item = Keys.of(total.getKey());
            events.add(new ItemObtained(item, total.getValue()));
            byKey.put(item, total.getValue());
        }
        // y el inventario entero, para los conjuntos ("3 lanas distintas")
        events.add(new InventoryScanned(byKey));
        updater.apply(player, events);
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // cuenta para el que lo mató (también si es otro jugador: "minecraft:player")
        Player killer = event.getEntity().getKiller();
        String entity = Keys.of(event.getEntityType());
        if (killer == null || entity == null) return;
        updater.apply(killer, List.of(new EntityKilled(entity)));
    }
    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        String advancement = event.getAdvancement().getKey().toString();
        updater.apply(event.getPlayer(), List.of(new AdvancementDone(advancement)));
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
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        // para las tareas con condición de dimensión: lo que ya lleva puede contar en el mundo nuevo
        requestScan(event.getPlayer());
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

        if (game.inGame()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                mapManager.giveMap(player);
            }, 1L);
        }
    }
}
