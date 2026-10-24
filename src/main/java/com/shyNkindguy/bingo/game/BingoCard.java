package com.shyNkindguy.bingo.game;

import org.bukkit.Material;

import java.util.*;

public class BingoCard {
    private static final int SIZE = 5;
    private final BingoCell[][] grid = new BingoCell[SIZE][SIZE];

    public BingoCard(List<BingoObjective> objectives){
        Collections.shuffle(objectives);

        int index = 0;

        for (int row = 0; row < SIZE; row++){
            for (int col=0; col < SIZE; col++){
                grid[row][col] = new BingoCell(objectives.get(index++));
            }
        }
    }
    // Devuelve el objetivo que se completó, o null si el material no estaba pendiente en la cartilla.
    public BingoObjective completeByMaterial(Material material) {
        for (BingoCell[] row : grid) {
            for (BingoCell cell : row) {

                if (!cell.isCompleted()
                        && cell.getObjective().getMaterial() == material) {

                    cell.setCompleted(true);
                    return cell.getObjective();
                }
            }
        }
        return null;
    }

    public BingoCell[][] getGrid(){
        return grid;
    }
    }



