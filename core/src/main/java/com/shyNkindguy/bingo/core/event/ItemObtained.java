package com.shyNkindguy.bingo.core.event;

// El jugador tiene ahora `total` unidades de `item` ("minecraft:oak_log") en su inventario.
public record ItemObtained(String item, int total) implements TaskEvent {
}
