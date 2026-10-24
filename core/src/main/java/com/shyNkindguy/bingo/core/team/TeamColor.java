package com.shyNkindguy.bingo.core.team;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

// Los colores de equipo: cada equipo de /bingo team es un color. Se escriben en español o en inglés.
public enum TeamColor {
    RED("Rojo", "red"),
    BLUE("Azul", "blue"),
    GREEN("Verde", "green"),
    YELLOW("Amarillo", "yellow"),
    AQUA("Celeste", "aqua"),
    PURPLE("Morado", "purple"),
    ORANGE("Naranja", "orange"),
    PINK("Rosa", "pink"),
    WHITE("Blanco", "white"),
    GRAY("Gris", "gray");

    private final String displayName;
    private final String english;

    TeamColor(String displayName, String english) {
        this.displayName = displayName;
        this.english = english;
    }

    public String displayName() {
        return displayName;
    }

    // "rojo", "Rojo", "red" → RED; sin tildes ni mayúsculas.
    public static Optional<TeamColor> parse(String text) {
        String wanted = plain(text);
        for (TeamColor color : values()) {
            if (plain(color.displayName).equals(wanted) || color.english.equals(wanted)) return Optional.of(color);
        }
        return Optional.empty();
    }

    private static String plain(String text) {
        String decomposed = Normalizer.normalize(text.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "");
    }
}
