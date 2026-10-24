package com.shyNkindguy.bingo.core.stats;

import java.util.Objects;

// Lo que lleva jugado alguien: partidas terminadas (las detenidas con /bingo stop no cuentan), victorias,
// casillas que completó él y su victoria más rápida en segundos de juego (-1 si todavía no ganó). `name` es
// el último nombre con el que jugó.
public record PlayerStats(String name, int games, int wins, int cells, int bestWinSeconds) {

    public PlayerStats {
        Objects.requireNonNull(name, "name");
        if (games < 0 || wins < 0 || cells < 0) throw new IllegalArgumentException("negativo: " + games + "/" + wins + "/" + cells);
        if (wins > games) throw new IllegalArgumentException("más victorias (" + wins + ") que partidas (" + games + ")");
        if (bestWinSeconds < -1) throw new IllegalArgumentException("bestWinSeconds < -1: " + bestWinSeconds);
    }

    public static PlayerStats empty(String name) {
        return new PlayerStats(name, 0, 0, 0, -1);
    }

    // Con una partida más: `cells` casillas suyas y, si ganó, `seconds` cuenta para la más rápida.
    public PlayerStats plusGame(String newName, boolean won, int newCells, int seconds) {
        int best = bestWinSeconds;
        if (won && (best < 0 || seconds < best)) best = seconds;
        return new PlayerStats(newName, games + 1, wins + (won ? 1 : 0), cells + newCells, best);
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
