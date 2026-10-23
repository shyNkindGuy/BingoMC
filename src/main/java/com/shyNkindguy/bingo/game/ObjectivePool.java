package com.shyNkindguy.bingo.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// Objetivos candidatos (ítem de Minecraft + nombre en español) y el sorteo de la cartilla.
// Clase pura: el ítem se guarda como nombre de Material y se resuelve recién en ObjectiveFactory.
// Hasta la tarea 1.3, que los pasa a YAML.
public final class ObjectivePool {

    public record Entry(String material, String displayName) {
    }

    public static final List<Entry> DEFAULT = List.of(
            new Entry("OAK_LOG", "Tronco de roble"),
            new Entry("COBBLESTONE", "Roca"),
            new Entry("IRON_INGOT", "Lingote de hierro"),
            new Entry("DIAMOND", "Diamante"),
            new Entry("BREAD", "Pan"),
            new Entry("COAL", "Carbón"),
            new Entry("GOLD_INGOT", "Lingote de oro"),
            new Entry("REDSTONE", "Polvo de redstone"),
            new Entry("LAPIS_LAZULI", "Lapislázuli"),
            new Entry("EMERALD", "Esmeralda"),
            new Entry("ACACIA_BOAT", "Bote de acacia"),
            new Entry("BARREL", "Barril"),
            new Entry("RED_DYE", "Tinte rojo"),
            new Entry("PURPLE_DYE", "Tinte morado"),
            new Entry("ACACIA_WOOD", "Madera de acacia"),
            new Entry("ARROW", "Flecha"),
            new Entry("BUCKET", "Balde"),
            new Entry("MILK_BUCKET", "Balde de leche"),
            new Entry("WATER_BUCKET", "Balde de agua"),
            new Entry("DIORITE", "Diorita"),
            new Entry("FLINT", "Pedernal"),
            new Entry("FLINT_AND_STEEL", "Mechero"),
            new Entry("DIAMOND_PICKAXE", "Pico de diamante"),
            new Entry("GOLDEN_APPLE", "Manzana dorada"),
            new Entry("COMPOSTER", "Compostador"),
            new Entry("LAVA_BUCKET", "Balde de lava"),
            new Entry("COPPER_INGOT", "Lingote de cobre"),
            new Entry("APPLE", "Manzana"),
            new Entry("COOKED_BEEF", "Filete"),
            new Entry("BOW", "Arco"),
            new Entry("SHIELD", "Escudo"),
            new Entry("WHITE_WOOL", "Lana blanca"),
            new Entry("BOOK", "Libro"),
            new Entry("SUGAR_CANE", "Caña de azúcar"),
            new Entry("PUMPKIN", "Calabaza"),
            new Entry("TORCH", "Antorcha"),
            new Entry("GLASS", "Vidrio"),
            new Entry("COMPASS", "Brújula"),
            new Entry("SHEARS", "Tijeras"),
            new Entry("STRING", "Hilo"),
            new Entry("BONE", "Hueso"),
            new Entry("GUNPOWDER", "Pólvora"),
            new Entry("OBSIDIAN", "Obsidiana")
    );

    private ObjectivePool() {
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
