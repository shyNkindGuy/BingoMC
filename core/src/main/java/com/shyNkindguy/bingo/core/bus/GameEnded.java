package com.shyNkindguy.bingo.core.bus;

import com.shyNkindguy.bingo.core.win.WinMode;

import java.util.List;
import java.util.Objects;
import java.util.Set;

// Terminó una partida que duró `seconds` segundos de juego, con el modo `mode`. `aborted`: se detuvo con
// /bingo stop (no cuenta para las estadísticas ni los logros de partida). `winners`: los ids de los equipos
// que ganaron (vacío si nadie). `players`: todos los que jugaron, también los que se desconectaron.
public record GameEnded(int seconds, boolean aborted, WinMode mode, Set<String> winners, List<Participant> players)
        implements DomainEvent {

    // `deaths`: cuántas veces murió desde que empezó la partida (-1 si no se sabe: se desconectó).
    public record Participant(String id, String name, String team, int deaths) {
        public Participant {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(team, "team");
            if (deaths < -1) throw new IllegalArgumentException("deaths < -1: " + deaths);
        }
    }

    public GameEnded {
        if (seconds < 0) throw new IllegalArgumentException("seconds < 0: " + seconds);
        Objects.requireNonNull(mode, "mode");
        winners = Set.copyOf(winners);
        players = List.copyOf(players);
    }

    public boolean won(Participant player) {
        return winners.contains(player.team());
    }
}
