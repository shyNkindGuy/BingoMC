package com.shyNkindguy.bingo.core.stats;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RankingTest {

    private static Map<String, PlayerStats> jugadores() {
        Map<String, PlayerStats> all = new LinkedHashMap<>();
        all.put("id-ana", new PlayerStats("Ana", 10, 4, 60, 700));
        all.put("id-beto", new PlayerStats("Beto", 6, 4, 40, 500));
        all.put("id-carla", new PlayerStats("carla", 12, 1, 90, 900));
        all.put("id-dani", new PlayerStats("Dani", 3, 0, 9, -1));
        all.put("id-eva", new PlayerStats("Eva", 0, 0, 0, -1, Set.of("madrugador")));
        return all;
    }

    private static List<String> nombres(List<Ranking.Entry> top) {
        return top.stream().map(entry -> entry.position() + ":" + entry.stats().name()).toList();
    }

    @Test
    void porVictoriasDesempataQuienJugoMenos() {
        assertEquals(List.of("1:Beto", "2:Ana", "3:carla", "4:Dani"), nombres(Ranking.top(jugadores(), Ranking.Metric.WINS, 10)));
        assertEquals(List.of("1:Beto", "2:Ana"), nombres(Ranking.top(jugadores(), Ranking.Metric.WINS, 2)));
    }

    @Test
    void porPartidasCasillasYTiempo() {
        assertEquals(List.of("1:carla", "2:Ana", "3:Beto", "4:Dani"), nombres(Ranking.top(jugadores(), Ranking.Metric.GAMES, 10)));
        assertEquals(List.of("1:carla", "2:Ana", "3:Beto", "4:Dani"), nombres(Ranking.top(jugadores(), Ranking.Metric.CELLS, 10)));
        assertEquals(List.of("1:Beto", "2:Ana", "3:carla"), nombres(Ranking.top(jugadores(), Ranking.Metric.FASTEST, 10)));
    }

    @Test
    void lasMetricasSeEscribenEnEspanolOIngles() {
        assertEquals(Optional.of(Ranking.Metric.WINS), Ranking.Metric.parse("Victorias"));
        assertEquals(Optional.of(Ranking.Metric.FASTEST), Ranking.Metric.parse("time"));
        assertEquals(Optional.empty(), Ranking.Metric.parse("kills"));
        assertEquals(List.of("victorias", "partidas", "casillas", "tiempo"), Ranking.Metric.keys());
    }
}
