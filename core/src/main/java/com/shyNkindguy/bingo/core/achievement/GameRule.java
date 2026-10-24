package com.shyNkindguy.bingo.core.achievement;

import com.shyNkindguy.bingo.core.bus.GameEnded;
import com.shyNkindguy.bingo.core.stats.PlayerStats;
import com.shyNkindguy.bingo.core.win.WinMode;

// al: partida. Se revisa al terminar cada partida (no las detenidas), con las estadísticas ya sumadas. Lo que
// esté en "da igual" no pide nada: `won` null, `withinSeconds` 0, `mode` null, `cellsInGame` 0, `noDeath`
// false y los totales en 0.
// - won: ganó (o perdió, con false).
// - withinSeconds: la partida duró menos que eso.
// - mode: se jugó con ese modo.
// - cellsInGame: completó al menos esas casillas en la partida.
// - noDeath: no murió (si se desconectó no se sabe, y no cuenta).
// - totalGames, totalWins, totalCells: lo que lleva desde siempre, con esta partida.
public record GameRule(Boolean won, int withinSeconds, WinMode mode, int cellsInGame, boolean noDeath,
                       int totalGames, int totalWins, int totalCells) implements AchievementRule {

    public GameRule {
        if (withinSeconds < 0 || cellsInGame < 0 || totalGames < 0 || totalWins < 0 || totalCells < 0) {
            throw new IllegalArgumentException("valores negativos");
        }
    }

    public boolean matches(GameEnded event, GameEnded.Participant player, int cells, PlayerStats after) {
        if (won != null && won != event.won(player)) return false;
        if (withinSeconds > 0 && event.seconds() >= withinSeconds) return false;
        if (mode != null && !mode.equals(event.mode())) return false;
        if (cells < cellsInGame) return false;
        if (noDeath && player.deaths() != 0) return false;
        return after.games() >= totalGames && after.wins() >= totalWins && after.cells() >= totalCells;
    }
}
