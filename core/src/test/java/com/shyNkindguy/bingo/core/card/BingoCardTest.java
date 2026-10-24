package com.shyNkindguy.bingo.core.card;

import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BingoCardTest {

    private static List<Task> tareas(int cantidad) {
        List<Task> tareas = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            tareas.add(new ItemTask("T" + i, "Tarea " + i, "minecraft:item_" + i, 1));
        }
        return tareas;
    }

    @Test
    void elTamanoSaleDeLaCantidadDeTareas() {
        assertEquals(5, new BingoCard(tareas(25)).getSize());
        assertEquals(3, new BingoCard(tareas(9)).getSize());
    }

    @Test
    void unaCantidadQueNoEsCuadradoEsError() {
        assertThrows(IllegalArgumentException.class, () -> new BingoCard(tareas(10)));
        assertThrows(IllegalArgumentException.class, () -> new BingoCard(tareas(0)));
    }

    @Test
    void noModificaLaListaQueRecibe() {
        List<Task> lista = tareas(9);
        List<Task> copia = new ArrayList<>(lista);
        new BingoCard(lista, new Random(3));
        assertEquals(copia, lista);
    }

    @Test
    void laMismaSemillaDaElMismoOrden() {
        List<Task> lista = tareas(9);
        BingoCard a = new BingoCard(lista, new Random(5));
        BingoCard b = new BingoCard(lista, new Random(5));
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                assertSame(a.getGrid()[r][c].getTask(), b.getGrid()[r][c].getTask());
            }
        }
    }

    @Test
    void lasCasillasEmpiezanSinAvance() {
        BingoCard cartilla = new BingoCard(tareas(9));
        Arrays.stream(cartilla.getGrid()).flatMap(Arrays::stream).forEach(celda -> {
            assertFalse(celda.isCompleted());
            assertEquals(0, celda.getProgress().current());
        });
        assertEquals(0, cartilla.countCompleted());
    }

    @Test
    void completarUnaCasillaCuenta() {
        BingoCard cartilla = new BingoCard(tareas(9));
        cartilla.getGrid()[1][2].complete();
        assertTrue(cartilla.getGrid()[1][2].isCompleted());
        assertEquals(1, cartilla.countCompleted());
    }

    @Test
    void cadaTareaApareceUnaVez() {
        BingoCard cartilla = new BingoCard(tareas(25));
        List<String> ids = new ArrayList<>();
        Arrays.stream(cartilla.getGrid()).flatMap(Arrays::stream).forEach(c -> ids.add(c.getTask().id()));
        assertEquals(25, ids.stream().distinct().count());
        assertTrue(ids.contains("T0") && ids.contains("T24"));
    }

    @Test
    void unaCasillaAnotaCuandoSeCompletoUnaSolaVez() {
        BingoCell celda = new BingoCell(new ItemTask("t", "T", "minecraft:t", 1));
        celda.markCompletedAt(10);
        assertEquals(-1, celda.completedAt());
        celda.complete();
        celda.markCompleted(20, "Ana");
        celda.markCompleted(30, "Beto");
        assertEquals(20, celda.completedAt());
        assertEquals("Ana", celda.completedBy());
    }

    @Test
    void enLockoutUnaCasillaPendienteSeBloqueaYUnaCompletaNo() {
        BingoCell pendiente = new BingoCell(new ItemTask("t", "T", "minecraft:t", 1));
        pendiente.lock("rojo");
        assertTrue(pendiente.isLocked());
        assertEquals("rojo", pendiente.lockedBy());

        BingoCell completa = new BingoCell(new ItemTask("t", "T", "minecraft:t", 1));
        completa.complete();
        completa.lock("rojo");
        assertFalse(completa.isLocked());
    }
}
