package com.shyNkindguy.bingo.game;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeLimitWinnersTest {

    private static Map<String, Integer> puntos(Object... nombreYCasillas) {
        Map<String, Integer> puntos = new LinkedHashMap<>();
        for (int i = 0; i < nombreYCasillas.length; i += 2) {
            puntos.put((String) nombreYCasillas[i], (Integer) nombreYCasillas[i + 1]);
        }
        return puntos;
    }

    @Test
    void ganaElQueTieneMasCasillas() {
        assertEquals(List.of("Ana"), TimeLimitWinners.leaders(puntos("Ana", 7, "Beto", 3, "Caro", 5)));
    }

    @Test
    void empateDevuelveTodosLosLideresEnOrden() {
        assertEquals(List.of("Beto", "Caro"),
                TimeLimitWinners.leaders(puntos("Ana", 2, "Beto", 6, "Caro", 6)));
    }

    @Test
    void sinCasillasNoHayGanador() {
        assertTrue(TimeLimitWinners.leaders(puntos("Ana", 0, "Beto", 0)).isEmpty());
    }

    @Test
    void sinJugadoresNoHayGanador() {
        assertTrue(TimeLimitWinners.leaders(Map.of()).isEmpty());
    }
}
