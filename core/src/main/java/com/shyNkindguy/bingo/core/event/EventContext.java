package com.shyNkindguy.bingo.core.event;

// Dónde y cómo estaba el jugador cuando pasó el evento, para las condiciones de las tareas: dimensión
// ("minecraft:the_nether"), bioma ("minecraft:desert"), hora del mundo (0–23999), altura Y y cuántas veces
// murió desde que empezó la partida. null = no se sabe (con NONE no se cumple ninguna condición).
public record EventContext(String dimension, String biome, Long timeOfDay, Integer y, int deaths) {

    public static final EventContext NONE = new EventContext(null, null, null, null, 0);
}
