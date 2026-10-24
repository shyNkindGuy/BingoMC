package com.shyNkindguy.bingo.core.bus;

import com.shyNkindguy.bingo.core.event.EventContext;

import java.util.Objects;

// Un jugador completó una casilla de la cartilla de su equipo en el segundo `second` de la partida, estando
// en `context` (para los logros con condiciones).
public record CellCompleted(String team, String playerId, String playerName, String taskId, String taskName,
                            int second, EventContext context) implements DomainEvent {

    public CellCompleted {
        Objects.requireNonNull(team, "team");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(playerName, "playerName");
        Objects.requireNonNull(taskId, "taskId");
        Objects.requireNonNull(taskName, "taskName");
        if (context == null) context = EventContext.NONE;
    }
}
