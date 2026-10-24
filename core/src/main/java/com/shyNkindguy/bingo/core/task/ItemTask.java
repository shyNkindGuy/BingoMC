package com.shyNkindguy.bingo.core.task;

import com.shyNkindguy.bingo.core.event.ItemObtained;
import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.core.progress.TaskProgress;

// "Tener `amount` de `item` a la vez en el inventario". Cuenta el máximo que el jugador llegó a tener.
public record ItemTask(String id, String name, String item, int amount) implements Task {

    public ItemTask {
        if (amount < 1) throw new IllegalArgumentException("La cantidad tiene que ser al menos 1: " + amount);
    }

    @Override
    public String displayName() {
        return amount > 1 ? amount + " × " + name : name;
    }

    @Override
    public String icon() {
        return item;
    }

    @Override
    public int target() {
        return amount;
    }

    @Override
    public TaskProgress apply(TaskEvent event, TaskProgress progress) {
        return switch (event) {
            case ItemObtained obtained when obtained.item().equals(item) -> progress.reach(obtained.total());
            default -> progress;
        };
    }
}
