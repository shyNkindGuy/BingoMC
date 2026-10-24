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
    public String description() {
        return "en " + String.join(" o ", new TreeSet<>(dimensions).stream().map(InDimension::name).toList());
    }

    private static String name(String dimension) {
        return switch (dimension) {
            case "minecraft:overworld" -> "el mundo normal";
            case "minecraft:the_nether" -> "el Nether";
            case "minecraft:the_end" -> "el End";
            default -> dimension;
        };
    }

    @Override
    public String key() {
        return "dimension=" + String.join(",", new TreeSet<>(dimensions));
    }
}
