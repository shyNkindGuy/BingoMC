package com.shyNkindguy.bingo.core.condition;

import com.shyNkindguy.bingo.core.event.EventContext;

import java.util.List;
import java.util.stream.Collectors;

// Todas a la vez (lo que pasa con varias condiciones en una tarea).
public record AllOf(List<Condition> conditions) implements Condition {

    public AllOf {
        conditions = List.copyOf(conditions);
        if (conditions.isEmpty()) throw new IllegalArgumentException("Hace falta al menos una condición");
    }

    @Override
    public boolean test(EventContext context) {
        return conditions.stream().allMatch(condition -> condition.test(context));
    }

    @Override
    public String description() {
        return conditions.stream().map(Condition::description).collect(Collectors.joining(" y "));
    }

    @Override
    public String key() {
        return conditions.stream().map(Condition::key).collect(Collectors.joining("&"));
    }
}
