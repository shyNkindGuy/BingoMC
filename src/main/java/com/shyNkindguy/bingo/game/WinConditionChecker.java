package com.shyNkindguy.bingo.game;

public class WinConditionChecker {
    public static boolean hasBingo(BingoCard card){
        BingoCell[][] g = card.getGrid();

        for (int r = 0; r < 5; r++){
            boolean win = true;
            for (int c = 0; c < 5; c++){
                if (!g[r][c].isCompleted()) win =false;
            }
            if(win) return true;
        }
        for (int c = 0; c < 5; c++){
            boolean win = true;
            for (int r = 0; r < 5; r++){
                if(!g[r][c].isCompleted()) win = false;
            }
            if (win) return true;
        }
        //diagonal
        boolean diag1=true;
        boolean diag2=true;
        for (int i = 0; i< 5; i++){
            diag1 &= g[i][i].isCompleted();
            diag2 &= g[i][4 - i].isCompleted();
        }
        return  diag1 || diag2;
    }
}
