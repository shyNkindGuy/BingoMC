package com.shyNkindguy.bingo.items;

import com.shyNkindguy.bingo.adapter.Keys;
import com.shyNkindguy.bingo.core.item.SpecialItem;
import com.shyNkindguy.bingo.core.status.ItemSet;
import com.shyNkindguy.bingo.core.status.PlayerStatus;
import com.shyNkindguy.bingo.ui.Texts;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

// Los ítems especiales de items.yml y las piezas de los conjuntos de sets.yml como ItemStack: el ítem de
// Minecraft con nombre, descripción y una marca PDC (adaptivebingo:especial = id, adaptivebingo:pieza =
// "conjunto:posición"). Con la marca se reconocen y no cuentan para las casillas.
public class SpecialItems {
    // Lo que puede salir en el modo caos.
    public record Drop(String name, ItemStack stack) {
    }

    private final NamespacedKey key;
    private final NamespacedKey pieceKey;
    private final Supplier<List<SpecialItem>> items;
    private final Supplier<List<ItemSet>> sets;

    public SpecialItems(Plugin plugin, Supplier<List<SpecialItem>> items, Supplier<List<ItemSet>> sets) {
        this.key = new NamespacedKey(plugin, "especial");
        this.pieceKey = new NamespacedKey(plugin, "pieza");
        this.items = items;
        this.sets = sets;
    }

    public List<ItemSet> sets() {
        return sets.get();
    }

    public Optional<ItemSet> findSet(String id) {
        return sets().stream().filter(set -> set.id().equals(id)).findFirst();
    }

    public List<String> setIds() {
        return sets().stream().map(ItemSet::id).toList();
    }

    public ItemStack createPiece(ItemSet set, int index) {
        ItemSet.Piece piece = set.pieces().get(index);
        Material material = Keys.toMaterial(piece.material());
        ItemStack item = new ItemStack(material != null ? material : Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(pieceName(piece));
        meta.setLore(pieceLore(set));
        meta.getPersistentDataContainer().set(pieceKey, PersistentDataType.STRING, set.id() + ":" + index);
        item.setItemMeta(meta);
        return item;
    }

    public static String pieceName(ItemSet.Piece piece) {
        return "§b" + piece.name();
    }

    static List<String> pieceLore(ItemSet set) {
        List<String> statuses = set.statuses().stream().map(PlayerStatus::description).toList();
        return List.of(Texts.get("conjuntos.lore", "conjunto", set.name(), "necesita", set.needed(),
                "piezas", set.pieces().size(), "estados", String.join(", ", statuses)));
    }

    // Qué piezas (por posición) lleva de cada conjunto, en cualquier lugar del inventario.
    public Map<String, Set<Integer>> piecesCarried(Player player) {
        Map<String, Set<Integer>> carried = new HashMap<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !item.hasItemMeta()) continue;
            String mark = item.getItemMeta().getPersistentDataContainer().get(pieceKey, PersistentDataType.STRING);
            if (mark == null) continue;
            int colon = mark.lastIndexOf(':');
            try {
                carried.computeIfAbsent(mark.substring(0, colon), id -> new HashSet<>())
                        .add(Integer.parseInt(mark.substring(colon + 1)));
            } catch (RuntimeException e) {
                // una marca rota (a mano): no cuenta
            }
        }
        return carried;
    }

    // Cada ítem especial y cada pieza de cada conjunto, para el modo caos.
    public List<Drop> chaosPool() {
        List<Drop> pool = new ArrayList<>();
        for (SpecialItem item : all()) pool.add(new Drop(displayName(item), create(item)));
        for (ItemSet set : sets()) {
            for (int i = 0; i < set.pieces().size(); i++) pool.add(new Drop(pieceName(set.pieces().get(i)), createPiece(set, i)));
        }
        return pool;
    }

    public List<SpecialItem> all() {
        return items.get();
    }

    public Optional<SpecialItem> find(String id) {
        return all().stream().filter(item -> item.id().equals(id)).findFirst();
    }

    public List<String> ids() {
        return all().stream().map(SpecialItem::id).toList();
    }

    public ItemStack create(SpecialItem special) {
        Material material = Keys.toMaterial(special.material());
        ItemStack item = new ItemStack(material != null ? material : Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName(special));
        meta.setLore(List.of("§7" + special.description(), Texts.get("especiales.lore_uso")));
        meta.setEnchantmentGlintOverride(true);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, special.id());
        item.setItemMeta(meta);
        return item;
    }

    public static String displayName(SpecialItem special) {
        return "§d" + special.name();
    }

    // Un ítem especial o una pieza de un conjunto.
    public boolean isSpecial(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        var data = item.getItemMeta().getPersistentDataContainer();
        return data.has(key, PersistentDataType.STRING) || data.has(pieceKey, PersistentDataType.STRING);
    }

    // El de items.yml que lleva este ítem (vacío si no es especial o se borró de items.yml).
    public Optional<SpecialItem> of(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return Optional.empty();
        String id = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return id == null ? Optional.empty() : find(id);
    }

    public void give(Player player, SpecialItem special) {
        give(player, create(special));
    }

    // Al inventario; si no entra, al piso a sus pies.
    public void give(Player player, ItemStack stack) {
        for (ItemStack left : player.getInventory().addItem(stack).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), left);
        }
    }

    // Al terminar la partida: ítems especiales y piezas fuera de los inventarios de los que están conectados.
    public void removeAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerInventory inventory = player.getInventory();
            ItemStack[] contents = inventory.getContents();
            for (int slot = 0; slot < contents.length; slot++) {
                if (isSpecial(contents[slot])) inventory.setItem(slot, null);
            }
        }
    }
}
