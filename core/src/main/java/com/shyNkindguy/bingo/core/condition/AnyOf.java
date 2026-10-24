package com.shyNkindguy.bingo.core.condition;

import com.shyNkindguy.bingo.core.event.EventContext;

import java.util.List;
import java.util.stream.Collectors;

// Alguna de estas ("en el desierto o en las badlands", "de noche o debajo de Y 0").
public record AnyOf(List<Condition> conditions) implements Condition {

    public AnyOf {
        conditions = List.copyOf(conditions);
        if (conditions.isEmpty()) throw new IllegalArgumentException("Hace falta al menos una condición");
    }

    @Override
    public boolean test(EventContext context) {
        return conditions.stream().anyMatch(condition -> condition.test(context));
    }

    @Override
    public String key() {
        return "alguna(" + conditions.stream().map(Condition::key).collect(Collectors.joining("|")) + ")";
    }
}
