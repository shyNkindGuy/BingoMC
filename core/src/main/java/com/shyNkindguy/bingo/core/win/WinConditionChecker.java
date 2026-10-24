package com.shyNkindguy.bingo.core.win;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;

// Línea completa en una cartilla de cualquier tamaño: fila, columna o una de las dos diagonales.
public class WinConditionChecker {
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
