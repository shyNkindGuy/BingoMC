package com.shyNkindguy.bingo.core.team;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamRegistryTest {

    @Test
    void unirseYSalir() {
        TeamRegistry<String> equipos = new TeamRegistry<>();
        Team rojo = equipos.join("ana", TeamColor.RED);
        equipos.join("beto", TeamColor.RED);
        equipos.join("caro", TeamColor.BLUE);

        assertEquals("Equipo Rojo", rojo.label());
        assertEquals(List.of("ana", "beto"), equipos.members(rojo));
        assertEquals(List.of(Team.ofColor(TeamColor.RED), Team.ofColor(TeamColor.BLUE)), equipos.teams());

        // cambiar de equipo saca del anterior
        equipos.join("beto", TeamColor.BLUE);
        assertEquals(List.of("ana"), equipos.members(rojo));

        assertEquals(Optional.of(rojo), equipos.leave("ana"));
        assertEquals(List.of(Team.ofColor(TeamColor.BLUE)), equipos.teams());
        assertEquals(Optional.empty(), equipos.teamOf("ana"));
    }

    @Test
    void quienNoTieneEquipoJuegaSoloConUnColorLibre() {
        TeamRegistry<String> equipos = new TeamRegistry<>();
        equipos.join("ana", TeamColor.RED);

        Team beto = equipos.soloIfAlone("beto", "Beto");
        Team caro = equipos.soloIfAlone("caro", "Caro");

        assertTrue(beto.solo());
        assertEquals("Beto", beto.label());
        assertNotEquals(TeamColor.RED, beto.color());
        assertNotEquals(beto.color(), caro.color());
        // a quien ya tiene equipo no se lo cambia
        assertEquals(Team.ofColor(TeamColor.RED), equipos.soloIfAlone("ana", "Ana"));
    }

    @Test
    void alTerminarSeDeshacenLosSolos() {
        TeamRegistry<String> equipos = new TeamRegistry<>();
        equipos.join("ana", TeamColor.RED);
        equipos.soloIfAlone("beto", "Beto");

        equipos.removeSolos();

        assertEquals(List.of(Team.ofColor(TeamColor.RED)), equipos.teams());
        assertEquals(Optional.empty(), equipos.teamOf("beto"));
    }

    @Test
    void alAzarQuedanParejos() {
        TeamRegistry<String> equipos = new TeamRegistry<>();
        equipos.join("zoe", TeamColor.PINK);
        List<String> jugadores = List.of("a", "b", "c", "d", "e", "f", "g");

        List<Team> armados = equipos.randomize(jugadores, 3, new Random(1));

        assertEquals(List.of(TeamColor.RED, TeamColor.BLUE, TeamColor.GREEN),
                armados.stream().map(Team::color).sorted().toList());
        Map<Team, Integer> tamanos = new HashMap<>();
        for (String jugador : jugadores) tamanos.merge(equipos.teamOf(jugador).orElseThrow(), 1, Integer::sum);
        assertTrue(tamanos.values().stream().allMatch(n -> n == 2 || n == 3), tamanos.toString());
        assertEquals(Optional.empty(), equipos.teamOf("zoe"));

        assertThrows(IllegalArgumentException.class, () -> equipos.randomize(jugadores, 1, new Random()));
        assertThrows(IllegalArgumentException.class, () -> equipos.randomize(List.of("a"), 2, new Random()));
    }

    @Test
    void losColoresSeEscribenEnEspanolOIngles() {
        assertEquals(Optional.of(TeamColor.RED), TeamColor.parse("rojo"));
        assertEquals(Optional.of(TeamColor.RED), TeamColor.parse("Red"));
        assertEquals(Optional.of(TeamColor.PURPLE), TeamColor.parse("MORADO"));
        assertEquals(Optional.empty(), TeamColor.parse("fucsia"));
    }
}
