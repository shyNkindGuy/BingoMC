package com.shyNkindguy.bingo.core.stats;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;

// Los placeholders %adaptivebingo_<clave>% de PlaceholderAPI que salen de las estadísticas (los de la
// partida en curso los pone el plugin):
//   partidas, victorias, casillas, porcentaje, mejor_tiempo, logros  (del jugador)
//   top_<métrica>_<n> (nombre del puesto n) y top_<métrica>_<n>_valor  (métrica: victorias, partidas,
//   casillas o tiempo)
// null si la clave no es de estas (PlaceholderAPI la deja como está).
public final class Placeholders {
    private static final int MAX_TOP = 100;

    private Placeholders() {
    }

    // `mine`: las del jugador (null si no tiene); `clock`: cómo se escribe un tiempo; `none`: lo que va si no hay
    // valor (sin victorias, puesto vacío).
    public static String resolve(String params, PlayerStats mine, Map<String, PlayerStats> all, IntFunction<String> clock,
                                 String none) {
        PlayerStats stats = mine != null ? mine : PlayerStats.empty("");
        switch (params) {
            case "partidas" -> {
                return String.valueOf(stats.games());
            }
            case "victorias" -> {
                return String.valueOf(stats.wins());
            }
            case "casillas" -> {
                return String.valueOf(stats.cells());
            }
            case "porcentaje" -> {
                return String.valueOf(stats.winPercent());
            }
            case "mejor_tiempo" -> {
                return stats.hasWon() ? clock.apply(stats.bestWinSeconds()) : none;
            }
            case "logros" -> {
                return String.valueOf(stats.achievements().size());
            }
            default -> {
                return top(params, all, clock, none);
            }
        }
    }

    // top_victorias_1 / top_victorias_1_valor
    private static String top(String params, Map<String, PlayerStats> all, IntFunction<String> clock, String none) {
        String[] parts = params.split("_");
        if (parts.length < 3 || parts.length > 4 || !parts[0].equals("top")) return null;
        if (parts.length == 4 && !parts[3].equals("valor")) return null;
        Optional<Ranking.Metric> metric = Ranking.Metric.parse(parts[1]);
        int position;
        try {
            position = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return null;
        }
        if (metric.isEmpty() || position < 1 || position > MAX_TOP) return null;

        List<Ranking.Entry> top = Ranking.top(all, metric.get(), position);
        if (top.size() < position) return none;
        PlayerStats stats = top.get(position - 1).stats();
        if (parts.length == 3) return stats.name();
        int value = metric.get().value(stats);
        return metric.get() == Ranking.Metric.FASTEST ? clock.apply(value) : String.valueOf(value);
    }
}
