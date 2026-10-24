package com.shyNkindguy.bingo.core.item;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;
import com.shyNkindguy.bingo.core.condition.AllOf;
import com.shyNkindguy.bingo.core.condition.AnyOf;
import com.shyNkindguy.bingo.core.condition.Condition;
import com.shyNkindguy.bingo.core.condition.InBiome;
import com.shyNkindguy.bingo.core.task.ConditionalTask;

import java.util.Set;
import java.util.TreeSet;

// Para la brújula del explorador: los biomas que piden las casillas pendientes (sin completar ni bloquear) de
// una cartilla, buscados también adentro de "todas" y "alguna".
public final class BiomeTargets {

    private BiomeTargets() {
    }

    public static Set<String> of(BingoCard card) {
        Set<String> biomes = new TreeSet<>();
        for (BingoCell[] row : card.getGrid()) {
            for (BingoCell cell : row) {
                if (cell.isCompleted() || cell.isLocked()) continue;
                if (cell.getTask() instanceof ConditionalTask conditional) collect(conditional.condition(), biomes);
            }
        }
        return biomes;
    }

    private static void collect(Condition condition, Set<String> biomes) {
        switch (condition) {
            case InBiome inBiome -> biomes.addAll(inBiome.biomes());
            case AllOf all -> all.conditions().forEach(inner -> collect(inner, biomes));
            case AnyOf any -> any.conditions().forEach(inner -> collect(inner, biomes));
            default -> {
            }
        }
    }
}
