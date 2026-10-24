package com.shyNkindguy.bingo.core.card;

import com.shyNkindguy.bingo.core.task.Task;

import java.util.*;

// Cartilla N×N: N sale de la cantidad de tareas (9 → 3×3, 25 → 5×5). Las tareas se reparten barajadas
// en una copia, así varias cartillas pueden salir de la misma lista. El avance lo aplica ProgressTracker.
public class BingoCard {
    public static final int DEFAULT_SIZE = 5;
    private final BingoCell[][] grid;

    public BingoCard(List<? extends Task> tasks){
        this(tasks, new Random());
    }

    public BingoCard(List<? extends Task> tasks, Random random){
        int size = (int) Math.round(Math.sqrt(tasks.size()));
        if (size == 0 || size * size != tasks.size()) {
            throw new IllegalArgumentException(
                    "Una cartilla N×N necesita N² tareas y llegaron " + tasks.size());
        }

        List<Task> shuffled = new ArrayList<>(tasks);
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
