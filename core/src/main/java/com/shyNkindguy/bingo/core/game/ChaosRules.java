package com.shyNkindguy.bingo.core.game;

// Modo caos (config.yml `caos`): cada `everyMinutes` minutos de juego cada jugador recibe un ítem especial
// al azar de items.yml. 0 = apagado.
public record ChaosRules(int everyMinutes) {

    public static final int MAX_MINUTES = 60;
    public static final ChaosRules DEFAULT = new ChaosRules(0);

    public ChaosRules {
        if (everyMinutes < 0 || everyMinutes > MAX_MINUTES) {
            throw new IllegalArgumentException("Caos fuera de rango: " + everyMinutes);
        }
    }

    public boolean enabled() {
        return everyMinutes > 0;
    }

    // Si toca repartir en el segundo `elapsed` (el reloj se queda quieto en pausa: `lastDealt` evita repartir
    // dos veces en el mismo segundo).
    public boolean due(int elapsed, int lastDealt) {
        return enabled() && elapsed > 0 && elapsed != lastDealt && elapsed % (everyMinutes * 60) == 0;
    }
}
