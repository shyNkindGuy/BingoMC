package com.shyNkindguy.bingo.core.item;

import java.util.Objects;

// Un ítem especial de items.yml: `material` es la clave del ítem de Minecraft que lo lleva
// ("minecraft:amethyst_shard"); el plugin lo marca para no confundirlo con uno normal.
public record SpecialItem(String id, String name, String description, String material, ItemEffect effect) {

    public SpecialItem {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(effect, "effect");
    }
}
