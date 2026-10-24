package com.shyNkindguy.bingo.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GameBossBarTest {

    @Test
    void elRelojSeLeeComoMinutosYSegundos() {
        assertEquals("0:05", GameBossBar.clock(5));
        assertEquals("12:34", GameBossBar.clock(754));
        assertEquals("1:02:03", GameBossBar.clock(3723));
    }
}
