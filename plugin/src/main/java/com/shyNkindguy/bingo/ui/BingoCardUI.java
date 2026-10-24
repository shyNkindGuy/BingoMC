//package com.shyNkindguy.bingo.ui;
//
//import com.shyNkindguy.bingo.game.BingoCard;
//import com.shyNkindguy.bingo.game.BingoCell;
//import org.bukkit.*;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.*;
//import org.bukkit.inventory.meta.ItemMeta;
//
//public class BingoCardUI {
//    private static final int[] GRID_SLOTS = {
//            10,11,12,13,14,
//            19,20,21,22,23,
//            28,29,30,31,32,
//            37,38,39,40,41,
//            46,47,48,49,50
//    };
//
//    public static void open(Player player, BingoCard card) {
//        Inventory inv = Bukkit.createInventory(
//                null,
//                54,
//                "§a§lBINGO"
//        );
//
//        BingoCell[][] grid = card.getGrid();
//        int index = 0;
//
//        for (int row = 0; row < 5; row++) {
//            for (int col = 0; col < 5; col++) {
//                BingoCell cell = grid[row][col];
//                ItemStack item = createItem(cell);
//                inv.setItem(GRID_SLOTS[index++], item);
//            }
//        }
//
//        player.openInventory(inv);
//    }
//
//    private static ItemStack createItem(BingoCell cell) {
//        Material mat = cell.getObjective().getMaterial();
//        ItemStack item = new ItemStack(mat);
//        ItemMeta meta = item.getItemMeta();
//
//        if (cell.isCompleted()) {
//            meta.setDisplayName("§a✔ " + cell.getObjective().getDisplayName());
//        } else {
//            meta.setDisplayName("§c✘ " + cell.getObjective().getDisplayName());
//        }
//
//        item.setItemMeta(meta);
//        return item;
//    }
//    public static void refresh(Player player, BingoCard card) {
//        Inventory inv = player.getOpenInventory().getTopInventory();
//        if (inv == null) return;
//        if (!player.getOpenInventory().getTitle().equals("§a§lBINGO")) return;
//
//        BingoCell[][] grid = card.getGrid();
//        int index = 0;
//
//        for (int r = 0; r < 5; r++) {
//            for (int c = 0; c < 5; c++) {
//                inv.setItem(GRID_SLOTS[index++], createItem(grid[r][c]));
//            }
//        }
//    }
//}
