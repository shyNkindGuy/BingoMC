package com.shyNkindguy.bingo.game;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class MapIconRegistry {
    private static final Map<Material, byte[][]> ICONS = new HashMap<>();

    static {
        register(Material.OAK_LOG, IconPresets.LOG);
        register(Material.DIAMOND, IconPresets.DIAMOND);
        register(Material.IRON_INGOT, IconPresets.INGOT);
        register(Material.BREAD, IconPresets.BREAD);
        register(Material.WATER_BUCKET, IconPresets.BUCKET);
        register(Material.LAVA_BUCKET, IconPresets.BUCKET);
        register(Material.MILK_BUCKET, IconPresets.BUCKET);
    }

    private static void register(Material mat, byte[][] icon) {
        ICONS.put(mat, icon);
    }

    public static byte[][] get(Material mat) {
        return ICONS.get(mat);
    }
    public static byte[][] fallback(Material mat) {
        if (mat.name().endsWith("_INGOT")) return IconPresets.INGOT;
        if (mat.name().endsWith("_LOG")) return IconPresets.LOG;
        if (mat.name().contains("BUCKET")) return IconPresets.BUCKET;
        if (mat.isEdible()) return IconPresets.FOOD;
        if (mat.isBlock()) return IconPresets.BLOCK;
        return IconPresets.UNKNOWN;
    }

}
