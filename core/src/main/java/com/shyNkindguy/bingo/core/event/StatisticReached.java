package com.shyNkindguy.bingo.core.event;

// La estadística `statistic` ("minecraft:jump"), de `subject` si la estadística es por bloque, ítem o mob
// ("minecraft:mine_block" de "minecraft:stone"; null si no), vale `value` contando desde que empezó la
// partida.
public record StatisticReached(String statistic, String subject, int value) implements TaskEvent {
}
