package com.shyNkindguy.bingo.core.stats;

import com.shyNkindguy.bingo.core.bus.CellCompleted;
import com.shyNkindguy.bingo.core.bus.DomainEvents;
import com.shyNkindguy.bingo.core.bus.GameEnded;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

// Las estadísticas de todos, al día con los eventos de las partidas: mientras se juega junta las casillas de
// cada jugador y al terminar (GameEnded) las suma junto con la partida y la victoria. Una partida detenida no
// suma nada. Por id de jugador (en el plugin, el UUID en texto).
public final class StatsBook {
    private final Map<String, PlayerStats> byId = new LinkedHashMap<>();
    private final Map<String, Integer> pendingCells = new HashMap<>();

    public StatsBook(Map<String, PlayerStats> loaded) {
        byId.putAll(loaded);
    }

    public void subscribeTo(DomainEvents events) {
        events.subscribe(CellCompleted.class, this::onCellCompleted);
        events.subscribe(GameEnded.class, this::onGameEnded);
    }

    public void onCellCompleted(CellCompleted event) {
        pendingCells.merge(event.playerId(), 1, Integer::sum);
    }

    public void onGameEnded(GameEnded event) {
        if (!event.aborted()) {
            for (GameEnded.Participant player : event.players()) {
                PlayerStats before = byId.getOrDefault(player.id(), PlayerStats.empty(player.name()));
                int cells = pendingCells.getOrDefault(player.id(), 0);
                byId.put(player.id(), before.plusGame(player.name(), event.won(player), cells, event.seconds()));
            }
        }
        pendingCells.clear();
    }

    public Optional<PlayerStats> get(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    // Sin importar mayúsculas; si dos tuvieron el mismo nombre, el que jugó más.
    public Optional<PlayerStats> findByName(String name) {
        String wanted = name.toLowerCase(Locale.ROOT);
        PlayerStats found = null;
        for (PlayerStats stats : byId.values()) {
            if (stats.name().toLowerCase(Locale.ROOT).equals(wanted) && (found == null || stats.games() > found.games())) {
                found = stats;
            }
        }
        return Optional.ofNullable(found);
    }

    public List<String> names() {
        List<String> names = new ArrayList<>();
        for (PlayerStats stats : byId.values()) names.add(stats.name());
        return names;
    }

    // Una copia para guardar (los PlayerStats son inmutables).
    public Map<String, PlayerStats> snapshot() {
        return new LinkedHashMap<>(byId);
    }
}
