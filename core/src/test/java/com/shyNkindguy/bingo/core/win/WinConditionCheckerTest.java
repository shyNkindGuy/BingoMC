package com.shyNkindguy.bingo.core.win;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.Task;

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
        card = new BingoCard(objetivos(25));
    }

    private static List<Task> objetivos(int cantidad) {
        List<Task> objetivos = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            objetivos.add(new ItemTask("T" + i, "Tarea " + i, "minecraft:item_" + i, 1));
        }
        return objetivos;
    }

    private void completar(int fila, int columna) {
        card.getGrid()[fila][columna].complete();
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

    @Test
    void enUnaCartillaDe3x3LaDiagonalInversaEsBingo() {
        card = new BingoCard(objetivos(9));
        for (int i = 0; i < 3; i++) completar(i, 2 - i);
        assertTrue(WinConditionChecker.hasBingo(card));
    }

    @Test
    void enUnaCartillaDe3x3DosDeTresNoEsBingo() {
        card = new BingoCard(objetivos(9));
        completar(0, 0);
        completar(1, 1);
        assertFalse(WinConditionChecker.hasBingo(card));
    }
}
