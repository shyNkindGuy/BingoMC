package com.shyNkindguy.bingo.core.game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChaosAndTimersTest {

    @Test
    void elCaosRepartePorMinutoYUnaSolaVezPorSegundo() {
        ChaosRules every2 = new ChaosRules(2);
        assertFalse(every2.due(0, -1));
        assertFalse(every2.due(119, -1));
        assertTrue(every2.due(120, -1));
        assertFalse(every2.due(120, 120));
        assertTrue(every2.due(240, 120));
        assertFalse(ChaosRules.DEFAULT.due(120, -1));
        assertThrows(IllegalArgumentException.class, () -> new ChaosRules(61));
    }

    @Test
    void losTiemposSeSumanYSeAcaban() {
        Timers<String> shields = new Timers<>();
        assertFalse(shields.active("ana", 0));
        shields.add("ana", 10, 60);
        assertEquals(60, shields.remaining("ana", 10));
        assertEquals(30, shields.remaining("ana", 40));
        shields.add("ana", 40, 60);
        assertEquals(90, shields.remaining("ana", 40));
        assertFalse(shields.active("ana", 130));
        shields.add("ana", 200, 5);
        assertEquals(5, shields.remaining("ana", 200));
        shields.clear();
        assertEquals(0, shields.remaining("ana", 200));
    }
}
