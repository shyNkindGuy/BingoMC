package com.shyNkindguy.bingo.core.win;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Quién gana cuando se acaba el tiempo: los que tienen más casillas (puede haber empate).
public final class TimeLimitWinners {

    private TimeLimitWinners() {
    }

    // Nombres con más casillas, en el orden del mapa; vacío si nadie completó ninguna.
    public static List<String> leaders(Map<String, Integer> completedByPlayer) {
        int max = 0;
        for (int completed : completedByPlayer.values()) {
            max = Math.max(max, completed);
        }
        if (max == 0) return List.of();

        List<String> leaders = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : completedByPlayer.entrySet()) {
            if (entry.getValue() == max) leaders.add(entry.getKey());
        }
        return leaders;
    }
}
