package com.shyNkindguy.bingo.core.task;

import com.shyNkindguy.bingo.core.event.ItemObtained;
import com.shyNkindguy.bingo.core.progress.TaskProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemTaskTest {

    private final ItemTask troncos = new ItemTask("PICKUP_OAK_LOG", "Tronco de roble", "minecraft:oak_log", 16);

    @Test
    void elNombreLlevaLaCantidad() {
        assertEquals("16 × Tronco de roble", troncos.displayName());
        assertEquals("Diamante", new ItemTask("PICKUP_DIAMOND", "Diamante", "minecraft:diamond", 1).displayName());
    }

    @Test
    void elIconoEsElItem() {
        assertEquals("minecraft:oak_log", troncos.icon());
    }

    @Test
    void avanzaConElItemQuePide() {
        TaskProgress p = troncos.apply(new ItemObtained("minecraft:oak_log", 7), TaskProgress.start(16));
        assertEquals(7, p.current());
    }

    @Test
    void otroItemNoLaMueve() {
        TaskProgress inicio = TaskProgress.start(16);
        assertSame(inicio, troncos.apply(new ItemObtained("minecraft:birch_log", 64), inicio));
    }

    @Test
    void juntarLaCantidadLaCompleta() {
        TaskProgress p = troncos.apply(new ItemObtained("minecraft:oak_log", 16), TaskProgress.start(16));
        assertTrue(p.isComplete());
    }

    @Test
    void cantidadCeroEsError() {
        assertThrows(IllegalArgumentException.class, () -> new ItemTask("X", "X", "minecraft:x", 0));
    }
}
