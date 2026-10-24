package com.shyNkindguy.bingo.ui;

import com.shyNkindguy.bingo.adapter.Keys;
import com.shyNkindguy.bingo.core.achievement.Achievement;
import com.shyNkindguy.bingo.core.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

// /bingo logros: los logros de achievements.yml en un cofre de solo mirar; los desbloqueados con su ícono y
// brillo, los que faltan en gris. Entran los primeros 54 (6 filas).
public class AchievementMenu implements ReadOnlyMenu {
    private static final int COLUMNS = 9;
    private static final int MAX_SLOTS = 54;

    // Lo que se escribe en cada uno (sin Bukkit, para los tests).
    record Entry(String name, List<String> lore) {
    }

    private final Inventory inventory;

    private AchievementMenu(List<Achievement> achievements, PlayerStats stats) {
        List<Achievement> shown = achievements.subList(0, Math.min(achievements.size(), MAX_SLOTS));
        int rows = (shown.size() + COLUMNS - 1) / COLUMNS;
        long unlocked = achievements.stream().filter(achievement -> stats.hasAchievement(achievement.id())).count();
        inventory = Bukkit.createInventory(this, rows * COLUMNS,
                Texts.get("logros.menu_titulo", "desbloqueados", unlocked, "total", achievements.size()));
        for (int slot = 0; slot < shown.size(); slot++) {
            inventory.setItem(slot, item(shown.get(slot), stats.hasAchievement(shown.get(slot).id())));
        }
    }

    public static void open(Player player, List<Achievement> achievements, PlayerStats stats) {
        if (achievements.isEmpty()) {
            player.sendMessage(Texts.get("logros.ninguno"));
            return;
        }
        player.openInventory(new AchievementMenu(achievements, stats).inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    static Entry entry(Achievement achievement, boolean unlocked) {
        String name = (unlocked ? "§a" : "§7") + achievement.name();
        return new Entry(name, List.of("§f" + achievement.description(),
                Texts.get(unlocked ? "logros.desbloqueado" : "logros.bloqueado")));
    }

    private static ItemStack item(Achievement achievement, boolean unlocked) {
        Entry entry = entry(achievement, unlocked);
        ItemStack item = new ItemStack(unlocked ? icon(achievement) : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(entry.name());
        meta.setLore(entry.lore());
        if (unlocked) meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        return item;
    }

    private static Material icon(Achievement achievement) {
        Material material = achievement.icon() == null ? null : Keys.toMaterial(achievement.icon());
        return material != null && material.isItem() ? material : Material.NETHER_STAR;
    }
}
