package com.shyNkindguy.bingo.core.event;

// El jugador mató un `entity` ("minecraft:zombie"). Uno por muerte.
public record EntityKilled(String entity) implements TaskEvent {
}
