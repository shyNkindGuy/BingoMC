package com.shyNkindguy.bingo.core.win;

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
    void elEmpateLoGanaQuienLlegoPrimero() {
        List<TimeLimitWinners.Score> puntajes = List.of(
                new TimeLimitWinners.Score("Rojo", 6, 900),
                new TimeLimitWinners.Score("Azul", 6, 640),
                new TimeLimitWinners.Score("Verde", 4, 100));
        assertEquals(List.of("Azul"), TimeLimitWinners.winners(puntajes));
    }

    @Test
    void siLlegaronEnElMismoSegundoEmpatan() {
        List<TimeLimitWinners.Score> puntajes = List.of(
                new TimeLimitWinners.Score("Rojo", 3, 50),
                new TimeLimitWinners.Score("Azul", 3, 50));
        assertEquals(List.of("Rojo", "Azul"), TimeLimitWinners.winners(puntajes));
        assertTrue(TimeLimitWinners.winners(List.of(new TimeLimitWinners.Score("Rojo", 0, -1))).isEmpty());
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
