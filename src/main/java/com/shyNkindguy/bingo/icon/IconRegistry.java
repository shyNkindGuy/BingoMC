package com.shyNkindguy.bingo.icon;

import com.shyNkindguy.bingo.icon.presets.BlockIcons;
import com.shyNkindguy.bingo.icon.presets.FoodIcons;
import com.shyNkindguy.bingo.icon.presets.TooLIcons;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class IconRegistry {
    private static final Map<Material, IconDefinition> ICONS = new HashMap<>();
    private static final Map<IconCategory, IconDefinition> FALLBACKS = new HashMap<>();

    static {
        initializeFallbacks();
        registerIcons();
    }

    private static void initializeFallbacks() {
        for (IconCategory category : IconCategory.values()) {
            FALLBACKS.put(category, new IconDefinition(
                    "fallback_" + category.name().toLowerCase(), category.getFallback(), category));
        }
    }

    private static void registerIcons() {
        register(Material.DIAMOND, BlockIcons.DIAMOND_GEM, IconCategory.ORE);
        register(Material.BREAD, FoodIcons.BREAD, IconCategory.FOOD);
        register(Material.IRON_PICKAXE, TooLIcons.PICKAXE, IconCategory.TOOL);

        // Automáticos por patrón
        autoRegisterByPattern();
    }

    private static void autoRegisterByPattern() {
        for (Material mat : Material.values()) {
            if (mat.name().endsWith("_INGOT")) {
                registerIngot(mat);
            }
        }
    }

    private static void register(Material material, byte[][] pixels, IconCategory category) {
        ICONS.put(material, new IconDefinition(material.name().toLowerCase(), pixels, category));
    }

    private static void registerIngot(Material material) {
        register(material, BlockIcons.INGOT, IconCategory.ORE);
    }

    public static IconDefinition get(Material material) {
        return ICONS.get(material);
    }

    public static IconDefinition getFallback(Material material) {
        IconCategory category = categorize(material);
        return FALLBACKS.get(category);
    }

    private static IconCategory categorize(Material mat) {
        if (mat.name().contains("PICKAXE") || mat.name().contains("AXE"))
            return IconCategory.TOOL;
        if (mat.isEdible()) return IconCategory.FOOD;
        if (mat.name().contains("BUCKET")) return IconCategory.FLUID;
        if (mat.isBlock()) return IconCategory.BLOCK;
        return IconCategory.MISC;
    }
}
