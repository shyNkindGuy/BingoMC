package com.shyNkindguy.bingo.core.stats;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlaceholdersTest {
    private static final IntFunction<String> RELOJ = seconds -> (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    private static final PlayerStats ANA = new PlayerStats("Ana", 4, 3, 30, 754, Set.of("veterano", "campeon"));
    private static final Map<String, PlayerStats> TODOS = Map.of("id-ana", ANA, "id-beto", new PlayerStats("Beto", 2, 1, 8, 400));

    private static String valor(String clave, PlayerStats mio) {
        return Placeholders.resolve(clave, mio, TODOS, RELOJ, "-");
    }

    @Test
    void losDelJugador() {
        assertEquals("4", valor("partidas", ANA));
        assertEquals("3", valor("victorias", ANA));
        assertEquals("30", valor("casillas", ANA));
        assertEquals("75", valor("porcentaje", ANA));
        assertEquals("12:34", valor("mejor_tiempo", ANA));
        assertEquals("2", valor("logros", ANA));
        assertEquals("0", valor("partidas", null));
        assertEquals("-", valor("mejor_tiempo", null));
    }

    @Test
    void losDelTop() {
        assertEquals("Ana", valor("top_victorias_1", null));
        assertEquals("3", valor("top_victorias_1_valor", null));
        assertEquals("Beto", valor("top_tiempo_1", null));
        assertEquals("6:40", valor("top_tiempo_1_valor", null));
        assertEquals("-", valor("top_partidas_3", null));
    }

    @Test
    void loQueNoExisteQuedaComoEsta() {
        assertNull(valor("muertes", ANA));
        assertNull(valor("top_kills_1", ANA));
        assertNull(valor("top_victorias_x", ANA));
        assertNull(valor("top_victorias_0", ANA));
        assertNull(valor("top_victorias_1_nombre", ANA));
    }
}
