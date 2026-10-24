package com.shyNkindguy.bingo.game;

import com.shyNkindguy.bingo.adapter.Keys;
import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.ObjectivePool;
import com.shyNkindguy.bingo.core.task.Task;

import java.util.*;

public class ObjectiveFactory {
    private static final Random RANDOM = new Random();

    // Los objetivos de una partida: una cartilla de 5×5 sorteada del pool, los mismos para todos los
    // jugadores. Los ítems que no existen en esta versión se saltean (ObjectivePoolMaterialsTest lo evita).
    public static List<Task> createObjectives() {
        int cells = BingoCard.DEFAULT_SIZE * BingoCard.DEFAULT_SIZE;
        return ObjectivePool.draw(ObjectivePool.DEFAULT, cells, RANDOM, item -> Keys.toMaterial(item) != null);
    }
}
