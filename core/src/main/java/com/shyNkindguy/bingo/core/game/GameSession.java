package com.shyNkindguy.bingo.core.game;

// La máquina de estados de una partida, sin Bukkit: el plugin llama a tick() una vez por segundo y hace lo
// que le dice la señal (mostrar la cuenta, arrancar, cortar por tiempo).
public final class GameSession {

    public enum Signal {
        NONE,       // nada que hacer
        COUNTDOWN,  // un segundo menos de cuenta regresiva: countdownLeft() dice cuántos quedan
        STARTED,    // terminó la cuenta: la partida está en curso
        TIME_UP     // se acabó el tiempo: hay que terminarla (finish)
    }

    private Phase phase = Phase.LOBBY;
    private int countdownLeft;
    private int elapsedSeconds;
    private int durationSeconds;

    public Phase phase() {
        return phase;
    }

    // En cuenta regresiva, en curso o en pausa.
    public boolean inGame() {
        return phase == Phase.COUNTDOWN || phase == Phase.RUNNING || phase == Phase.PAUSED;
    }

    // En curso → en pausa (el tiempo deja de correr). false si no estaba en curso.
    public boolean pause() {
        if (phase != Phase.RUNNING) return false;
        phase = Phase.PAUSED;
        return true;
    }

    // En pausa → en curso. false si no estaba en pausa.
    public boolean resume() {
        if (phase != Phase.PAUSED) return false;
        phase = Phase.RUNNING;
        return true;
    }

    public int countdownLeft() {
        return countdownLeft;
    }

    public int elapsedSeconds() {
        return elapsedSeconds;
    }

    // 0 = sin límite.
    public int durationSeconds() {
        return durationSeconds;
    }

    // Empieza una partida: cuenta regresiva de `countdownSeconds` (con 0 arranca en el acto: STARTED) y
    // `durationSeconds` de juego (0 = sin límite). Desde LOBBY o ENDED; si ya hay una, no hace nada (NONE).
    public Signal begin(int countdownSeconds, int durationSeconds) {
        if (inGame()) return Signal.NONE;
        if (countdownSeconds < 0 || durationSeconds < 0) {
            throw new IllegalArgumentException("Tiempos negativos: " + countdownSeconds + ", " + durationSeconds);
        }
        this.durationSeconds = durationSeconds;
        this.elapsedSeconds = 0;
        this.countdownLeft = countdownSeconds;
        if (countdownSeconds == 0) {
            phase = Phase.RUNNING;
            return Signal.STARTED;
        }
        phase = Phase.COUNTDOWN;
        return Signal.COUNTDOWN;
    }

    public Signal tick() {
        switch (phase) {
            case COUNTDOWN -> {
                countdownLeft--;
                if (countdownLeft > 0) return Signal.COUNTDOWN;
                phase = Phase.RUNNING;
                return Signal.STARTED;
            }
            case RUNNING -> {
                elapsedSeconds++;
                return durationSeconds > 0 && elapsedSeconds >= durationSeconds ? Signal.TIME_UP : Signal.NONE;
            }
            default -> {
                return Signal.NONE;
            }
        }
    }

    // Bingo, tiempo o /bingo stop (también durante la cuenta regresiva o en pausa). false si no había partida.
    public boolean finish() {
        if (!inGame()) return false;
        phase = Phase.ENDED;
        return true;
    }
}
