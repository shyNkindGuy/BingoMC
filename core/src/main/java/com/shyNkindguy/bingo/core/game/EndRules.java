package com.shyNkindguy.bingo.core.game;

// Cómo termina la partida (config.yml): cuántos segundos dura el festejo (fuegos artificiales) y, después,
// si cada jugador vuelve a donde estaba al empezar y si se vacían los inventarios.
public record EndRules(boolean returnToOrigin, boolean clearInventory, int waitSeconds) {

    public static final int MAX_WAIT = 120;
    public static final EndRules DEFAULT = new EndRules(false, false, 10);

    public EndRules {
        if (waitSeconds < 0 || waitSeconds > MAX_WAIT) {
            throw new IllegalArgumentException("Espera final fuera de rango: " + waitSeconds);
        }
    }
}
