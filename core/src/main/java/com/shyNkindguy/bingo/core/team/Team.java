package com.shyNkindguy.bingo.core.team;

import java.util.Locale;

// Un equipo: los de color se arman con /bingo team; los de un solo jugador (`solo`) se crean al empezar
// para quien no tiene equipo y se deshacen al terminar. Cada equipo juega con una cartilla.
public record Team(String id, String name, TeamColor color, boolean solo) {

    public static Team ofColor(TeamColor color) {
        return new Team(color.name().toLowerCase(Locale.ROOT), color.displayName(), color, false);
    }

    public static Team solo(String playerName, TeamColor color) {
        return new Team("solo:" + playerName, playerName, color, true);
    }

    // "Equipo Rojo" o el nombre del jugador si juega solo.
    public String label() {
        return solo ? name : "Equipo " + name;
    }
}
