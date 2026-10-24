package com.shyNkindguy.bingo.core.stats;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

// Lo que lleva jugado alguien: partidas terminadas (las detenidas con /bingo stop no cuentan), victorias,
// casillas que completó él, su victoria más rápida en segundos de juego (-1 si todavía no ganó) y los ids de
// los logros que desbloqueó (desde la 3.2). `name` es el último nombre con el que jugó.
public record PlayerStats(String name, int games, int wins, int cells, int bestWinSeconds, Set<String> achievements) {

    public PlayerStats {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(achievements, "achievements");
        achievements = Collections.unmodifiableSortedSet(new TreeSet<>(achievements));
        if (games < 0 || wins < 0 || cells < 0) throw new IllegalArgumentException("negativo: " + games + "/" + wins + "/" + cells);
        if (wins > games) throw new IllegalArgumentException("más victorias (" + wins + ") que partidas (" + games + ")");
        if (bestWinSeconds < -1) throw new IllegalArgumentException("bestWinSeconds < -1: " + bestWinSeconds);
    }

    public PlayerStats(String name, int games, int wins, int cells, int bestWinSeconds) {
        this(name, games, wins, cells, bestWinSeconds, Set.of());
    }

    public static PlayerStats empty(String name) {
        return new PlayerStats(name, 0, 0, 0, -1);
    }

    // Con una partida más: `cells` casillas suyas y, si ganó, `seconds` cuenta para la más rápida.
    public PlayerStats plusGame(String newName, boolean won, int newCells, int seconds) {
        int best = bestWinSeconds;
        if (won && (best < 0 || seconds < best)) best = seconds;
        return new PlayerStats(newName, games + 1, wins + (won ? 1 : 0), cells + newCells, best, achievements);
    }

    public boolean hasAchievement(String id) {
        return achievements.contains(id);
    }

    public PlayerStats withAchievement(String newName, String id) {
        Set<String> more = new TreeSet<>(achievements);
        more.add(id);
        return new PlayerStats(newName, games, wins, cells, bestWinSeconds, more);
    }

    public boolean hasWon() {
        return bestWinSeconds >= 0;
    }

    // 0–100, redondeado.
    public int winPercent() {
        return games == 0 ? 0 : Math.round(100f * wins / games);
    }

    public double cellsPerGame() {
        return games == 0 ? 0 : (double) cells / games;
    }
}
