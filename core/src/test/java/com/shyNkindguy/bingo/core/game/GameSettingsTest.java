package com.shyNkindguy.bingo.core.game;

import com.shyNkindguy.bingo.core.win.WinMode;
import org.junit.jupiter.api.Test;

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
        assertThrows(IllegalArgumentException.class, () -> new GameSettings(null, -1, 20, true, null, WinMode.DEFAULT));
        assertThrows(IllegalArgumentException.class, () -> new GameSettings(null, 61, 20, true, null, WinMode.DEFAULT));
        assertThrows(IllegalArgumentException.class, () -> new GameSettings(null, 10, 601, true, null, WinMode.DEFAULT));
        assertThrows(IllegalArgumentException.class, () -> new GameSettings(null, 10, 0, true, null, new WinMode.TimeOnly()));
    }
}
