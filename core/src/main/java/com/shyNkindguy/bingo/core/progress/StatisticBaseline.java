package com.shyNkindguy.bingo.core.progress;

import java.util.HashMap;
import java.util.Map;

// Las estadísticas de Minecraft cuentan desde siempre; una tarea cuenta desde que empezó la partida. Guarda
// el primer valor visto de cada estadística por jugador y devuelve cuánto subió desde ahí.
public final class StatisticBaseline<P> {

    private final Map<P, Map<String, Integer>> first = new HashMap<>();

    public int sinceStart(P player, String statistic, int current) {
        int base = first.computeIfAbsent(player, p -> new HashMap<>()).computeIfAbsent(statistic, s -> current);
        return Math.max(0, current - base);
    }

    public void clear() {
        first.clear();
    }
}
