package com.shyNkindguy.bingo.core.win;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;

// Línea completa en una cartilla de cualquier tamaño: fila, columna o una de las dos diagonales.
public class WinConditionChecker {
    // Cuántas líneas completas tiene (de 0 a 2N + 2).
    public static int countLines(BingoCard card) {
        BingoCell[][] g = card.getGrid();
        int n = card.getSize();
        int lines = 0;
        for (int i = 0; i < n; i++) {
            boolean row = true;
            boolean col = true;
            for (int j = 0; j < n; j++) {
                row &= g[i][j].isCompleted();
                col &= g[j][i].isCompleted();
            }
            if (row) lines++;
            if (col) lines++;
        }
        boolean diag1 = true;
        boolean diag2 = true;
        for (int i = 0; i < n; i++) {
            diag1 &= g[i][i].isCompleted();
            diag2 &= g[i][n - 1 - i].isCompleted();
        }
        if (diag1) lines++;
        if (diag2) lines++;
        return lines;
    }

    public static boolean hasBingo(BingoCard card){
        BingoCell[][] g = card.getGrid();
        int n = card.getSize();

        for (int r = 0; r < n; r++){
            boolean win = true;
            for (int c = 0; c < n; c++){
                if (!g[r][c].isCompleted()) win =false;
            }
            if(win) return true;
        }
        for (int c = 0; c < n; c++){
            boolean win = true;
            for (int r = 0; r < n; r++){
                if(!g[r][c].isCompleted()) win = false;
            }
            if (win) return true;
        }
        //diagonal
        boolean diag1=true;
        boolean diag2=true;
        for (int i = 0; i< n; i++){
            diag1 &= g[i][i].isCompleted();
            diag2 &= g[i][n - 1 - i].isCompleted();
        }
        return  diag1 || diag2;
    }
}
