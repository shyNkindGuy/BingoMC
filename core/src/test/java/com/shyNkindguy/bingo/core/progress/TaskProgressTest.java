package com.shyNkindguy.bingo.core.progress;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskProgressTest {

    @Test
    void empiezaEnCero() {
        TaskProgress p = TaskProgress.start(16);
        assertEquals(0, p.current());
        assertEquals(16, p.target());
        assertFalse(p.isComplete());
    }

    @Test
    void avanzaHastaLoQueSeTiene() {
        assertEquals(5, TaskProgress.start(16).reach(5).current());
    }

    @Test
    void noBajaSiDespuesSeTieneMenos() {
        assertEquals(10, TaskProgress.start(16).reach(10).reach(3).current());
    }

    @Test
    void noPasaDelObjetivoYSeCompleta() {
        TaskProgress p = TaskProgress.start(16).reach(40);
        assertEquals(16, p.current());
        assertTrue(p.isComplete());
    }

    @Test
    void sinCambioDevuelveElMismoObjeto() {
        TaskProgress p = TaskProgress.start(4).reach(2);
        assertSame(p, p.reach(1));
    }

    @Test
    void laBarraEsProporcionalAlAvance() {
        assertEquals(0, TaskProgress.start(16).scaledTo(16));
        assertEquals(8, new TaskProgress(8, 16).scaledTo(16));
        assertEquals(4, new TaskProgress(1, 4).scaledTo(16));
        assertEquals(16, TaskProgress.done(3).scaledTo(16));
    }

    @Test
    void valoresImposiblesSonError() {
        assertThrows(IllegalArgumentException.class, () -> new TaskProgress(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new TaskProgress(5, 4));
        assertThrows(IllegalArgumentException.class, () -> new TaskProgress(-1, 4));
    }
}
