package com.shyNkindguy.bingo.game;

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

    @Test
    void hayMasObjetivosQueCasillas() {
        assertTrue(ObjectivePool.DEFAULT.size() > ObjectiveFactory.CARD_CELLS);
    }

    @Test
    void ningunItemSeRepite() {
        Set<String> materiales = new HashSet<>();
        for (ObjectivePool.Entry entry : ObjectivePool.DEFAULT) {
            assertTrue(materiales.add(entry.material()), "Repetido: " + entry.material());
        }
    }

    @Test
    void cadaObjetivoTieneNombreUnico() {
        Set<String> nombres = new HashSet<>();
        for (ObjectivePool.Entry entry : ObjectivePool.DEFAULT) {
            assertFalse(entry.displayName().isBlank(), "Sin nombre: " + entry.material());
            assertTrue(nombres.add(entry.displayName()), "Nombre repetido: " + entry.displayName());
        }
    }

    @Test
    void losItemsUsanNombresDeMaterial() {
        for (ObjectivePool.Entry entry : ObjectivePool.DEFAULT) {
            assertTrue(entry.material().matches("[A-Z0-9_]+"), "No es un nombre de Material: " + entry.material());
        }
    }

    @Test
    void elSorteoDaVeinticincoDistintosDelPool() {
        List<ObjectivePool.Entry> cartilla = ObjectivePool.pick(ObjectivePool.DEFAULT, 25, new Random(7));
        assertEquals(25, cartilla.size());
        assertEquals(25, new HashSet<>(cartilla).size());
        assertTrue(ObjectivePool.DEFAULT.containsAll(cartilla));
    }

    @Test
    void laMismaSemillaDaLaMismaCartilla() {
        assertEquals(ObjectivePool.pick(ObjectivePool.DEFAULT, 25, new Random(42)),
                ObjectivePool.pick(ObjectivePool.DEFAULT, 25, new Random(42)));
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
