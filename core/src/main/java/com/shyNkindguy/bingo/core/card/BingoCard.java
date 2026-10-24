package com.shyNkindguy.bingo.core.card;

import java.util.*;

// Cartilla N×N: N sale de la cantidad de objetivos (9 → 3×3, 25 → 5×5). Los objetivos se reparten
// barajados en una copia, así varias cartillas pueden salir de la misma lista.
public class BingoCard {
    public static final int DEFAULT_SIZE = 5;
    private final BingoCell[][] grid;

    public BingoCard(List<BingoObjective> objectives){
        this(objectives, new Random());
    }

    public BingoCard(List<BingoObjective> objectives, Random random){
        int size = (int) Math.round(Math.sqrt(objectives.size()));
        if (size == 0 || size * size != objectives.size()) {
            throw new IllegalArgumentException(
                    "Una cartilla N×N necesita N² objetivos y llegaron " + objectives.size());
        }

        List<BingoObjective> shuffled = new ArrayList<>(objectives);
        Collections.shuffle(shuffled, random);

        grid = new BingoCell[size][size];
        int index = 0;

        for (int row = 0; row < size; row++){
            for (int col=0; col < size; col++){
                grid[row][col] = new BingoCell(shuffled.get(index++));
            }
        }
    }

    public int getSize() {
        return grid.length;
    }

    // Devuelve el objetivo que se completó, o null si el ítem no estaba pendiente en la cartilla.
    public BingoObjective completeByItem(String item) {
        for (BingoCell[] row : grid) {
            for (BingoCell cell : row) {

                if (!cell.isCompleted()
                        && cell.getObjective().getItem().equals(item)) {

                    cell.setCompleted(true);
                    return cell.getObjective();
                }
            }
        }
        return null;
    }

    public int countCompleted() {
        int completed = 0;
        for (BingoCell[] row : grid) {
            for (BingoCell cell : row) {
                if (cell.isCompleted()) completed++;
            }
        }
        return completed;
    }

    public BingoCell[][] getGrid(){
        return grid;
    }
    }
