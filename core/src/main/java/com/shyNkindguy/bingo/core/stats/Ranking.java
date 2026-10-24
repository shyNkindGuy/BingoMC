package com.shyNkindguy.bingo.core.stats;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

// /bingo top: los mejores por victorias, partidas, casillas o victoria más rápida (solo los que ya ganaron).
// Quedan afuera los que todavía no terminaron una partida. Si empatan: más victorias, menos partidas y el
// nombre, en ese orden, así el puesto no cambia de una vez a otra.
public final class Ranking {

    public enum Metric {
        WINS("victorias"), GAMES("partidas"), CELLS("casillas"), FASTEST("tiempo");

        private final String key;

        Metric(String key) {
            this.key = key;
        }

        // Cómo se escribe en /bingo top y en los placeholders.
        public String key() {
            return key;
        }

        public int value(PlayerStats stats) {
            return switch (this) {
                case WINS -> stats.wins();
                case GAMES -> stats.games();
                case CELLS -> stats.cells();
                case FASTEST -> stats.bestWinSeconds();
            };
        }

        // "victorias", "Partidas", "tiempo" (o en inglés: wins, games, cells, time).
        public static Optional<Metric> parse(String text) {
            String plain = Normalizer.normalize(text.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");
            return switch (plain) {
                case "victorias", "wins" -> Optional.of(WINS);
                case "partidas", "games" -> Optional.of(GAMES);
                case "casillas", "cells" -> Optional.of(CELLS);
                case "tiempo", "time" -> Optional.of(FASTEST);
                default -> Optional.empty();
            };
        }

        public static List<String> keys() {
            List<String> keys = new ArrayList<>();
            for (Metric metric : values()) keys.add(metric.key);
            return keys;
        }
    }

    // `position` empieza en 1.
    public record Entry(int position, String id, PlayerStats stats) {
    }

    private Ranking() {
    }

    public static List<Entry> top(Map<String, PlayerStats> all, Metric metric, int limit) {
        Comparator<Map.Entry<String, PlayerStats>> byMetric = Comparator.comparingInt(entry -> metric.value(entry.getValue()));
        if (metric != Metric.FASTEST) byMetric = byMetric.reversed();
        Comparator<Map.Entry<String, PlayerStats>> order = byMetric
                .thenComparing(entry -> entry.getValue().wins(), Comparator.reverseOrder())
                .thenComparingInt(entry -> entry.getValue().games())
                .thenComparing(entry -> entry.getValue().name().toLowerCase(Locale.ROOT))
                .thenComparing(Map.Entry::getKey);

        List<Map.Entry<String, PlayerStats>> sorted = new ArrayList<>();
        for (Map.Entry<String, PlayerStats> entry : all.entrySet()) {
            PlayerStats stats = entry.getValue();
            if (stats.games() == 0 || (metric == Metric.FASTEST && !stats.hasWon())) continue;
            sorted.add(entry);
        }
        sorted.sort(order);

        List<Entry> top = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, sorted.size()); i++) {
            top.add(new Entry(i + 1, sorted.get(i).getKey(), sorted.get(i).getValue()));
        }
        return top;
    }
}
