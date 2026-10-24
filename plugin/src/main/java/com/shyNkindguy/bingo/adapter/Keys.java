package com.shyNkindguy.bingo.adapter;

import org.bukkit.Material;

import java.util.Locale;

// Traduce entre los Material de Paper y las claves de texto del núcleo ("minecraft:diamond").
public final class Keys {

    private Keys() {
    }

    public static String of(Material material) {
        return "minecraft:" + material.name().toLowerCase(Locale.ROOT);
    }

    // null si la clave no es un ítem de esta versión de Minecraft.
    public static Material toMaterial(String key) {
        return Material.matchMaterial(key);
    }
}
