package com.shyNkindguy.bingo.items;

import com.shyNkindguy.bingo.adapter.Keys;
import com.shyNkindguy.bingo.core.item.SpecialItem;
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

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

// Los ítems especiales de items.yml como ItemStack: el ítem de Minecraft con nombre, descripción y la marca
// PDC adaptivebingo:especial = id. Con la marca se reconocen al usarlos y no cuentan para las casillas.
public class SpecialItems {
    private final NamespacedKey key;
    private final Supplier<List<SpecialItem>> items;

    public SpecialItems(Plugin plugin, Supplier<List<SpecialItem>> items) {
        this.key = new NamespacedKey(plugin, "especial");
        this.items = items;
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

    public boolean isSpecial(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING);
    }

    // El de items.yml que lleva este ítem (vacío si no es especial o se borró de items.yml).
    public Optional<SpecialItem> of(ItemStack item) {
        if (!isSpecial(item)) return Optional.empty();
        return find(item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING));
    }

    // Al inventario; si no entra, al piso a sus pies.
    public void give(Player player, SpecialItem special) {
        for (ItemStack left : player.getInventory().addItem(create(special)).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), left);
        }
    }

    // Al terminar la partida: fuera de los inventarios de los que están conectados.
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
