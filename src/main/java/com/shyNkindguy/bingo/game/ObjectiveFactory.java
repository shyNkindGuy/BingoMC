package com.shyNkindguy.bingo.game;

import org.bukkit.Material;
import java.util.*;

public class ObjectiveFactory {

    public static List<BingoObjective> createObjectives() {
        List<BingoObjective> list = new ArrayList<>();

        list.add(new BingoObjective("PICKUP_OAK_LOG", "Madera de Roble", Material.OAK_LOG));
        list.add(new BingoObjective("PICKUP_COBBLESTONE", "Piedra", Material.COBBLESTONE));
        list.add(new BingoObjective("PICKUP_IRON_INGOT", "Lingote de Hierro", Material.IRON_INGOT));
        list.add(new BingoObjective("PICKUP_DIAMOND", "Diamante", Material.POLISHED_DIORITE_SLAB));
        list.add(new BingoObjective("PICKUP_BREAD", "Pan", Material.BREAD));
        list.add(new BingoObjective("PICKUP_COAL", "Carbón", Material.COAL));
        list.add(new BingoObjective("PICKUP_GOLD_INGOT", "Lingote de Oro", Material.GOLD_INGOT));
        list.add(new BingoObjective("PICKUP_REDSTONE", "Redstone", Material.REDSTONE));
        list.add(new BingoObjective("PICKUP_LAPIS", "Lapislázuli", Material.LAPIS_LAZULI));
        list.add(new BingoObjective("PICKUP_EMERALD", "Esmeralda", Material.EMERALD));

        list.add(new BingoObjective("PICKUP_ACACIA_BOAT", "Barca de Acacia", Material.WHITE_WOOL));
        list.add(new BingoObjective("PICKUP_ACACIA_SIGN", "Barril ps", Material.BARREL));
        list.add(new BingoObjective("PICKUP_ACACIA_LEAVES", "Tinte rojo", Material.RED_DYE));
        list.add(new BingoObjective("PICKUP_ACACIA_TRAPDOOR", "Tinte del morao :v", Material.PURPLE_DYE));
        list.add(new BingoObjective("PICKUP_ACACIA_WOOD", "Madera de Acacia", Material.ACACIA_WOOD));

        list.add(new BingoObjective("PICKUP_ARROW", "Flecha", Material.ARROW));
        list.add(new BingoObjective("PICKUP_BUCKET", "Balde", Material.BUCKET));
        list.add(new BingoObjective("PICKUP_MILK_BUCKET", "Balde de Leche", Material.MILK_BUCKET));
        list.add(new BingoObjective("PICKUP_WATER_BUCKET", "Balde de Agua", Material.WATER_BUCKET));
        list.add(new BingoObjective("PICKUP_DIORITE", "Diorita", Material.DIORITE));

        list.add(new BingoObjective("PICKUP_FLINT", "Pedernal", Material.FLINT));
        list.add(new BingoObjective("PICKUP_FLINT_STEEL", "Mechero", Material.FLINT_AND_STEEL));
        list.add(new BingoObjective("PICKUP_DIAMOND_PICKAXE", "Pico de Diamante", Material.DIAMOND_PICKAXE));
        list.add(new BingoObjective("PICKUP_GOLDEN_APPLE", "Manzana Dorada", Material.GOLDEN_APPLE));
        list.add(new BingoObjective("PICKUP_GOLDEN_CHESTPLATE", "Composter de aldeano idk", Material.COMPOSTER));

        Collections.shuffle(list);
        return list;
    }
}
