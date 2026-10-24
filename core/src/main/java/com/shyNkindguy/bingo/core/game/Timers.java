package com.shyNkindguy.bingo.core.game;

import java.util.HashMap;
import java.util.Map;

// Cuánto le queda a cada jugador de algo que dura unos segundos de partida (el escudo anti-PvP, la espera de
// la brújula). Con el segundo de partida, así en pausa no corre.
public final class Timers<P> {
    private final Map<P, Integer> until = new HashMap<>();

    // Suma `seconds` a lo que le quedaba (o desde `now`, si no le quedaba nada).
    public void add(P player, int now, int seconds) {
        if (seconds < 0) throw new IllegalArgumentException("seconds < 0: " + seconds);
        until.put(player, Math.max(now, until.getOrDefault(player, now)) + seconds);
    }

    public int remaining(P player, int now) {
        Integer end = until.get(player);
        return end == null ? 0 : Math.max(0, end - now);
    }

    public boolean active(P player, int now) {
        return remaining(player, now) > 0;
    }

    public void clear() {
        until.clear();
    }
}
