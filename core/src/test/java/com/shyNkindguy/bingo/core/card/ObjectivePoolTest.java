package com.shyNkindguy.bingo.core.card;

import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectivePoolTest {

    private static final int CASILLAS = BingoCard.DEFAULT_SIZE * BingoCard.DEFAULT_SIZE;

    @Test
    void hayMasObjetivosQueCasillas() {
        assertTrue(ObjectivePool.DEFAULT.size() > CASILLAS);
    }

    @Test
    void ningunItemSeRepite() {
        Set<String> items = new HashSet<>();
        for (ObjectivePool.Entry entry : ObjectivePool.DEFAULT) {
            assertTrue(items.add(entry.item()), "Repetido: " + entry.item());
        }
    }

    @Test
    void cadaObjetivoTieneNombreUnico() {
        Set<String> nombres = new HashSet<>();
        for (ObjectivePool.Entry entry : ObjectivePool.DEFAULT) {
            assertFalse(entry.displayName().isBlank(), "Sin nombre: " + entry.item());
            assertTrue(nombres.add(entry.displayName()), "Nombre repetido: " + entry.displayName());
        }
    }

    @Test
    void losItemsSonClavesDeMinecraft() {
        for (ObjectivePool.Entry entry : ObjectivePool.DEFAULT) {
            assertTrue(entry.item().matches("minecraft:[a-z0-9_]+"), "No es una clave de ítem: " + entry.item());
        }
    }

    @Test
    void elIdDeLaTareaSaleDelItem() {
        ItemTask diamante = new ObjectivePool.Entry("minecraft:diamond", "Diamante").toTask();
        assertEquals("PICKUP_DIAMOND", diamante.id());
        assertEquals("Diamante", diamante.displayName());
        assertEquals("minecraft:diamond", diamante.item());
        assertEquals(1, diamante.amount());
    }

    @Test
    void laCantidadPasaALaTarea() {
        ItemTask troncos = new ObjectivePool.Entry("minecraft:oak_log", "Tronco de roble", 16).toTask();
        assertEquals(16, troncos.target());
        assertEquals("16 × Tronco de roble", troncos.displayName());
    }

    @Test
    void todasLasCantidadesSonPositivasYAlgunasPidenMasDeUno() {
        assertTrue(ObjectivePool.DEFAULT.stream().allMatch(entry -> entry.amount() >= 1));
        assertTrue(ObjectivePool.DEFAULT.stream().anyMatch(entry -> entry.amount() > 1));
    }

    @Test
    void elSorteoDaUnaCartillaDeObjetivosDistintos() {
        List<Task> cartilla = ObjectivePool.draw(ObjectivePool.DEFAULT, CASILLAS, new Random(7), item -> true);
        assertEquals(CASILLAS, cartilla.size());
        Set<String> items = new HashSet<>();
        for (Task tarea : cartilla) items.add(tarea.icon());
        assertEquals(CASILLAS, items.size());
    }

    @Test
    void elSorteoSalteaLosItemsQueNoExisten() {
        List<ObjectivePool.Entry> pool = List.of(
                new ObjectivePool.Entry("minecraft:diamond", "Diamante"),
                new ObjectivePool.Entry("minecraft:no_existe", "Fantasma"),
                new ObjectivePool.Entry("minecraft:bread", "Pan"));

        List<Task> cartilla = ObjectivePool.draw(pool, 2, new Random(1), item -> !item.endsWith("no_existe"));

        assertEquals(2, cartilla.size());
        for (Task tarea : cartilla) assertFalse(tarea.icon().endsWith("no_existe"));
    }

    @Test
    void sinSuficientesItemsValidosEsError() {
        assertThrows(IllegalArgumentException.class,
                () -> ObjectivePool.draw(ObjectivePool.DEFAULT, CASILLAS, new Random(), item -> false));
    }

    @Test
    void laMismaSemillaDaLaMismaCartilla() {
        assertEquals(ObjectivePool.pick(ObjectivePool.DEFAULT, CASILLAS, new Random(42)),
                ObjectivePool.pick(ObjectivePool.DEFAULT, CASILLAS, new Random(42)));
    }

    @Test
    void elSorteoNoModificaElPool() {
        List<String> pool = List.of("a", "b", "c", "d");
        ObjectivePool.pick(pool, 2, new Random(1));
        assertEquals(List.of("a", "b", "c", "d"), pool);
    }

    @Test
    void poolMasChicoQueLaCartillaEsError() {
        assertThrows(IllegalArgumentException.class, () -> ObjectivePool.pick(List.of("a"), 2, new Random()));
    }
}
