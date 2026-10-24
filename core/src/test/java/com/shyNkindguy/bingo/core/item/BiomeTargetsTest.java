package com.shyNkindguy.bingo.core.item;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;
import com.shyNkindguy.bingo.core.condition.AllOf;
import com.shyNkindguy.bingo.core.condition.AnyOf;
import com.shyNkindguy.bingo.core.condition.InBiome;
import com.shyNkindguy.bingo.core.condition.TimeOfDay;
import com.shyNkindguy.bingo.core.task.ConditionalTask;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BiomeTargetsTest {

    private static Task item(String id) {
        return new ItemTask(id, id, "minecraft:" + id, 1);
    }

    @Test
    void juntaLosBiomasDeLasPendientesTambienAdentroDeTodasYAlguna() {
        Task desierto = new ConditionalTask("arena", item("sand"), new InBiome(Set.of("minecraft:desert")));
        Task mezcla = new ConditionalTask("cactus", item("cactus"), new AllOf(List.of(new TimeOfDay(true),
                new AnyOf(List.of(new InBiome(Set.of("minecraft:badlands")), new TimeOfDay(false))))));
        Task hecha = new ConditionalTask("hielo", item("ice"), new InBiome(Set.of("minecraft:snowy_plains")));
        BingoCard card = new BingoCard(List.of(desierto, mezcla, hecha, item("dirt")), new Random(1));
        for (BingoCell[] row : card.getGrid()) {
            for (BingoCell cell : row) {
                if (cell.getTask().id().equals("hielo")) cell.complete();
            }
        }

        assertEquals(Set.of("minecraft:badlands", "minecraft:desert"), BiomeTargets.of(card));
    }
}
