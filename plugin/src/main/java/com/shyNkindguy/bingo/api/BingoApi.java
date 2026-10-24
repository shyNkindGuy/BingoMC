package com.shyNkindguy.bingo.api;

import com.shyNkindguy.bingo.core.bus.DomainEvent;
import com.shyNkindguy.bingo.core.bus.DomainEvents;
import com.shyNkindguy.bingo.core.stats.PlayerStats;
import com.shyNkindguy.bingo.core.stats.Ranking;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.storage.StatsService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

// AdaptiveBingoApi sobre la partida y las estadísticas de este servidor.
public class BingoApi implements AdaptiveBingoApi {
    private final BingoGame game;
    private final StatsService stats;

    public BingoApi(BingoGame game, StatsService stats) {
        this.game = game;
        this.stats = stats;
    }

    @Override
    public boolean inGame() {
        return game.inGame();
    }

    @Override
    public boolean isRunning() {
        return game.isRunning();
    }

    @Override
    public int progress(UUID player) {
        return game.getProgress(player);
    }

    @Override
    public Optional<PlayerStats> stats(UUID player) {
        return stats.book().get(player.toString());
    }

    @Override
    public List<Ranking.Entry> top(Ranking.Metric metric, int limit) {
        return Ranking.top(stats.book().snapshot(), metric, limit);
    }

    @Override
    public <E extends DomainEvent> DomainEvents.Subscription subscribe(Class<E> type, Consumer<? super E> listener) {
        return game.events().subscribe(type, listener);
    }
}
