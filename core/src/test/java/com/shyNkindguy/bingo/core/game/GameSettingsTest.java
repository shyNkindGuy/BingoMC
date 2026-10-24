package com.shyNkindguy.bingo.core.game;

import com.shyNkindguy.bingo.core.win.WinMode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameSettingsTest {

    @Test
    void porDefecto() {
        assertNull(GameSettings.DEFAULT.card());
        assertEquals(10, GameSettings.DEFAULT.countdownSeconds());
        assertEquals(20 * 60, GameSettings.DEFAULT.durationSeconds());
    }

    @Test
    void seCambiaLaCartillaYLaSemilla() {
        GameSettings rapida = GameSettings.DEFAULT.withCard("rapida").withSeed(42L);
        assertEquals("rapida", rapida.card());
        assertEquals(42L, rapida.seed());
        assertEquals(GameSettings.DEFAULT.durationMinutes(), rapida.durationMinutes());
    }

    @Test
    void fueraDeRangoEsError() {
        StartRules inicio = StartRules.DEFAULT;
        assertThrows(IllegalArgumentException.class, () -> new GameSettings(null, -1, 20, true, null, WinMode.DEFAULT, inicio));
        assertThrows(IllegalArgumentException.class, () -> new GameSettings(null, 61, 20, true, null, WinMode.DEFAULT, inicio));
        assertThrows(IllegalArgumentException.class, () -> new GameSettings(null, 10, 601, true, null, WinMode.DEFAULT, inicio));
        assertThrows(IllegalArgumentException.class, () -> new GameSettings(null, 10, 0, true, null, new WinMode.TimeOnly(), inicio));
    }

    @Test
    void reglasDeInicio() {
        assertThrows(IllegalArgumentException.class, () -> new StartRules(-1, 0, false, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new StartRules(0, 8, false, List.of()));
        // con borde, los equipos tienen que quedar adentro
        assertThrows(IllegalArgumentException.class, () -> new StartRules(500, 1000, false, List.of()));
        assertEquals(1, new StartRules(400, 1000, true, List.of(new StartRules.KitItem("minecraft:bread", 8))).kit().size());
    }
}
