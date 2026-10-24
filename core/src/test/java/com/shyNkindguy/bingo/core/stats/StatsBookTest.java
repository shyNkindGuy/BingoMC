package com.shyNkindguy.bingo.core.stats;

import com.shyNkindguy.bingo.core.bus.CellCompleted;
import com.shyNkindguy.bingo.core.bus.DomainEvents;
import com.shyNkindguy.bingo.core.bus.GameEnded;
import com.shyNkindguy.bingo.core.event.EventContext;
import com.shyNkindguy.bingo.core.win.WinMode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatsBookTest {
    private static final WinMode LINE = new WinMode.Lines(1);
    private static final GameEnded.Participant ANA = new GameEnded.Participant("id-ana", "Ana", "rojo", 0);
    private static final GameEnded.Participant BETO = new GameEnded.Participant("id-beto", "Beto", "rojo", 0);
    private static final GameEnded.Participant CARLA = new GameEnded.Participant("id-carla", "Carla", "azul", 0);

    private static CellCompleted cell(GameEnded.Participant player, String task) {
        return new CellCompleted(player.team(), player.id(), player.name(), task, task, 10, EventContext.NONE);
    }

    @Test
    void alTerminarSumaPartidasVictoriasCasillasYMejorTiempo() {
        DomainEvents events = new DomainEvents();
        StatsBook book = new StatsBook(Map.of());
        book.subscribeTo(events);

        events.publish(cell(ANA, "a"));
        events.publish(cell(ANA, "b"));
        events.publish(cell(CARLA, "c"));
        events.publish(new GameEnded(900, false, LINE, Set.of("rojo"), List.of(ANA, BETO, CARLA)));

        assertEquals(new PlayerStats("Ana", 1, 1, 2, 900), book.get("id-ana").orElseThrow());
        assertEquals(new PlayerStats("Beto", 1, 1, 0, 900), book.get("id-beto").orElseThrow());
        assertEquals(new PlayerStats("Carla", 1, 0, 1, -1), book.get("id-carla").orElseThrow());

        events.publish(cell(CARLA, "d"));
        events.publish(new GameEnded(1200, false, LINE, Set.of("azul"), List.of(ANA, CARLA)));
        events.publish(new GameEnded(600, false, LINE, Set.of("rojo"), List.of(ANA)));

        assertEquals(new PlayerStats("Ana", 3, 2, 2, 600), book.get("id-ana").orElseThrow());
        assertEquals(new PlayerStats("Carla", 2, 1, 2, 1200), book.get("id-carla").orElseThrow());
    }

    @Test
    void unaPartidaDetenidaNoSumaNiDejaCasillasParaLaSiguiente() {
        StatsBook book = new StatsBook(Map.of());
        book.onCellCompleted(cell(ANA, "a"));
        book.onGameEnded(new GameEnded(300, true, LINE, Set.of(), List.of(ANA)));
        assertTrue(book.get("id-ana").isEmpty());

        book.onGameEnded(new GameEnded(300, false, LINE, Set.of(), List.of(ANA)));
        assertEquals(new PlayerStats("Ana", 1, 0, 0, -1), book.get("id-ana").orElseThrow());
    }

    @Test
    void siguieDesdeLoCargadoYGuardaElUltimoNombre() {
        StatsBook book = new StatsBook(Map.of("id-ana", new PlayerStats("AnaVieja", 4, 1, 9, 700)));
        book.onCellCompleted(cell(ANA, "a"));
        book.onGameEnded(new GameEnded(800, false, LINE, Set.of("rojo"), List.of(ANA)));

        assertEquals(new PlayerStats("Ana", 5, 2, 10, 700), book.get("id-ana").orElseThrow());
        assertEquals(List.of("Ana"), book.names());
        assertEquals(Map.of("id-ana", new PlayerStats("Ana", 5, 2, 10, 700)), book.snapshot());
    }

    @Test
    void buscaPorNombreSinImportarMayusculas() {
        StatsBook book = new StatsBook(Map.of(
                "id-1", new PlayerStats("Ana", 2, 0, 0, -1),
                "id-2", new PlayerStats("ana", 5, 0, 0, -1)));
        assertEquals(5, book.findByName("ANA").orElseThrow().games());
        assertFalse(book.findByName("Beto").isPresent());
    }

    @Test
    void porcentajeYPromedio() {
        PlayerStats stats = new PlayerStats("Ana", 3, 2, 10, 500);
        assertEquals(67, stats.winPercent());
        assertEquals(10.0 / 3, stats.cellsPerGame(), 1e-9);
        assertTrue(stats.hasWon());
        assertEquals(0, PlayerStats.empty("Beto").winPercent());
        assertEquals(0, PlayerStats.empty("Beto").cellsPerGame());
        assertFalse(PlayerStats.empty("Beto").hasWon());
    }

    @Test
    void noAceptaValoresImposibles() {
        assertThrows(IllegalArgumentException.class, () -> new PlayerStats("Ana", 1, 2, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new PlayerStats("Ana", -1, 0, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> new PlayerStats("Ana", 1, 0, 0, -2));
    }
}
