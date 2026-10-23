package com.shyNkindguy.bingo.ui;
import com.shyNkindguy.bingo.game.BingoGame;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BingoMapManager {
    private final BingoGame game;
    private final Map<UUID, MapView> maps = new HashMap<>();

    public BingoMapManager(BingoGame game) {
        this.game = game;
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
                id -> {
                    MapView m = Bukkit.createMap(player.getWorld());
                    m.getRenderers().clear();
                    m.addRenderer(new BingoMapRenderer(game));
                    return m;
                }
        );
        map.getRenderers().clear();
        map.addRenderer(new BingoMapRenderer(game));

        ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
        MapMeta meta = (MapMeta) mapItem.getItemMeta();

        meta.setMapView(map);
        meta.setDisplayName("§aBINGO HUMILDE");
        mapItem.setItemMeta(meta);

        // offhand
        player.getInventory().setItemInOffHand(mapItem);
    }

    // Al terminar la partida: quita la cartilla del inventario de cada jugador conectado.
    public void removeMaps() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            MapView map = maps.get(player.getUniqueId());
            if (map == null) continue;

            PlayerInventory inventory = player.getInventory();
            ItemStack[] contents = inventory.getContents();
            for (int slot = 0; slot < contents.length; slot++) {
                if (isBingoMap(contents[slot], map)) {
                    inventory.setItem(slot, null);
                }
            }
        }
    }

    private static boolean isBingoMap(ItemStack item, MapView map) {
        if (item == null || item.getType() != Material.FILLED_MAP) return false;
        return item.getItemMeta() instanceof MapMeta meta
                && meta.hasMapView()
                && meta.getMapView() != null
                && meta.getMapView().getId() == map.getId();
    }
}
