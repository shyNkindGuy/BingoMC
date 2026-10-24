package com.shyNkindguy.bingo.core.item;

// Qué hace un ítem especial al usarlo (clic derecho).
public sealed interface ItemEffect {

    // Si se gasta uno al usarlo (la brújula no: se vuelve a usar para apuntar de nuevo).
    boolean consumed();

    // cambiar_casilla: cambia una casilla sin completar por otra tarea del mismo nivel.
    record Reroll() implements ItemEffect {
        @Override
        public boolean consumed() {
            return true;
        }
    }

    // brujula_bioma: apunta al bioma más cercano (hasta `radius` bloques) que pida una casilla pendiente.
    record BiomeCompass(int radius) implements ItemEffect {
        public static final int MIN_RADIUS = 100;
        public static final int MAX_RADIUS = 5000;

        public BiomeCompass {
            if (radius < MIN_RADIUS || radius > MAX_RADIUS) throw new IllegalArgumentException("radio fuera de rango: " + radius);
        }

        @Override
        public boolean consumed() {
            return false;
        }
    }

    // escudo_pvp: ningún jugador le puede pegar durante `seconds` segundos de partida.
    record PvpShield(int seconds) implements ItemEffect {
        public static final int MAX_SECONDS = 600;

        public PvpShield {
            if (seconds < 1 || seconds > MAX_SECONDS) throw new IllegalArgumentException("segundos fuera de rango: " + seconds);
        }

        @Override
        public boolean consumed() {
            return true;
        }
    }
}
