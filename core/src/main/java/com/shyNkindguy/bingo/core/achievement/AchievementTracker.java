package com.shyNkindguy.bingo.core.achievement;

import com.shyNkindguy.bingo.core.bus.AchievementUnlocked;
import com.shyNkindguy.bingo.core.bus.CellCompleted;
import com.shyNkindguy.bingo.core.bus.DomainEvents;
import com.shyNkindguy.bingo.core.bus.GameEnded;
import com.shyNkindguy.bingo.core.stats.PlayerStats;
import com.shyNkindguy.bingo.core.stats.StatsBook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

// Revisa los logros con los eventos de la partida y, la primera vez que alguien cumple uno, lo anota en el
// StatsBook (se guarda con stats.yml) y publica AchievementUnlocked. Los logros salen de `achievements` en
// cada evento, así un /bingo reload los cambia sin tocar nada más.
public final class AchievementTracker {
    private final Supplier<List<Achievement>> achievements;
    private final StatsBook book;
    private final DomainEvents events;
    private final Map<String, Integer> cellsInGame = new HashMap<>();

    public AchievementTracker(Supplier<List<Achievement>> achievements, StatsBook book, DomainEvents events) {
        this.achievements = achievements;
        this.book = book;
        this.events = events;
    }

    // Después de StatsBook.subscribeTo: los logros de partida miran las estadísticas con la partida ya sumada.
    public void subscribe() {
        events.subscribe(CellCompleted.class, this::onCellCompleted);
        events.subscribe(GameEnded.class, this::onGameEnded);
    }

    void onCellCompleted(CellCompleted event) {
        cellsInGame.merge(event.playerId(), 1, Integer::sum);
        for (Achievement achievement : achievements.get()) {
            if (achievement.rule() instanceof CellRule rule && rule.matches(event)) {
                unlock(event.playerId(), event.playerName(), achievement);
            }
        }
    }

    void onGameEnded(GameEnded event) {
        if (!event.aborted()) {
            for (GameEnded.Participant player : event.players()) {
                PlayerStats after = book.get(player.id()).orElse(PlayerStats.empty(player.name()));
                int cells = cellsInGame.getOrDefault(player.id(), 0);
                for (Achievement achievement : achievements.get()) {
                    if (achievement.rule() instanceof GameRule rule && rule.matches(event, player, cells, after)) {
                        unlock(player.id(), player.name(), achievement);
                    }
                }
            }
        }
        cellsInGame.clear();
    }

    private void unlock(String playerId, String playerName, Achievement achievement) {
        if (book.unlock(playerId, playerName, achievement.id())) {
            events.publish(new AchievementUnlocked(playerId, playerName, achievement.id(), achievement.name(),
                    achievement.description()));
        }
    }
}
