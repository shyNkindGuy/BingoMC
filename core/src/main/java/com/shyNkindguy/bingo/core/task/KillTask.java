package com.shyNkindguy.bingo.core.task;

import com.shyNkindguy.bingo.core.event.EntityKilled;
import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.core.progress.TaskProgress;

// "Matar `amount` de `entity`" durante la partida. Ícono por defecto: el huevo de ese mob.
public record KillTask(String id, String name, String entity, int amount, String icon) implements Task {

    public KillTask {
        if (amount < 1) throw new IllegalArgumentException("La cantidad tiene que ser al menos 1: " + amount);
        if (icon == null) icon = entity + "_spawn_egg";
    }

    public KillTask(String id, String name, String entity, int amount) {
        this(id, name, entity, amount, null);
    }

    @Override
    public String displayName() {
        return amount > 1 ? amount + " × " + name : name;
    }

    @Override
    public int target() {
        return amount;
    }

    @Override
    public TaskProgress apply(TaskEvent event, TaskProgress progress) {
        return switch (event) {
            case EntityKilled killed when killed.entity().equals(entity) -> progress.reach(progress.current() + 1);
            default -> progress;
        };
    }
}
