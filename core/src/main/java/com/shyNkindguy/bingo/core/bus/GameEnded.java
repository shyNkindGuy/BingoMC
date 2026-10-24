package com.shyNkindguy.bingo.core.bus;

import java.util.List;
import java.util.Objects;
import java.util.Set;

// Terminó una partida que duró `seconds` segundos de juego. `aborted`: se detuvo con /bingo stop (no
// cuenta para las estadísticas). `winners`: los ids de los equipos que ganaron (vacío si nadie). `players`:
// todos los que jugaron, también los que se desconectaron.
public record GameEnded(int seconds, boolean aborted, Set<String> winners, List<Participant> players)
        implements DomainEvent {

    public record Participant(String id, String name, String team) {
        public Participant {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(team, "team");
        }
    }

    public GameEnded {
        if (seconds < 0) throw new IllegalArgumentException("seconds < 0: " + seconds);
        winners = Set.copyOf(winners);
        players = List.copyOf(players);
    }

    public boolean won(Participant player) {
        return winners.contains(player.team());
    }
}
