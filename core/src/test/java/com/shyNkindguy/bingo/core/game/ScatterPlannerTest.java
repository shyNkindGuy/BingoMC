package com.shyNkindguy.bingo.core.game;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScatterPlannerTest {

    @Test
    void todosQuedanEnElCirculoYALaMismaDistancia() {
        List<ScatterPlanner.Offset> puntos = ScatterPlanner.positions(4, 500, new Random(3));
        assertEquals(4, puntos.size());
        for (ScatterPlanner.Offset punto : puntos) {
            assertEquals(500, Math.hypot(punto.x(), punto.z()), 1.0);
        }
        // 4 equipos: cuadrado de lado 500·√2
        for (int i = 0; i < 4; i++) {
            assertEquals(500 * Math.sqrt(2), puntos.get(i).distanceTo(puntos.get((i + 1) % 4)), 2.0);
        }
    }

    @Test
    void conDosQuedanEnfrentados() {
        List<ScatterPlanner.Offset> puntos = ScatterPlanner.positions(2, 300, new Random(1));
        assertEquals(600, puntos.get(0).distanceTo(puntos.get(1)), 2.0);
    }

    @Test
    void elCirculoGiraConLaSemilla() {
        assertEquals(ScatterPlanner.positions(3, 200, new Random(7)), ScatterPlanner.positions(3, 200, new Random(7)));
        assertNotEquals(ScatterPlanner.positions(3, 200, new Random(7)), ScatterPlanner.positions(3, 200, new Random(8)));
    }

    @Test
    void unEquipoQueLlegaTardeTambienQuedaEnElCirculo() {
        ScatterPlanner.Offset punto = ScatterPlanner.extra(250, new Random(5));
        assertTrue(Math.abs(Math.hypot(punto.x(), punto.z()) - 250) <= 1.0);
    }
}
