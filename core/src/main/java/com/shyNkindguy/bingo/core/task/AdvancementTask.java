package com.shyNkindguy.bingo.core.task;

import com.shyNkindguy.bingo.core.event.AdvancementDone;
import com.shyNkindguy.bingo.core.event.EventContext;
import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.core.progress.TaskProgress;

// "Conseguir el logro `advancement` de Minecraft" durante la partida. Sin `icon`, el plugin dibuja el
// ícono del propio logro.
public record AdvancementTask(String id, String name, String advancement, String icon) implements Task {

    public AdvancementTask(String id, String name, String advancement) {
        this(id, name, advancement, null);
    }

    @Override
    public String displayName() {
        return name;
    }

    @Override
    public int target() {
        return 1;
    }

    @Override
    public TaskProgress apply(TaskEvent event, EventContext context, TaskProgress progress) {
        return switch (event) {
            case AdvancementDone done when done.advancement().equals(advancement) -> progress.reach(1);
            default -> progress;
        };
    }
}
