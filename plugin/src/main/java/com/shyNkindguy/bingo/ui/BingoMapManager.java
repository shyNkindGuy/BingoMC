package com.shyNkindguy.bingo.ui;
import com.shyNkindguy.bingo.game.BingoGame;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BingoMapManager {
    private final BingoGame game;
    private final Map<UUID, MapView> maps = new HashMap<>();
    // Marca que llevan las cartillas: así se reconocen aunque cambie el id del mapa o se reinicie el servidor.
    private final NamespacedKey cardKey;

    public BingoMapManager(Plugin plugin, BingoGame game) {
        this.game = game;
        this.cardKey = new NamespacedKey(plugin, "cartilla");
    }
    public void invalidate(Player player) {
        MapView map = maps.get(player.getUniqueId());
        if (map == null) return;

        map.getRenderers().forEach(renderer -> {
            if (renderer instanceof BingoMapRenderer br) {
                br.invalidate();
            }
        });
    }

    public void giveMap(Player player) {

        MapView map = maps.computeIfAbsent(
                player.getUniqueId(),
                id -> Bukkit.createMap(player.getWorld())
        );
        map.getRenderers().clear();
        map.addRenderer(new BingoMapRenderer(game));

        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();

        meta.setMapView(map);
        meta.setDisplayName("§aBINGO HUMILDE");
        meta.getPersistentDataContainer().set(cardKey, PersistentDataType.BYTE, (byte) 1);
        mapItem.setItemMeta(meta);

        // offhand: sin cartillas repetidas, y lo que había en la mano izquierda no se pierde
        PlayerInventory inventory = player.getInventory();
        removeMapsFrom(inventory);
        ItemStack previous = inventory.getItemInOffHand();
        inventory.setItemInOffHand(mapItem);
        if (!previous.getType().isAir()) {
            // pasa al inventario; si no entra, se suelta a los pies del jugador
            for (ItemStack leftover : inventory.addItem(previous).values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
    }

    public boolean isBingoMap(ItemStack item) {
        if (item == null || item.getType() != Material.FILLED_MAP || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(cardKey, PersistentDataType.BYTE);
    }

    // Al terminar la partida: quita la cartilla del inventario de cada jugador conectado.
    public void removeMaps() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeMapsFrom(player.getInventory());
        }
    }

    private void removeMapsFrom(PlayerInventory inventory) {
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            if (isBingoMap(contents[slot])) {
                inventory.setItem(slot, null);
            }
        }
    }
}
