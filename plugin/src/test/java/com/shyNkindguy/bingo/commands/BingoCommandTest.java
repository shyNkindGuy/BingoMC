package com.shyNkindguy.bingo.commands;

import com.shyNkindguy.bingo.core.stats.PlayerStats;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BingoCommandTest {

    @Test
    void lasEstadisticasMuestranPorcentajePromedioYMejorTiempo() {
        List<String> lines = BingoCommand.statsLines(new PlayerStats("Ana", 3, 2, 14, 754));
        assertEquals(List.of(
                "§6§lBingo · §fAna",
                "§7Partidas: §f3 §7· victorias: §f2 §7(67%)",
                "§7Casillas completadas: §f14 §7(4,7 por partida)",
                "§7Victoria más rápida: §f12:34"), lines);
    }

    @Test
    void sinVictoriasLoDice() {
        List<String> lines = BingoCommand.statsLines(new PlayerStats("Beto", 2, 0, 1, -1));
        assertEquals("§7Todavía sin victorias.", lines.get(3));
    }

    @Test
    void autocompletaSinImportarMayusculas() {
        assertEquals(List.of("Ana", "anabel"), BingoCommand.matching(List.of("Ana", "Beto", "anabel"), "AN"));
    }
}
