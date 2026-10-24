package com.shyNkindguy.bingo.core.win;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Quién gana cuando se acaba el tiempo: los que tienen más casillas (puede haber empate).
public final class TimeLimitWinners {

    // `lastCompletedAt`: segundo de partida en que completó su última casilla (-1 si ninguna).
    public record Score(String name, int completed, int lastCompletedAt) {
    }

    private TimeLimitWinners() {
    }

    // Los que tienen más casillas y, entre ellos, los que llegaron antes a esa cantidad (desempate); vacío
    // si nadie completó ninguna.
    public static List<String> winners(List<Score> scores) {
        int max = 0;
        for (Score score : scores) max = Math.max(max, score.completed());
        if (max == 0) return List.of();

        int earliest = Integer.MAX_VALUE;
        for (Score score : scores) {
            if (score.completed() == max) earliest = Math.min(earliest, score.lastCompletedAt());
        }
        List<String> winners = new ArrayList<>();
        for (Score score : scores) {
            if (score.completed() == max && score.lastCompletedAt() == earliest) winners.add(score.name());
        }
        return winners;
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
