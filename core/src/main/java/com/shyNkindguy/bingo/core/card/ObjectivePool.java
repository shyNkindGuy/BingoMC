package com.shyNkindguy.bingo.core.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Predicate;

// Objetivos candidatos (clave del ítem de Minecraft + nombre en español) y el sorteo de la cartilla.
// Hasta la tarea 1.3, que los pasa a YAML.
public final class ObjectivePool {

    public record Entry(String item, String displayName) {

        // El id sale del ítem ("minecraft:oak_log" → "PICKUP_OAK_LOG"): no puede quedar desalineado.
        public BingoObjective toObjective() {
            String path = item.substring(item.indexOf(':') + 1);
            return new BingoObjective("PICKUP_" + path.toUpperCase(Locale.ROOT), displayName, item);
        }
    }

    public static final List<Entry> DEFAULT = List.of(
            new Entry("minecraft:oak_log", "Tronco de roble"),
            new Entry("minecraft:cobblestone", "Roca"),
            new Entry("minecraft:iron_ingot", "Lingote de hierro"),
            new Entry("minecraft:diamond", "Diamante"),
            new Entry("minecraft:bread", "Pan"),
            new Entry("minecraft:coal", "Carbón"),
            new Entry("minecraft:gold_ingot", "Lingote de oro"),
            new Entry("minecraft:redstone", "Polvo de redstone"),
            new Entry("minecraft:lapis_lazuli", "Lapislázuli"),
            new Entry("minecraft:emerald", "Esmeralda"),
            new Entry("minecraft:acacia_boat", "Bote de acacia"),
            new Entry("minecraft:barrel", "Barril"),
            new Entry("minecraft:red_dye", "Tinte rojo"),
            new Entry("minecraft:purple_dye", "Tinte morado"),
            new Entry("minecraft:acacia_wood", "Madera de acacia"),
            new Entry("minecraft:arrow", "Flecha"),
            new Entry("minecraft:bucket", "Balde"),
            new Entry("minecraft:milk_bucket", "Balde de leche"),
            new Entry("minecraft:water_bucket", "Balde de agua"),
            new Entry("minecraft:diorite", "Diorita"),
            new Entry("minecraft:flint", "Pedernal"),
            new Entry("minecraft:flint_and_steel", "Mechero"),
            new Entry("minecraft:diamond_pickaxe", "Pico de diamante"),
            new Entry("minecraft:golden_apple", "Manzana dorada"),
            new Entry("minecraft:composter", "Compostador"),
            new Entry("minecraft:lava_bucket", "Balde de lava"),
            new Entry("minecraft:copper_ingot", "Lingote de cobre"),
            new Entry("minecraft:apple", "Manzana"),
            new Entry("minecraft:cooked_beef", "Filete"),
            new Entry("minecraft:bow", "Arco"),
            new Entry("minecraft:shield", "Escudo"),
            new Entry("minecraft:white_wool", "Lana blanca"),
            new Entry("minecraft:book", "Libro"),
            new Entry("minecraft:sugar_cane", "Caña de azúcar"),
            new Entry("minecraft:pumpkin", "Calabaza"),
            new Entry("minecraft:torch", "Antorcha"),
            new Entry("minecraft:glass", "Vidrio"),
            new Entry("minecraft:compass", "Brújula"),
            new Entry("minecraft:shears", "Tijeras"),
            new Entry("minecraft:string", "Hilo"),
            new Entry("minecraft:bone", "Hueso"),
            new Entry("minecraft:gunpowder", "Pólvora"),
            new Entry("minecraft:obsidian", "Obsidiana")
    );

    private ObjectivePool() {
    }

    // Los objetivos de una cartilla: `count` del pool al azar, salteando los ítems que `itemExists` no
    // reconoce (el plugin le pasa "existe en esta versión de Minecraft").
    public static List<BingoObjective> draw(List<Entry> pool, int count, Random random, Predicate<String> itemExists) {
        List<BingoObjective> candidates = new ArrayList<>();
        for (Entry entry : pool) {
            if (itemExists.test(entry.item())) candidates.add(entry.toObjective());
        }
        return pick(candidates, count, random);
    }

    // `count` elementos distintos del pool, en orden al azar. No modifica el pool.
    public static <T> List<T> pick(List<T> pool, int count, Random random) {
        if (pool.size() < count) {
            throw new IllegalArgumentException("El pool tiene " + pool.size() + " objetivos y se piden " + count);
        }
        List<T> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, random);
        return new ArrayList<>(shuffled.subList(0, count));
    }
}
