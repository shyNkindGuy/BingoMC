package com.shyNkindguy.bingo.core.stats;

// Error al leer o guardar las estadísticas. El mensaje va en español y dice el archivo y el jugador.
public class StatsException extends RuntimeException {

    public StatsException(String message) {
        super(message);
    }

    public StatsException(String message, Throwable cause) {
        super(message, cause);
    }
}
