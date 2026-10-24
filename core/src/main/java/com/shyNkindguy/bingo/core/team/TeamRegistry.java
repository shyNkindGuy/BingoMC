package com.shyNkindguy.bingo.core.team;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

// Quién está en qué equipo (P = cómo se identifica a un jugador: UUID en el plugin). Los métodos son
// synchronized porque el chat de equipo pregunta desde otro hilo.
public final class TeamRegistry<P> {

    private final Map<P, Team> teamOf = new LinkedHashMap<>();

    // Entra al equipo de ese color (si no existe, se crea) y sale del que tenía.
    public synchronized Team join(P player, TeamColor color) {
        Team team = Team.ofColor(color);
        teamOf.put(player, team);
        return team;
    }

    public synchronized Optional<Team> leave(P player) {
        return Optional.ofNullable(teamOf.remove(player));
    }

    public synchronized Optional<Team> teamOf(P player) {
        return Optional.ofNullable(teamOf.get(player));
    }

    // En el orden en que entraron.
    public synchronized List<P> members(Team team) {
        List<P> members = new ArrayList<>();
        teamOf.forEach((player, playerTeam) -> {
            if (playerTeam.equals(team)) members.add(player);
        });
        return members;
    }

    // Los equipos que tienen alguien, en el orden en que se armaron.
    public synchronized List<Team> teams() {
        return teamOf.values().stream().distinct().toList();
    }

    // Si el jugador no tiene equipo, juega solo: un equipo de 1 con un color que no use ningún equipo de
    // color (si están todos, blanco).
    public synchronized Team soloIfAlone(P player, String name) {
        Team current = teamOf.get(player);
        if (current != null) return current;

        Set<TeamColor> used = EnumSet.noneOf(TeamColor.class);
        int solos = 0;
        for (Team team : teams()) {
            if (team.solo()) solos++;
            else used.add(team.color());
        }
        List<TeamColor> free = new ArrayList<>();
        for (TeamColor color : TeamColor.values()) {
            if (!used.contains(color)) free.add(color);
        }
        TeamColor color = free.isEmpty() ? TeamColor.WHITE : free.get(solos % free.size());

        Team solo = Team.solo(name, color);
        teamOf.put(player, solo);
        return solo;
    }

    // Al terminar la partida: los que jugaban solos quedan sin equipo; los de color siguen armados.
    public synchronized void removeSolos() {
        teamOf.values().removeIf(Team::solo);
    }

    // Reparte a `players` al azar en `count` equipos de color (los primeros colores), lo más parejo posible.
    // Borra los equipos que hubiera.
    public synchronized List<Team> randomize(List<P> players, int count, Random random) {
        if (count < 2 || count > TeamColor.values().length) {
            throw new IllegalArgumentException("Se pueden armar de 2 a " + TeamColor.values().length + " equipos: " + count);
        }
        if (players.size() < count) {
            throw new IllegalArgumentException("Hay " + players.size() + " jugadores para " + count + " equipos");
        }
        List<P> shuffled = new ArrayList<>(players);
        Collections.shuffle(shuffled, random);
        teamOf.clear();
        for (int i = 0; i < shuffled.size(); i++) {
            teamOf.put(shuffled.get(i), Team.ofColor(TeamColor.values()[i % count]));
        }
        return teams();
    }
}
