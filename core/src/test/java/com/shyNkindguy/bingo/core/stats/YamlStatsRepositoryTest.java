package com.shyNkindguy.bingo.core.stats;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlStatsRepositoryTest {

    @TempDir
    Path folder;

    private String loadError(String yaml) throws IOException {
        Files.writeString(folder.resolve("stats.yml"), yaml, StandardCharsets.UTF_8);
        return assertThrows(StatsException.class, () -> new YamlStatsRepository(folder).load()).getMessage();
    }

    @Test
    void sinArchivoEstaVacio() {
        assertTrue(new YamlStatsRepository(folder).load().isEmpty());
    }

    @Test
    void guardaYVuelveALeerLoMismo() throws IOException {
        Map<String, PlayerStats> stats = new LinkedHashMap<>();
        stats.put("0b7e3c1a-0000-4000-8000-000000000001", new PlayerStats("Ana", 3, 2, 14, 754));
        stats.put("0b7e3c1a-0000-4000-8000-000000000002", new PlayerStats("Ñandú_7", 1, 0, 0, -1));
        YamlStatsRepository repository = new YamlStatsRepository(folder);

        repository.save(stats);

        assertEquals(stats, new YamlStatsRepository(folder).load());
        String text = Files.readString(folder.resolve("stats.yml"), StandardCharsets.UTF_8);
        assertTrue(text.startsWith("# Estadísticas de AdaptiveBingo"), text);
        assertTrue(text.contains("mejor_tiempo: 754"), text);
        assertFalse(Files.exists(folder.resolve("stats.yml.tmp")));
    }

    @Test
    void guardarVacioTambienSeLee() {
        YamlStatsRepository repository = new YamlStatsRepository(folder);
        repository.save(Map.of());
        assertTrue(repository.load().isEmpty());
    }

    @Test
    void loQueFaltaEsCero() throws IOException {
        Files.writeString(folder.resolve("stats.yml"), "jugadores:\n  id-ana:\n    nombre: Ana\n", StandardCharsets.UTF_8);
        assertEquals(Map.of("id-ana", PlayerStats.empty("Ana")), new YamlStatsRepository(folder).load());
    }

    @Test
    void validaConMensajesQueDicenDonde() throws IOException {
        assertEquals("stats.yml, jugador id-ana: no conozco \"partida\" (se puede usar: casillas, mejor_tiempo, "
                + "nombre, partidas, victorias)", loadError("jugadores:\n  id-ana:\n    nombre: Ana\n    partida: 3\n"));
        assertEquals("stats.yml, jugador id-ana: falta nombre", loadError("jugadores:\n  id-ana:\n    partidas: 3\n"));
        assertEquals("stats.yml, jugador id-ana: tiene más victorias (4) que partidas (3)",
                loadError("jugadores:\n  id-ana:\n    nombre: Ana\n    partidas: 3\n    victorias: 4\n"));
        assertEquals("stats.yml, jugador id-ana: casillas: no puede ser negativo (vino -1)",
                loadError("jugadores:\n  id-ana:\n    nombre: Ana\n    casillas: -1\n"));
        assertEquals("stats.yml, jugador id-ana: partidas: tiene que ser un número entero (vino tres)",
                loadError("jugadores:\n  id-ana:\n    nombre: Ana\n    partidas: tres\n"));
        assertEquals("stats.yml, jugador id-ana: tiene mejor_tiempo pero ninguna victoria",
                loadError("jugadores:\n  id-ana:\n    nombre: Ana\n    partidas: 1\n    mejor_tiempo: 300\n"));
        assertEquals("stats.yml: version: solo se conoce la 1 (vino 2)", loadError("version: 2\n"));
        assertEquals("stats.yml: no conozco \"jugador\" (se puede usar: jugadores, version)", loadError("jugador: {}\n"));
        assertTrue(loadError("jugadores: [\n").startsWith("stats.yml: YAML mal escrito"));
    }

    @Test
    void avisaSiNoEstaEnUtf8() throws IOException {
        Files.write(folder.resolve("stats.yml"), "jugadores:\n  id-ana:\n    nombre: Ñ\n".getBytes(StandardCharsets.ISO_8859_1));
        assertTrue(assertThrows(StatsException.class, () -> new YamlStatsRepository(folder).load()).getMessage()
                .contains("UTF-8"));
    }
}
