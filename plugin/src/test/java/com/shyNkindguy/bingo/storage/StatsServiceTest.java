package com.shyNkindguy.bingo.storage;

import com.shyNkindguy.bingo.core.achievement.Achievement;
import com.shyNkindguy.bingo.core.achievement.GameRule;
import com.shyNkindguy.bingo.core.bus.CellCompleted;
import com.shyNkindguy.bingo.core.bus.DomainEvents;
import com.shyNkindguy.bingo.core.bus.GameEnded;
import com.shyNkindguy.bingo.core.event.EventContext;
import com.shyNkindguy.bingo.core.stats.PlayerStats;
import com.shyNkindguy.bingo.core.stats.StatsException;
import com.shyNkindguy.bingo.core.stats.StatsRepository;
import com.shyNkindguy.bingo.core.win.WinMode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatsServiceTest {
    private static final WinMode LINE = new WinMode.Lines(1);
    private static final GameEnded.Participant ANA = new GameEnded.Participant("id-ana", "Ana", "rojo", 0);

    private static final class MemoryRepository implements StatsRepository {
        Map<String, PlayerStats> stored;
        int saves;
        boolean broken;

        MemoryRepository(Map<String, PlayerStats> stored) {
            this.stored = stored;
        }

        @Override
        public Map<String, PlayerStats> load() {
            if (broken) throw new StatsException("stats.yml, jugador x: falta nombre");
            return stored;
        }

        @Override
        public void save(Map<String, PlayerStats> stats) {
            saves++;
            stored = stats;
        }
    }

    private final List<String> logged = new ArrayList<>();

    private Logger logger() {
        Logger log = Logger.getAnonymousLogger();
        log.setUseParentHandlers(false);
        log.addHandler(new Handler() {
            @Override public void publish(LogRecord record) { logged.add(record.getMessage()); }
            @Override public void flush() { }
            @Override public void close() { }
        });
        return log;
    }

    @Test
    void cargaSigueLasPartidasYGuardaAlTerminar() {
        MemoryRepository repository = new MemoryRepository(Map.of("id-ana", new PlayerStats("Ana", 1, 0, 2, -1)));
        DomainEvents events = new DomainEvents();
        StatsService service = new StatsService(logger(), repository);
        service.start(events, List::of);

        events.publish(new CellCompleted("rojo", "id-ana", "Ana", "item:dirt", "Tierra", 5, EventContext.NONE));
        events.publish(new GameEnded(400, false, LINE, Set.of("rojo"), List.of(ANA)));

        assertEquals(1, repository.saves);
        assertEquals(new PlayerStats("Ana", 2, 1, 3, 400), repository.stored.get("id-ana"));
        assertEquals(repository.stored, service.book().snapshot());
    }

    @Test
    void losLogrosQuedanGuardados() {
        MemoryRepository repository = new MemoryRepository(Map.of());
        DomainEvents events = new DomainEvents();
        Achievement primera = new Achievement("primera", "Primera", "Termina una partida", null,
                new GameRule(null, 0, null, 0, false, 0, 0, 0));
        new StatsService(logger(), repository).start(events, () -> List.of(primera));

        events.publish(new GameEnded(400, false, LINE, Set.of(), List.of(ANA)));

        assertTrue(repository.stored.get("id-ana").hasAchievement("primera"));
        assertEquals(1, repository.stored.get("id-ana").games());
    }

    @Test
    void unaPartidaDetenidaNoSeGuarda() {
        MemoryRepository repository = new MemoryRepository(Map.of());
        DomainEvents events = new DomainEvents();
        new StatsService(logger(), repository).start(events, List::of);

        events.publish(new GameEnded(400, true, LINE, Set.of(), List.of(ANA)));

        assertEquals(0, repository.saves);
    }

    @Test
    void conUnErrorEnStatsYmlNoLoPisa() {
        MemoryRepository repository = new MemoryRepository(Map.of());
        repository.broken = true;
        DomainEvents events = new DomainEvents();
        StatsService service = new StatsService(logger(), repository);
        service.start(events, List::of);

        events.publish(new GameEnded(400, false, LINE, Set.of("rojo"), List.of(ANA)));

        assertEquals(0, repository.saves);
        assertEquals(1, service.book().get("id-ana").orElseThrow().games());
        assertTrue(logged.contains("stats.yml, jugador x: falta nombre"), logged.toString());
    }
}
