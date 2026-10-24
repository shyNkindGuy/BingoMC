package com.shyNkindguy.bingo.core.task;

import com.shyNkindguy.bingo.core.event.StatisticReached;
import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.core.progress.TaskProgress;

import java.util.Objects;

// "Que la estadística `statistic` (de `subject`, si es por bloque, ítem o mob) llegue a `amount` desde que
// empezó la partida": 200 saltos, 64 piedras picadas. Ícono por defecto: `subject`, si hay.
public record StatisticTask(String id, String name, String statistic, String subject, int amount, String icon)
        implements Task {

    public StatisticTask {
        if (amount < 1) throw new IllegalArgumentException("La cantidad tiene que ser al menos 1: " + amount);
        if (icon == null) icon = subject;
    }

    public StatisticTask(String id, String name, String statistic, String subject, int amount) {
        this(id, name, statistic, subject, amount, null);
    }

    // "minecraft:jump" o "minecraft:mine_block/minecraft:stone": una clave por estadística medida.
    public String key() {
        return subject == null ? statistic : statistic + "/" + subject;
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
            case StatisticReached reached when reached.statistic().equals(statistic)
                    && Objects.equals(reached.subject(), subject) -> progress.reach(reached.value());
            default -> progress;
        };
    }
}
