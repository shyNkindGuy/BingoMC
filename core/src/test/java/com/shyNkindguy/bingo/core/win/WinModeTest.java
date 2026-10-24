package com.shyNkindguy.bingo.core.win;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WinModeTest {

    private static BingoCard cartilla(int lado) {
        List<Task> tareas = new ArrayList<>();
        for (int i = 0; i < lado * lado; i++) tareas.add(new ItemTask("t" + i, "T" + i, "minecraft:t" + i, 1));
        return new BingoCard(tareas, new Random(0));
    }

    private static void completar(BingoCard cartilla, int fila, int columna) {
        cartilla.getGrid()[fila][columna].complete();
    }

    @Test
    void lineasCuentaFilasColumnasYDiagonales() {
        BingoCard c = cartilla(3);
        for (int i = 0; i < 3; i++) completar(c, 0, i);       // fila 0
        assertEquals(1, WinConditionChecker.countLines(c));
        assertTrue(new WinMode.Lines(1).wins(c));
        assertFalse(new WinMode.Lines(2).wins(c));

        for (int i = 1; i < 3; i++) completar(c, i, i);       // diagonal (0,0 ya estaba)
        assertEquals(2, WinConditionChecker.countLines(c));
        assertTrue(new WinMode.Lines(2).wins(c));
    }

    @Test
    void cartaLlenaPideTodas() {
        BingoCard c = cartilla(3);
        for (int f = 0; f < 3; f++) for (int col = 0; col < 3; col++) if (f + col < 4) completar(c, f, col);
        assertFalse(new WinMode.Blackout().wins(c));
        completar(c, 2, 2);
        assertTrue(new WinMode.Blackout().wins(c));
        assertEquals(8, WinConditionChecker.countLines(c));
    }

    @Test
    void casillasCuentaEnCualquierLugar() {
        BingoCard c = cartilla(3);
        completar(c, 0, 0);
        completar(c, 1, 2);
        completar(c, 2, 1);
        assertTrue(new WinMode.CompleteX(3).wins(c));
        assertFalse(new WinMode.CompleteX(4).wins(c));
        assertFalse(new WinMode.Lines(1).wins(c));
    }

    @Test
    void lockoutPideMasDeLaMitad() {
        BingoCard c = cartilla(3);
        for (int i = 0; i < 4; i++) completar(c, i / 3, i % 3);
        assertFalse(new WinMode.Lockout().wins(c));
        completar(c, 1, 1);
        assertTrue(new WinMode.Lockout().wins(c));
        assertTrue(new WinMode.Lockout().lockout());
        assertFalse(new WinMode.Lines(1).lockout());
    }

    @Test
    void porTiempoNadieGanaAntes() {
        BingoCard c = cartilla(3);
        for (int f = 0; f < 3; f++) for (int col = 0; col < 3; col++) completar(c, f, col);
        assertFalse(new WinMode.TimeOnly().wins(c));
    }

    @Test
    void seLeenEnEspanolConOSinTildes() {
        assertEquals(new WinMode.Lines(1), WinMode.parse("línea"));
        assertEquals(new WinMode.Lines(1), WinMode.parse("Linea"));
        assertEquals(new WinMode.Lines(3), WinMode.parse("3 lineas"));
        assertEquals(new WinMode.Lines(2), WinMode.parse("2 líneas"));
        assertEquals(new WinMode.Blackout(), WinMode.parse("carta llena"));
        assertEquals(new WinMode.Blackout(), WinMode.parse("carta_llena"));
        assertEquals(new WinMode.CompleteX(12), WinMode.parse("12 casillas"));
        assertEquals(new WinMode.Lockout(), WinMode.parse("LOCKOUT"));
        assertEquals(new WinMode.TimeOnly(), WinMode.parse("tiempo"));
        assertThrows(IllegalArgumentException.class, () -> WinMode.parse("volar"));
        assertThrows(IllegalArgumentException.class, () -> WinMode.parse("0 lineas"));
    }

    @Test
    void descripcionYProblemasConElTamano() {
        assertEquals("1 línea", new WinMode.Lines(1).description());
        assertEquals("2 líneas", new WinMode.Lines(2).description());
        assertEquals("12 casillas", new WinMode.CompleteX(12).description());
        assertNull(new WinMode.Lines(12).problemWith(5));
        assertEquals("una cartilla de 5×5 tiene 12 líneas", new WinMode.Lines(13).problemWith(5));
        assertEquals("una cartilla de 3×3 tiene 9 casillas", new WinMode.CompleteX(10).problemWith(3));
        assertNull(new WinMode.Lockout().problemWith(3));
    }
}
