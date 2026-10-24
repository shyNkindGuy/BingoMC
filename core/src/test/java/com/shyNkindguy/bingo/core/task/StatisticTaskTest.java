package com.shyNkindguy.bingo.core.task;

import com.shyNkindguy.bingo.core.event.StatisticReached;
import com.shyNkindguy.bingo.core.progress.TaskProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticTaskTest {

    private final StatisticTask saltos = new StatisticTask("statistic:jump", "Saltos", "minecraft:jump", null, 100);
    private final StatisticTask piedra =
            new StatisticTask("statistic:mine_block/stone", "Piedra picada", "minecraft:mine_block", "minecraft:stone", 64);

    @Test
    void avanzaHastaElValorDesdeQueEmpezo() {
        TaskProgress p = saltos.apply(new StatisticReached("minecraft:jump", null, 40), TaskProgress.start(100));
        assertEquals(40, p.current());
        p = saltos.apply(new StatisticReached("minecraft:jump", null, 120), p);
        assertTrue(p.isComplete());
    }

    @Test
    void laDeBloqueMiraElBloque() {
        TaskProgress inicio = TaskProgress.start(64);
        assertSame(inicio, piedra.apply(new StatisticReached("minecraft:mine_block", "minecraft:dirt", 64), inicio));
        assertSame(inicio, piedra.apply(new StatisticReached("minecraft:mine_block", null, 64), inicio));
        assertTrue(piedra.apply(new StatisticReached("minecraft:mine_block", "minecraft:stone", 64), inicio).isComplete());
    }

    @Test
    void otraEstadisticaNoLaMueve() {
        TaskProgress inicio = TaskProgress.start(100);
        assertSame(inicio, saltos.apply(new StatisticReached("minecraft:deaths", null, 100), inicio));
    }

    @Test
    void claveNombreEIcono() {
        assertEquals("minecraft:jump", saltos.key());
        assertEquals("minecraft:mine_block/minecraft:stone", piedra.key());
        assertEquals("100 × Saltos", saltos.displayName());
        assertNull(saltos.icon());
        assertEquals("minecraft:stone", piedra.icon());
    }
}
