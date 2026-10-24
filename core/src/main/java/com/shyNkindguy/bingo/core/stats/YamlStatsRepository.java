package com.shyNkindguy.bingo.core.stats;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

// stats.yml: las estadísticas de cada jugador por id. Las escribe el plugin cada vez que termina una
// partida; si se editan a mano, que sea con el servidor apagado. Al leer se valida todo (claves desconocidas
// = error) y al guardar se escribe un archivo aparte que después reemplaza al de antes, para no dejarlo a
// medias si el servidor se apaga justo.
public final class YamlStatsRepository implements StatsRepository {
    public static final String FILE = "stats.yml";
    private static final int VERSION = 1;
    private static final Set<String> TOP_KEYS = Set.of("version", "jugadores");
    private static final Set<String> PLAYER_KEYS = Set.of("nombre", "partidas", "victorias", "casillas", "mejor_tiempo");
    private static final String HEADER = """
            # Estadísticas de AdaptiveBingo por jugador (UUID). Las escribe el plugin cada vez que termina una
            # partida (las detenidas con /bingo stop no cuentan). Si las editas, que sea con el servidor apagado.
            # mejor_tiempo: segundos de juego de su victoria más rápida.
            """;

    private final Path file;

    public YamlStatsRepository(Path folder) {
        this.file = folder.resolve(FILE);
    }

    // Sin archivo, vacío.
    @Override
    public synchronized Map<String, PlayerStats> load() {
        if (!Files.exists(file)) return new LinkedHashMap<>();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parse(new Yaml(new SafeConstructor(new LoaderOptions())).load(reader));
        } catch (YAMLException e) {
            if (e.getCause() instanceof CharacterCodingException) throw notUtf8(e);
            throw new StatsException(FILE + ": YAML mal escrito: " + e.getMessage(), e);
        } catch (CharacterCodingException e) {
            throw notUtf8(e);
        } catch (IOException e) {
            throw new StatsException(FILE + ": no se pudo leer: " + e.getMessage(), e);
        }
    }

    static Map<String, PlayerStats> parse(Object root) {
        Map<String, PlayerStats> result = new LinkedHashMap<>();
        if (root == null) return result;
        Map<String, Object> top = map(root, FILE);
        unknownKeys(top, TOP_KEYS, FILE);
        if (top.containsKey("version")) {
            int version = nonNegative(top.get("version"), FILE + ": version");
            if (version != VERSION) throw error(FILE + ": version", "solo se conoce la " + VERSION + " (vino " + version + ")");
        }
        Object players = top.get("jugadores");
        if (players == null) return result;

        for (Map.Entry<String, Object> entry : map(players, FILE + ": jugadores").entrySet()) {
            String where = FILE + ", jugador " + entry.getKey();
            Map<String, Object> fields = map(entry.getValue(), where);
            unknownKeys(fields, PLAYER_KEYS, where);
            if (!(fields.get("nombre") instanceof String name) || name.isBlank()) throw error(where, "falta nombre");
            int games = count(fields, "partidas", where);
            int wins = count(fields, "victorias", where);
            int cells = count(fields, "casillas", where);
            if (wins > games) throw error(where, "tiene más victorias (" + wins + ") que partidas (" + games + ")");
            int best = -1;
            if (fields.containsKey("mejor_tiempo")) {
                best = nonNegative(fields.get("mejor_tiempo"), where + ": mejor_tiempo");
                if (wins == 0) throw error(where, "tiene mejor_tiempo pero ninguna victoria");
            }
            result.put(entry.getKey(), new PlayerStats(name, games, wins, cells, best));
        }
        return result;
    }

    @Override
    public synchronized void save(Map<String, PlayerStats> stats) {
        Map<String, Object> players = new LinkedHashMap<>();
        for (Map.Entry<String, PlayerStats> entry : stats.entrySet()) {
            PlayerStats player = entry.getValue();
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("nombre", player.name());
            fields.put("partidas", player.games());
            fields.put("victorias", player.wins());
            fields.put("casillas", player.cells());
            if (player.hasWon()) fields.put("mejor_tiempo", player.bestWinSeconds());
            players.put(entry.getKey(), fields);
        }
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("version", VERSION);
        root.put("jugadores", players);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setAllowUnicode(true);
        String text = HEADER + new Yaml(options).dump(root);

        Path temp = file.resolveSibling(FILE + ".tmp");
        try {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
            Files.writeString(temp, text, StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StatsException(FILE + ": no se pudo guardar: " + e.getMessage(), e);
        }
    }

    private static int count(Map<String, Object> fields, String key, String where) {
        return fields.containsKey(key) ? nonNegative(fields.get(key), where + ": " + key) : 0;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object raw, String where) {
        if (raw == null) throw error(where, "está vacío");
        if (!(raw instanceof Map<?, ?> map)) throw error(where, "tiene que ser un mapa (clave: valor)");
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) result.put(String.valueOf(entry.getKey()), entry.getValue());
        return result;
    }

    private static void unknownKeys(Map<String, Object> map, Set<String> allowed, String where) {
        for (String key : map.keySet()) {
            if (!allowed.contains(key)) {
                throw error(where, "no conozco \"" + key + "\" (se puede usar: " + String.join(", ", new TreeSet<>(allowed)) + ")");
            }
        }
    }

    private static int nonNegative(Object raw, String where) {
        if (raw instanceof Integer || raw instanceof Long) {
            long value = ((Number) raw).longValue();
            if (value < 0) throw error(where, "no puede ser negativo (vino " + value + ")");
            if (value <= Integer.MAX_VALUE) return (int) value;
        }
        throw error(where, "tiene que ser un número entero (vino " + raw + ")");
    }

    private static StatsException error(String where, String problem) {
        return new StatsException(where + ": " + problem);
    }

    private static StatsException notUtf8(Exception cause) {
        return new StatsException(FILE + ": no está guardado en UTF-8 (guárdalo como UTF-8 y reinicia)", cause);
    }
}
