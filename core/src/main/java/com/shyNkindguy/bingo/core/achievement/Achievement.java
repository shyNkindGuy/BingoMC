package com.shyNkindguy.bingo.core.achievement;

import java.util.Objects;

// Un logro de achievements.yml: se desbloquea una sola vez por jugador, cuando se cumple su regla. `icon`:
// clave del ítem para la GUI (null = uno por defecto).
public record Achievement(String id, String name, String description, String icon, AchievementRule rule) {

    public Achievement {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(rule, "rule");
    }
}
