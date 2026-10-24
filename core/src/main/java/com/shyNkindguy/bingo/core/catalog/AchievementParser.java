package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.achievement.Achievement;
import com.shyNkindguy.bingo.core.achievement.AchievementRule;
import com.shyNkindguy.bingo.core.achievement.CellRule;
import com.shyNkindguy.bingo.core.achievement.GameRule;
import com.shyNkindguy.bingo.core.condition.Condition;
import com.shyNkindguy.bingo.core.task.Task;
import com.shyNkindguy.bingo.core.win.WinMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.shyNkindguy.bingo.core.catalog.CatalogParser.checkKeys;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.conditions;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.error;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.key;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.list;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.map;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.nonNegative;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.positive;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.required;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.text;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.yesNo;

// achievements.yml: `logros:` es una lista; cada logro tiene id, nombre, descripcion, icono opcional, `al`
// (partida, por defecto, o casilla) y lo que pide según `al`. Todo se valida con "archivo, logro N: problema".
final class AchievementParser {
    private static final Pattern ID = Pattern.compile("[a-z0-9_]+");
    private static final int SECONDS_PER_MINUTE = 60;
    private static final Set<String> TOP_KEYS = Set.of("logros");
    private static final Set<String> COMMON_KEYS = Set.of("id", "nombre", "descripcion", "icono", "al");
    private static final Set<String> GAME_KEYS = with(COMMON_KEYS, "gana", "en_menos_de", "modo", "casillas",
            "sin_morir", "partidas", "victorias", "casillas_total");
    private static final Set<String> CELL_KEYS = with(COMMON_KEYS, "tarea", "condiciones", "en_menos_de");

    private AchievementParser() {
    }

    // `catalog`: para revisar que las tareas nombradas existan.
    static List<Achievement> parse(String file, Object root, Catalog catalog) {
        if (root == null) return List.of();
        Map<String, Object> top = map(root, file);
        checkKeys(top, TOP_KEYS, file);
        if (top.get("logros") == null) return List.of();

        Set<String> taskIds = new HashSet<>();
        for (Task task : catalog.tasks()) taskIds.add(task.id());
        List<?> entries = list(top.get("logros"), file + ": logros");
        List<Achievement> achievements = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < entries.size(); i++) {
            String where = file + ", logro " + (i + 1);
            Achievement achievement = achievement(map(entries.get(i), where), where, taskIds);
            if (!ids.add(achievement.id())) throw error(where, "el id " + achievement.id() + " ya está en otro logro");
            achievements.add(achievement);
        }
        return List.copyOf(achievements);
    }

    private static Achievement achievement(Map<String, Object> map, String where, Set<String> taskIds) {
        String id = text(required(map, "id", where), where + ": id");
        if (!ID.matcher(id).matches()) throw error(where + ": id", "solo minúsculas, números y _ (vino " + id + ")");
        String name = text(required(map, "nombre", where), where + ": nombre");
        String description = text(required(map, "descripcion", where), where + ": descripcion");
        String icon = map.containsKey("icono") ? key(map.get("icono"), where + ": icono") : null;

        String when = map.containsKey("al") ? text(map.get("al"), where + ": al").toLowerCase(Locale.ROOT) : "partida";
        AchievementRule rule = switch (when) {
            case "partida" -> {
                checkKeys(map, GAME_KEYS, where);
                yield gameRule(map, where);
            }
            case "casilla" -> {
                checkKeys(map, CELL_KEYS, where);
                yield cellRule(map, where, taskIds);
            }
            default -> throw error(where + ": al", "tiene que ser partida o casilla (vino " + map.get("al") + ")");
        };
        return new Achievement(id, name, description, icon, rule);
    }

    private static GameRule gameRule(Map<String, Object> map, String where) {
        Boolean won = map.containsKey("gana") ? yesNo(map.get("gana"), where + ": gana") : null;
        WinMode mode = null;
        if (map.containsKey("modo")) {
            try {
                mode = WinMode.parse(text(map.get("modo"), where + ": modo"));
            } catch (IllegalArgumentException e) {
                throw error(where + ": modo", e.getMessage());
            }
        }
        boolean noDeath = false;
        if (map.containsKey("sin_morir")) {
            noDeath = yesNo(map.get("sin_morir"), where + ": sin_morir");
            if (!noDeath) throw error(where + ": sin_morir", "se pone sin_morir: true (o se quita)");
        }
        return new GameRule(won, minutes(map, where), mode, count(map, "casillas", where), noDeath,
                count(map, "partidas", where), count(map, "victorias", where), count(map, "casillas_total", where));
    }

    private static CellRule cellRule(Map<String, Object> map, String where, Set<String> taskIds) {
        String task = null;
        if (map.containsKey("tarea")) {
            task = text(map.get("tarea"), where + ": tarea");
            if (!taskIds.contains(task)) throw error(where + ": tarea", "no hay ninguna tarea " + task + " en tasks/");
        }
        Condition condition = map.containsKey("condiciones") ? conditions(map.get("condiciones"), where + ": condiciones") : null;
        return new CellRule(task, condition, minutes(map, where));
    }

    // en_menos_de: N minutos → segundos (0 = sin límite).
    private static int minutes(Map<String, Object> map, String where) {
        return map.containsKey("en_menos_de") ? positive(map.get("en_menos_de"), where + ": en_menos_de") * SECONDS_PER_MINUTE : 0;
    }

    private static int count(Map<String, Object> map, String key, String where) {
        return map.containsKey(key) ? nonNegative(map.get(key), where + ": " + key) : 0;
    }

    private static Set<String> with(Set<String> base, String... more) {
        Set<String> all = new HashSet<>(base);
        all.addAll(List.of(more));
        return Set.copyOf(all);
    }
}
