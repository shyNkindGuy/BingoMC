package com.shyNkindguy.bingo.core.game;

import com.shyNkindguy.bingo.core.win.WinMode;

// Cómo se juega (config.yml; /bingo start puede cambiar la cartilla y la semilla): qué cartilla de
// cards.yml (null = su por_defecto), segundos de cuenta regresiva, minutos de partida (0 = sin límite), si
// hay PvP, una semilla fija (null = al azar en cada partida), cómo se gana, cómo empieza y cómo termina.
public record GameSettings(String card, int countdownSeconds, int durationMinutes, boolean pvp, Long seed,
                           WinMode mode, StartRules start, EndRules end) {

    public static final int MAX_COUNTDOWN = 60;
    public static final int MAX_DURATION = 600;
    public static final GameSettings DEFAULT =
            new GameSettings(null, 10, 20, true, null, WinMode.DEFAULT, StartRules.DEFAULT, EndRules.DEFAULT);

    public GameSettings {
        if (mode == null) throw new IllegalArgumentException("Falta el modo");
        if (start == null || end == null) throw new IllegalArgumentException("Faltan las reglas de inicio o de fin");
        if (mode instanceof WinMode.TimeOnly && durationMinutes == 0) {
            throw new IllegalArgumentException("Por tiempo hace falta una duración");
        }
        if (countdownSeconds < 0 || countdownSeconds > MAX_COUNTDOWN) {
            throw new IllegalArgumentException("Cuenta regresiva fuera de rango: " + countdownSeconds);
        }
        if (durationMinutes < 0 || durationMinutes > MAX_DURATION) {
            throw new IllegalArgumentException("Duración fuera de rango: " + durationMinutes);
        }
    }

    public int durationSeconds() {
        return durationMinutes * 60;
    }

    public GameSettings withCard(String card) {
        return new GameSettings(card, countdownSeconds, durationMinutes, pvp, seed, mode, start, end);
    }

    public GameSettings withSeed(Long seed) {
        return new GameSettings(card, countdownSeconds, durationMinutes, pvp, seed, mode, start, end);
    }
}
