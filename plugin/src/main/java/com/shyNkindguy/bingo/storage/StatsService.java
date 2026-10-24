package com.shyNkindguy.bingo.storage;

import com.shyNkindguy.bingo.core.achievement.Achievement;
import com.shyNkindguy.bingo.core.achievement.AchievementTracker;
import com.shyNkindguy.bingo.core.bus.AchievementUnlocked;
import com.shyNkindguy.bingo.core.bus.DomainEvents;
import com.shyNkindguy.bingo.core.bus.GameEnded;
import com.shyNkindguy.bingo.core.stats.StatsBook;
import com.shyNkindguy.bingo.core.stats.StatsException;
import com.shyNkindguy.bingo.core.stats.StatsRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

// Las estadísticas y los logros de los jugadores (stats.yml en plugins/AdaptiveBingo/): las lee al arrancar,
// las tiene al día con los eventos de las partidas (StatsBook y AchievementTracker) y las guarda cada vez que
// termina una o alguien desbloquea un logro (es un archivo chico y pasa pocas veces: se escribe en el
// momento). Si stats.yml tiene un error, lo dice y no lo pisa: lo de esta sesión queda en memoria hasta que
// se arregle y se reinicie.
public class StatsService {
    private final Logger log;
    private final StatsRepository repository;
    private StatsBook book = new StatsBook(Map.of());
    private boolean writable;

    public StatsService(Logger log, StatsRepository repository) {
        this.log = log;
        this.repository = repository;
    }

    // `achievements`: los de achievements.yml en ese momento (cambian con /bingo reload).
    public void start(DomainEvents events, Supplier<List<Achievement>> achievements) {
        try {
            book = new StatsBook(repository.load());
            writable = true;
        } catch (StatsException e) {
            log.severe(e.getMessage());
            log.severe("No se van a guardar las estadísticas hasta que se arregle stats.yml y se reinicie el servidor.");
            book = new StatsBook(Map.of());
            writable = false;
        }
        book.subscribeTo(events);
        new AchievementTracker(achievements, book, events).subscribe();
        events.subscribe(GameEnded.class, event -> {
            if (!event.aborted()) save();
        });
        events.subscribe(AchievementUnlocked.class, event -> save());
    }

    public StatsBook book() {
        return book;
    }

    private void save() {
        if (!writable) {
            log.warning("Las estadísticas no se guardaron: stats.yml tiene un error.");
            return;
        }
        try {
            repository.save(book.snapshot());
        } catch (StatsException e) {
            log.severe(e.getMessage());
        }
    }
}
