package com.shyNkindguy.bingo.core.win;

import com.shyNkindguy.bingo.core.card.BingoCard;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Cómo se gana (config.yml `modo`): N líneas, carta llena, N casillas, lockout (la casilla es del primer
// equipo que la completa; gana quien tenga más de la mitad) o por tiempo (nadie gana antes del final). En
// todos, al acabarse el tiempo gana quien tenga más casillas (TimeLimitWinners).
public sealed interface WinMode {

    boolean wins(BingoCard card);

    // "1 línea", "carta llena", "12 casillas", "lockout", "por tiempo".
    String description();

    // null si el modo se puede jugar con una cartilla de `size`×`size`; si no, por qué.
    default String problemWith(int size) {
        return null;
    }

    default boolean lockout() {
        return false;
    }

    record Lines(int count) implements WinMode {
        public Lines {
            if (count < 1) throw new IllegalArgumentException("Hace falta al menos 1 línea: " + count);
        }

        @Override
        public boolean wins(BingoCard card) {
            return WinConditionChecker.countLines(card) >= count;
        }

        @Override
        public String description() {
            return count == 1 ? "1 línea" : count + " líneas";
        }

        @Override
        public String problemWith(int size) {
            int lines = 2 * size + 2;
            return count > lines ? "una cartilla de " + size + "×" + size + " tiene " + lines + " líneas" : null;
        }
    }

    record Blackout() implements WinMode {
        @Override
        public boolean wins(BingoCard card) {
            return card.countCompleted() == card.getSize() * card.getSize();
        }

        @Override
        public String description() {
            return "carta llena";
        }
    }

    record CompleteX(int count) implements WinMode {
        public CompleteX {
            if (count < 1) throw new IllegalArgumentException("Hace falta al menos 1 casilla: " + count);
        }

        @Override
        public boolean wins(BingoCard card) {
            return card.countCompleted() >= count;
        }

        @Override
        public String description() {
            return count == 1 ? "1 casilla" : count + " casillas";
        }

        @Override
        public String problemWith(int size) {
            int cells = size * size;
            return count > cells ? "una cartilla de " + size + "×" + size + " tiene " + cells + " casillas" : null;
        }
    }

    record Lockout() implements WinMode {
        @Override
        public boolean wins(BingoCard card) {
            int cells = card.getSize() * card.getSize();
            return card.countCompleted() > cells / 2;
        }

        @Override
        public String description() {
            return "lockout";
        }

        @Override
        public boolean lockout() {
            return true;
        }
    }

    record TimeOnly() implements WinMode {
        @Override
        public boolean wins(BingoCard card) {
            return false;
        }

        @Override
        public String description() {
            return "por tiempo";
        }
    }

    WinMode DEFAULT = new Lines(1);

    Pattern COUNTED = Pattern.compile("(\\d+)\\s*(lineas?|casillas?)");

    // "linea", "2 lineas", "carta llena", "10 casillas", "lockout", "tiempo" (sin importar tildes ni
    // mayúsculas; también en inglés: line, blackout, lockout, time). IllegalArgumentException si no es
    // ninguno.
    static WinMode parse(String text) {
        String plain = Normalizer.normalize(text.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replace('_', ' ').replaceAll("\\s+", " ");
        switch (plain) {
            case "linea", "line", "bingo" -> {
                return new Lines(1);
            }
            case "carta llena", "blackout" -> {
                return new Blackout();
            }
            case "lockout" -> {
                return new Lockout();
            }
            case "tiempo", "por tiempo", "time" -> {
                return new TimeOnly();
            }
            default -> {
            }
        }
        Matcher matcher = COUNTED.matcher(plain);
        if (matcher.matches()) {
            int count = Integer.parseInt(matcher.group(1));
            if (count < 1) throw new IllegalArgumentException("tiene que ser al menos 1 (vino " + text + ")");
            return matcher.group(2).startsWith("linea") ? new Lines(count) : new CompleteX(count);
        }
        throw new IllegalArgumentException("\"" + text + "\" no es un modo (se puede usar: linea, 2 lineas, "
                + "carta llena, 10 casillas, lockout, tiempo)");
    }
}
