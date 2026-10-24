package com.shyNkindguy.bingo.adapter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Traduce entre los tipos de Paper (Material, EntityType, Statistic, Advancement) y las claves de texto
// del núcleo ("minecraft:diamond", "minecraft:zombie", "minecraft:jump", "minecraft:story/mine_diamond").
public final class Keys {

    private static final Map<String, EntityType> ENTITIES = new HashMap<>();
    private static final Map<String, Statistic> STATISTICS = new HashMap<>();

    static {
        for (EntityType type : EntityType.values()) {
            String key = of(type);
            if (key != null) ENTITIES.put(key, type);
        }
        for (Statistic statistic : Statistic.values()) {
            STATISTICS.put(statistic.getKey().toString(), statistic);
        }
    }

    private Keys() {
    }

    public static String of(Material material) {
        return "minecraft:" + material.name().toLowerCase(Locale.ROOT);
    }

    // null si la clave no es un ítem de esta versión de Minecraft.
    public static Material toMaterial(String key) {
        return Material.matchMaterial(key);
    }

    // null para EntityType.UNKNOWN, que no tiene clave.
    public static String of(EntityType type) {
        if (type == null || type == EntityType.UNKNOWN) return null;
        try {
            return type.getKey().toString();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static EntityType toEntityType(String key) {
        return ENTITIES.get(key);
    }

    public static Statistic toStatistic(String key) {
        return STATISTICS.get(key);
    }

    // Los logros los arma el servidor (y los datapacks): solo se pueden buscar con el servidor andando.
    public static Advancement toAdvancement(String key) {
        NamespacedKey namespaced = NamespacedKey.fromString(key);
        return namespaced == null ? null : Bukkit.getAdvancement(namespaced);
    }
}
