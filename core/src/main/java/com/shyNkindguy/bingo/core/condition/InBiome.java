package com.shyNkindguy.bingo.core.condition;

import com.shyNkindguy.bingo.core.event.EventContext;

import java.util.Set;
import java.util.TreeSet;

// En alguno de estos biomas ("minecraft:desert").
public record InBiome(Set<String> biomes) implements Condition {

    public InBiome {
        biomes = Set.copyOf(biomes);
    }

    @Override
    public boolean test(EventContext context) {
        return context.biome() != null && biomes.contains(context.biome());
    }

    @Override
    public String description() {
        return "en el bioma " + String.join(" o ", new TreeSet<>(biomes).stream()
                .map(biome -> biome.startsWith("minecraft:") ? biome.substring("minecraft:".length()) : biome).toList());
    }

    @Override
    public String key() {
        return "bioma=" + String.join(",", new TreeSet<>(biomes));
    }
}
