package com.shyNkindguy.bingo.ui;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;
import com.shyNkindguy.bingo.core.task.AdvancementTask;
import com.shyNkindguy.bingo.core.task.ConditionalTask;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.KillTask;
import com.shyNkindguy.bingo.core.task.SetTask;
import com.shyNkindguy.bingo.core.task.StatisticTask;
import com.shyNkindguy.bingo.core.task.Task;
import com.shyNkindguy.bingo.core.team.Team;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.icon.TaskIcons;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// /bingo card: la cartilla del equipo en un cofre de solo mirar (SessionListener cancela los clics), con el
// tipo, el avance, las condiciones y si está completa o bloqueada en la descripción de cada casilla. Hasta
// 6×6 se ve como grilla; una 7×7 va en orden, de izquierda a derecha.
public class CardMenu implements InventoryHolder {
    private static final int COLUMNS = 9;
    private static final int MAX_ROWS = 6;

    private final Inventory inventory;

    private CardMenu(BingoGame game, Team team, BingoCard card) {
        int size = card.getSize();
        boolean grid = size <= MAX_ROWS;
        int rows = grid ? size : MAX_ROWS;
        inventory = Bukkit.createInventory(this, rows * COLUMNS, Texts.get("cartilla.titulo", "equipo", team.label()));

        int offset = (COLUMNS - size) / 2;
        BingoCell[][] cells = card.getGrid();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int slot = grid ? row * COLUMNS + offset + col : row * size + col;
                inventory.setItem(slot, item(game, cells[row][col]));
            }
        }
    }

    public static void open(Player player, BingoGame game) {
        BingoCard card = game.getCard(player);
        Optional<Team> team = game.teamInGame(player);
        if (card == null || team.isEmpty()) {
            player.sendMessage(Texts.get("cartilla.no_jugando"));
            return;
        }
        player.openInventory(new CardMenu(game, team.get(), card).inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    private static ItemStack item(BingoGame game, BingoCell cell) {
        Task task = cell.getTask();
        Material material = TaskIcons.material(task);
        ItemStack item = new ItemStack(material != null && material.isItem() ? material : Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        String color = cell.isCompleted() ? "§a" : cell.isLocked() ? "§c" : "§f";
        meta.setDisplayName(color + task.displayName());
        meta.setLore(lore(game, cell));
        if (cell.isCompleted()) meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        return item;
    }

    private static List<String> lore(BingoGame game, BingoCell cell) {
        Task task = cell.getTask();
        List<String> lore = new ArrayList<>();
        switch (task.base()) {
            case ItemTask item -> lore.add(Texts.get("cartilla.tipo_item"));
            case KillTask kill -> lore.add(Texts.get("cartilla.tipo_matar"));
            case AdvancementTask advancement -> lore.add(Texts.get("cartilla.tipo_logro"));
            case StatisticTask statistic -> lore.add(Texts.get("cartilla.tipo_estadistica"));
            case SetTask set -> {
                lore.add(set.all() ? Texts.get("cartilla.tipo_todos")
                        : Texts.get("cartilla.tipo_alguno", "necesita", set.needed()));
                for (String key : set.items()) lore.add(Texts.get("cartilla.item_de", "item", shortKey(key)));
            }
            default -> {
            }
        }
        if (task instanceof ConditionalTask conditional) {
            lore.add(Texts.get("cartilla.condiciones", "condiciones", conditional.condition().description()));
        }
        if (cell.isCompleted()) {
            lore.add(Texts.get("cartilla.completada"));
        } else if (cell.isLocked()) {
            String owner = game.teamById(cell.lockedBy()).map(team -> TeamColors.chat(team.color()) + team.label())
                    .orElse(cell.lockedBy());
            lore.add(Texts.get("cartilla.bloqueada", "equipo", owner));
        } else if (task.target() > 1) {
            lore.add(Texts.get("cartilla.progreso", "actual", cell.getProgress().current(), "objetivo", task.target()));
        }
        return lore;
    }

    private static String shortKey(String key) {
        return key.startsWith("minecraft:") ? key.substring("minecraft:".length()) : key;
    }
}
