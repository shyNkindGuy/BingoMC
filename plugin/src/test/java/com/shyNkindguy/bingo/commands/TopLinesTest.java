package com.shyNkindguy.bingo.commands;

import com.shyNkindguy.bingo.core.stats.PlayerStats;
import com.shyNkindguy.bingo.core.stats.Ranking;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TopLinesTest {
    private static final Map<String, PlayerStats> TODOS = Map.of(
            "id-ana", new PlayerStats("Ana", 5, 3, 40, 754),
            "id-beto", new PlayerStats("Beto", 4, 1, 20, 600));

    @Test
    void elTopPorVictoriasYPorTiempo() {
        assertEquals(List.of("§6§lTop · victorias", "§e1. §fAna §7— §f3", "§e2. §fBeto §7— §f1"),
                BingoCommand.topLines(Ranking.top(TODOS, Ranking.Metric.WINS, 10), Ranking.Metric.WINS));
        assertEquals(List.of("§6§lTop · victoria más rápida", "§e1. §fBeto §7— §f10:00", "§e2. §fAna §7— §f12:34"),
                BingoCommand.topLines(Ranking.top(TODOS, Ranking.Metric.FASTEST, 10), Ranking.Metric.FASTEST));
    }

    @Test
    void sinPartidasLoDice() {
        assertEquals(List.of("§7Nadie terminó una partida todavía."), BingoCommand.topLines(List.of(), Ranking.Metric.GAMES));
    }
}
