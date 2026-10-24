package com.shyNkindguy.bingo.core.progress;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;
import com.shyNkindguy.bingo.core.event.ItemObtained;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressTrackerTest {

    // Cartilla 3×3: troncos ×16, diamante ×1 y 7 tareas de relleno.
    private static BingoCard cartilla() {
        List<Task> tareas = new ArrayList<>();
        tareas.add(new ItemTask("PICKUP_OAK_LOG", "Tronco de roble", "minecraft:oak_log", 16));
        tareas.add(new ItemTask("PICKUP_DIAMOND", "Diamante", "minecraft:diamond", 1));
        for (int i = 0; i < 7; i++) tareas.add(new ItemTask("T" + i, "Tarea " + i, "minecraft:item_" + i, 1));
        return new BingoCard(tareas);
    }

    private static BingoCell celda(BingoCard cartilla, String id) {
        return Arrays.stream(cartilla.getGrid()).flatMap(Arrays::stream)
                .filter(c -> c.getTask().id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void unEventoQueNoLeImportaANadieNoCambiaNada() {
        assertSame(ProgressResult.NONE, ProgressTracker.apply(cartilla(), new ItemObtained("minecraft:dirt", 64)));
    }

    @Test
    void avanzarSinCompletarSeReportaComoAvance() {
        BingoCard cartilla = cartilla();

        ProgressResult r = ProgressTracker.apply(cartilla, new ItemObtained("minecraft:oak_log", 5));

        assertTrue(r.changed());
        assertEquals(1, r.advanced().size());
        assertTrue(r.completed().isEmpty());
        assertEquals(5, celda(cartilla, "PICKUP_OAK_LOG").getProgress().current());
    }

    @Test
    void completarSeReportaUnaSolaVez() {
        BingoCard cartilla = cartilla();

        ProgressResult primera = ProgressTracker.apply(cartilla, new ItemObtained("minecraft:diamond", 1));
        ProgressResult segunda = ProgressTracker.apply(cartilla, new ItemObtained("minecraft:diamond", 2));

        assertEquals("PICKUP_DIAMOND", primera.completed().get(0).id());
        assertFalse(segunda.changed());
        assertEquals(1, cartilla.countCompleted());
    }

    @Test
    void juntarDeAPocoCompletaAlLlegar() {
        BingoCard cartilla = cartilla();
        ProgressTracker.apply(cartilla, new ItemObtained("minecraft:oak_log", 10));
        ProgressTracker.apply(cartilla, new ItemObtained("minecraft:oak_log", 4)); // tiró algunos: no baja

        ProgressResult r = ProgressTracker.apply(cartilla, new ItemObtained("minecraft:oak_log", 16));

        assertEquals(1, r.completed().size());
        assertTrue(celda(cartilla, "PICKUP_OAK_LOG").isCompleted());
    }

    @Test
    void unaCasillaBloqueadaNoAvanza() {
        BingoCard cartilla = cartilla();
        celda(cartilla, "PICKUP_DIAMOND").lock("azul");

        assertSame(ProgressResult.NONE, ProgressTracker.apply(cartilla, new ItemObtained("minecraft:diamond", 1)));
        assertFalse(celda(cartilla, "PICKUP_DIAMOND").isCompleted());
    }
}
