package com.shyNkindguy.bingo.core.event;

// El jugador consiguió el logro `advancement` ("minecraft:story/mine_diamond").
public record AdvancementDone(String advancement) implements TaskEvent {
}
