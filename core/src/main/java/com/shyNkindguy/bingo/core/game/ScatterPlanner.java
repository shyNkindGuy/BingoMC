package com.shyNkindguy.bingo.core.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Dónde arranca cada equipo: repartidos en un círculo de radio `radius` alrededor del spawn, a la misma
// distancia unos de otros (el círculo gira al azar en cada partida). Devuelve desplazamientos (x, z) desde
// el centro; el plugin busca ahí un lugar seguro.
public final class ScatterPlanner {

    public record Offset(int x, int z) {
        public double distanceTo(Offset other) {
            return Math.hypot(x - other.x, z - other.z);
        }
    }

    private ScatterPlanner() {
    }

    public static List<Offset> positions(int teams, int radius, Random random) {
        List<Offset> offsets = new ArrayList<>();
        double rotation = random.nextDouble() * 2 * Math.PI;
        for (int i = 0; i < teams; i++) {
            offsets.add(at(rotation + 2 * Math.PI * i / teams, radius));
        }
        return offsets;
    }

    // Para un equipo que llega con la partida empezada: un punto del círculo al azar.
    public static Offset extra(int radius, Random random) {
        return at(random.nextDouble() * 2 * Math.PI, radius);
    }

    private static Offset at(double angle, int radius) {
        return new Offset((int) Math.round(Math.cos(angle) * radius), (int) Math.round(Math.sin(angle) * radius));
    }
}
