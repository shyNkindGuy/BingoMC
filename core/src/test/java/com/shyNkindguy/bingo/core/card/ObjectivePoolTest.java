package com.shyNkindguy.bingo.core.card;

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
    void elIdDelObjetivoSaleDelItem() {
        BingoObjective diamante = new ObjectivePool.Entry("minecraft:diamond", "Diamante").toObjective();
        assertEquals("PICKUP_DIAMOND", diamante.getId());
        assertEquals("Diamante", diamante.getDisplayName());
        assertEquals("minecraft:diamond", diamante.getItem());
    }

    @Test
    void elSorteoDaUnaCartillaDeObjetivosDistintos() {
        List<BingoObjective> cartilla = ObjectivePool.draw(ObjectivePool.DEFAULT, CASILLAS, new Random(7), item -> true);
        assertEquals(CASILLAS, cartilla.size());
        Set<String> items = new HashSet<>();
        for (BingoObjective objetivo : cartilla) items.add(objetivo.getItem());
        assertEquals(CASILLAS, items.size());
    }

    @Test
    void elSorteoSalteaLosItemsQueNoExisten() {
        List<ObjectivePool.Entry> pool = List.of(
                new ObjectivePool.Entry("minecraft:diamond", "Diamante"),
                new ObjectivePool.Entry("minecraft:no_existe", "Fantasma"),
                new ObjectivePool.Entry("minecraft:bread", "Pan"));

        List<BingoObjective> cartilla = ObjectivePool.draw(pool, 2, new Random(1), item -> !item.endsWith("no_existe"));

        assertEquals(2, cartilla.size());
        for (BingoObjective objetivo : cartilla) assertFalse(objetivo.getItem().endsWith("no_existe"));
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
