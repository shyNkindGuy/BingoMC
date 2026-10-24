package com.shyNkindguy.bingo.core.task;

import com.shyNkindguy.bingo.core.condition.Condition;
import com.shyNkindguy.bingo.core.event.EventContext;
import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.core.progress.TaskProgress;

// Una tarea que solo avanza si `condition` se cumple en el momento del evento ("Filete en el Nether").
// Se ve igual que `inner` (nombre, ícono, cantidad); el id es propio para no chocar con la tarea sin
// condición.
public record ConditionalTask(String id, Task inner, Condition condition) implements Task {

    @Override
    public String displayName() {
        return inner.displayName();
    }

    @Override
    public String icon() {
        return inner.icon();
    }

    @Override
    public int target() {
        return inner.target();
    }

    @Override
    public TaskProgress apply(TaskEvent event, EventContext context, TaskProgress progress) {
        return condition.test(context) ? inner.apply(event, context, progress) : progress;
    }

    @Override
    public Task base() {
        return inner.base();
    }
}
