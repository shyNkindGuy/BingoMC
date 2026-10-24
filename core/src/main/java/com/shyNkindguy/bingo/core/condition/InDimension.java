package com.shyNkindguy.bingo.core.condition;

import com.shyNkindguy.bingo.core.event.EventContext;

import java.util.Set;
import java.util.TreeSet;

// En alguna de estas dimensiones ("minecraft:the_nether").
public record InDimension(Set<String> dimensions) implements Condition {

    public InDimension {
        dimensions = Set.copyOf(dimensions);
    }

    @Override
    public boolean test(EventContext context) {
        return context.dimension() != null && dimensions.contains(context.dimension());
    }

    @Override
    public String key() {
        return "dimension=" + String.join(",", new TreeSet<>(dimensions));
    }
}
