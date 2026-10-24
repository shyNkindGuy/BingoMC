package com.shyNkindguy.bingo.core.bus;

import java.util.Objects;

// Un jugador desbloqueó un logro de achievements.yml por primera vez.
public record AchievementUnlocked(String playerId, String playerName, String achievementId, String name,
                                  String description) implements DomainEvent {

    public AchievementUnlocked {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(playerName, "playerName");
        Objects.requireNonNull(achievementId, "achievementId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
    }
}
