package com.shyNkindguy.bingo.core.stats;

import java.util.Map;

// Dónde viven las estadísticas entre reinicios (hoy un YAML; más adelante puede ser SQLite con la misma
// interfaz). Por id de jugador. Los dos lanzan StatsException.
public interface StatsRepository {

    Map<String, PlayerStats> load();

    void save(Map<String, PlayerStats> stats);
}
