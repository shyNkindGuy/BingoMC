package com.shyNkindguy.bingo.core.bus;

import java.util.Objects;

// Un jugador completó una casilla de la cartilla de su equipo en el segundo `second` de la partida.
public record CellCompleted(String team, String playerId, String playerName, String taskId, String taskName,
                            int second) implements DomainEvent {

    public CellCompleted {
        Objects.requireNonNull(team, "team");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(playerName, "playerName");
        Objects.requireNonNull(taskId, "taskId");
        Objects.requireNonNull(taskName, "taskName");
    }
}
