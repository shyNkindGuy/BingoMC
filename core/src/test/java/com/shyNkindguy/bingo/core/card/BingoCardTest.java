package com.shyNkindguy.bingo.core.card;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BingoCardTest {

    private static List<BingoObjective> objetivos(int cantidad) {
        List<BingoObjective> objetivos = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            objetivos.add(new BingoObjective("T" + i, "Tarea " + i, "minecraft:item_" + i));
        }
        return objetivos;
    }

    @Test
    void elTamanoSaleDeLaCantidadDeObjetivos() {
        assertEquals(5, new BingoCard(objetivos(25)).getSize());
        assertEquals(3, new BingoCard(objetivos(9)).getSize());
    }

    @Test
    void unaCantidadQueNoEsCuadradoEsError() {
        assertThrows(IllegalArgumentException.class, () -> new BingoCard(objetivos(10)));
        assertThrows(IllegalArgumentException.class, () -> new BingoCard(objetivos(0)));
    }

    @Test
    void noModificaLaListaQueRecibe() {
        List<BingoObjective> lista = objetivos(9);
        List<BingoObjective> copia = new ArrayList<>(lista);
        new BingoCard(lista, new Random(3));
        assertEquals(copia, lista);
    }

    @Test
    void laMismaSemillaDaElMismoOrden() {
        List<BingoObjective> lista = objetivos(9);
        BingoCard a = new BingoCard(lista, new Random(5));
        BingoCard b = new BingoCard(lista, new Random(5));
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                assertSame(a.getGrid()[r][c].getObjective(), b.getGrid()[r][c].getObjective());
            }
        }
    }

    @Test
    void completarPorItemDevuelveElObjetivoUnaSolaVez() {
        BingoCard cartilla = new BingoCard(objetivos(9));

        BingoObjective completado = cartilla.completeByItem("minecraft:item_4");

        assertEquals("T4", completado.getId());
        assertNull(cartilla.completeByItem("minecraft:item_4"));
        assertEquals(1, cartilla.countCompleted());
    }

    @Test
    void unItemQueNoEstaEnLaCartillaNoCompletaNada() {
        BingoCard cartilla = new BingoCard(objetivos(9));
        assertNull(cartilla.completeByItem("minecraft:diamond"));
        assertEquals(0, cartilla.countCompleted());
    }

    @Test
    void cadaObjetivoApareceUnaVez() {
        BingoCard cartilla = new BingoCard(objetivos(25));
        List<String> ids = new ArrayList<>();
        Arrays.stream(cartilla.getGrid()).flatMap(Arrays::stream).forEach(c -> ids.add(c.getObjective().getId()));
        assertEquals(25, ids.stream().distinct().count());
        assertTrue(ids.contains("T0") && ids.contains("T24"));
    }
}
