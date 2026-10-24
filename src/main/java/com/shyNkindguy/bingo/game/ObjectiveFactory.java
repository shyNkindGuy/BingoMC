package com.shyNkindguy.bingo.game;

import org.bukkit.Material;
import java.util.*;

public class ObjectiveFactory {
    public static final int CARD_CELLS = 25;
    private static final Random RANDOM = new Random();

    // Los objetivos de una partida: 25 al azar del pool, los mismos para todos los jugadores.
    public static List<BingoObjective> createObjectives() {
        List<BingoObjective> candidates = new ArrayList<>();
        for (ObjectivePool.Entry entry : ObjectivePool.DEFAULT) {
            Material material = Material.matchMaterial(entry.material());
            if (material == null) continue; // ObjectivePoolMaterialsTest lo evita en el CI

            candidates.add(new BingoObjective("PICKUP_" + entry.material(), entry.displayName(), material));
        }
        return ObjectivePool.pick(candidates, CARD_CELLS, RANDOM);
    }
}
