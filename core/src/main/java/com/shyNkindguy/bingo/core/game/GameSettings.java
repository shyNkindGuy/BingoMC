package com.shyNkindguy.bingo.core.game;

// Cómo se juega (config.yml; /bingo start puede cambiar la cartilla y la semilla): qué cartilla de
// cards.yml (null = su por_defecto), segundos de cuenta regresiva, minutos de partida (0 = sin límite), si
// hay PvP y una semilla fija (null = al azar en cada partida).
public record GameSettings(String card, int countdownSeconds, int durationMinutes, boolean pvp, Long seed) {

    public static final int MAX_COUNTDOWN = 60;
    public static final int MAX_DURATION = 600;
    public static final GameSettings DEFAULT = new GameSettings(null, 10, 20, true, null);

    public GameSettings {
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
        return new GameSettings(card, countdownSeconds, durationMinutes, pvp, seed);
    }

    public GameSettings withSeed(Long seed) {
        return new GameSettings(card, countdownSeconds, durationMinutes, pvp, seed);
    }
}
