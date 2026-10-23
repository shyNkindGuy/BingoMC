package com.shyNkindguy.bingo.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WinConditionCheckerTest {

    private BingoCard card;

    @BeforeEach
    void cartillaVacia() {
        // Sin Material: la condición de victoria solo mira si cada casilla está completa.
        List<BingoObjective> objetivos = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            objetivos.add(new BingoObjective("T" + i, "Tarea " + i, null));
        }
        card = new BingoCard(objetivos);
    }

    private void completar(int fila, int columna) {
        card.getGrid()[fila][columna].setCompleted(true);
    }

    @Test
    void cartillaVaciaNoEsBingo() {
        assertFalse(WinConditionChecker.hasBingo(card));
    }

    @Test
    void filaCompletaEsBingo() {
        for (int c = 0; c < 5; c++) completar(2, c);
        assertTrue(WinConditionChecker.hasBingo(card));
    }

    @Test
    void columnaCompletaEsBingo() {
        for (int r = 0; r < 5; r++) completar(r, 4);
        assertTrue(WinConditionChecker.hasBingo(card));
    }

    @Test
    void diagonalPrincipalEsBingo() {
        for (int i = 0; i < 5; i++) completar(i, i);
        assertTrue(WinConditionChecker.hasBingo(card));
    }

    @Test
    void diagonalInversaEsBingo() {
        for (int i = 0; i < 5; i++) completar(i, 4 - i);
        assertTrue(WinConditionChecker.hasBingo(card));
    }

    @Test
    void veinteCasillasSinLineaNoEsBingo() {
        // Todo menos la diagonal principal: cada fila, columna y diagonal queda con un hueco
        // (el centro también es de la diagonal inversa).
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (r != c) completar(r, c);
            }
        }
        assertFalse(WinConditionChecker.hasBingo(card));
    }
}
