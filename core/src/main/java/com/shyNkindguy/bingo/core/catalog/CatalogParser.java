package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

// Pasa lo que devuelve SnakeYAML (mapas, listas, textos y números) a listas, cartillas y catálogo.
// Valida todo al leer, así un error se ve en /bingo reload y no a mitad de partida. Los mensajes dicen
// dónde está el problema: "tasks/basicos.yml, tarea 3: falta nombre".
public final class CatalogParser {

    private static final Pattern KEY = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    private static final String MINECRAFT = "minecraft:";

    private static final Set<String> LIST_KEYS = Set.of("lista", "tier", "tareas");
    private static final Set<String> ITEM_KEYS = Set.of("tipo", "id", "nombre", "item", "cantidad");
    private static final Set<String> CARDS_KEYS = Set.of("por_defecto", "cartillas");
    private static final Set<String> CARD_KEYS = Set.of("tamaño", "tamano", "listas");
    private static final Set<String> QUOTA_KEYS = Set.of("min", "max");

    private CatalogParser() {
    }

    // Un archivo de tasks/. `fileName` ("tasks/basicos.yml") va en los mensajes y da el nombre de la
    // lista si el archivo no trae `lista`.
    public static TaskList parseTaskList(String fileName, Object root) {
        Map<String, Object> map = map(root, fileName);
        checkKeys(map, LIST_KEYS, fileName);

        String name = map.containsKey("lista")
                ? text(map.get("lista"), fileName + ": lista")
                : baseName(fileName);
        Tier tier = tier(required(map, "tier", fileName), fileName + ": tier");

        List<?> rawTasks = list(required(map, "tareas", fileName), fileName + ": tareas");
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < rawTasks.size(); i++) {
            tasks.add(parseTask(rawTasks.get(i), fileName + ", tarea " + (i + 1)));
        }
        return new TaskList(name, tier, tasks);
    }

    public static Task parseTask(Object raw, String where) {
        Map<String, Object> map = map(raw, where);
        String type = map.containsKey("tipo")
                ? text(map.get("tipo"), where + ": tipo").toLowerCase(Locale.ROOT)
                : "item";
        return switch (type) {
            case "item" -> parseItem(map, where);
            default -> throw error(where, "el tipo \"" + type + "\" no existe (por ahora solo: item)");
        };
    }

    private static ItemTask parseItem(Map<String, Object> map, String where) {
        checkKeys(map, ITEM_KEYS, where);
        String item = key(required(map, "item", where), where + ": item");
        String name = text(required(map, "nombre", where), where + ": nombre");
        int amount = map.containsKey("cantidad") ? positive(map.get("cantidad"), where + ": cantidad") : 1;
        String id = map.containsKey("id") ? text(map.get("id"), where + ": id") : defaultId("item", item);
        return new ItemTask(id, name, item, amount);
    }

    // cards.yml + las listas ya leídas. Revisa que no haya listas ni ids repetidos y que cada cartilla
    // se pueda llenar con lo que hay.
    public static Catalog parseCatalog(String cardsFile, Object cardsRoot, List<TaskList> taskLists) {
        Map<String, TaskList> lists = new LinkedHashMap<>();
        Map<String, String> listOfTask = new HashMap<>();
        for (TaskList list : taskLists) {
            if (lists.putIfAbsent(list.name(), list) != null) {
                throw new CatalogException("Hay dos listas que se llaman " + list.name());
            }
            for (Task task : list.tasks()) {
                String previous = listOfTask.putIfAbsent(task.id(), list.name());
                if (previous != null) {
                    String place = previous.equals(list.name())
                            ? "dos veces en la lista " + previous
                            : "en las listas " + previous + " y " + list.name();
                    throw new CatalogException("La tarea " + task.id() + " está " + place
                            + "; si va dos veces, ponle otro id");
                }
            }
        }
        if (lists.isEmpty()) throw new CatalogException("No hay listas de tareas (tasks/*.yml)");

        Map<String, Object> map = map(cardsRoot, cardsFile);
        checkKeys(map, CARDS_KEYS, cardsFile);
        Map<String, Object> rawCards = map(required(map, "cartillas", cardsFile), cardsFile + ": cartillas");
        if (rawCards.isEmpty()) throw error(cardsFile, "no hay ninguna cartilla en cartillas");

        Map<String, CardSpec> cards = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : rawCards.entrySet()) {
            String where = cardsFile + ", cartilla " + entry.getKey();
            cards.put(entry.getKey(), parseCard(entry.getKey(), entry.getValue(), where, lists));
        }

        String defaultCard = map.containsKey("por_defecto")
                ? text(map.get("por_defecto"), cardsFile + ": por_defecto")
                : cards.keySet().iterator().next();
        if (!cards.containsKey(defaultCard)) {
            throw error(cardsFile + ": por_defecto", "no existe la cartilla " + defaultCard
                    + " (hay: " + String.join(", ", cards.keySet()) + ")");
        }
        return new Catalog(lists, cards, defaultCard);
    }

    private static CardSpec parseCard(String name, Object raw, String where, Map<String, TaskList> lists) {
        Map<String, Object> map = map(raw, where);
        checkKeys(map, CARD_KEYS, where);

        Object rawSize = map.containsKey("tamaño") ? map.get("tamaño") : map.get("tamano");
        if (rawSize == null) throw error(where, "falta tamaño");
        int size = positive(rawSize, where + ": tamaño");
        if (size < CardSpec.MIN_SIZE || size > CardSpec.MAX_SIZE) {
            throw error(where + ": tamaño", "tiene que ser de " + CardSpec.MIN_SIZE + " a " + CardSpec.MAX_SIZE
                    + " (vino " + size + ")");
        }

        Map<String, Object> rawLists = map(required(map, "listas", where), where + ": listas");
        if (rawLists.isEmpty()) throw error(where, "listas está vacío");

        Map<String, CardSpec.Quota> quotas = new LinkedHashMap<>();
        int minimums = 0;
        int maximums = 0;
        for (Map.Entry<String, Object> entry : rawLists.entrySet()) {
            String listName = entry.getKey();
            TaskList list = lists.get(listName);
            if (list == null) {
                throw error(where, "la lista " + listName + " no existe (hay: " + String.join(", ", lists.keySet()) + ")");
            }
            CardSpec.Quota quota = quota(entry.getValue(), where + ", lista " + listName);
            if (quota.min() > list.tasks().size()) {
                throw error(where, "pide mínimo " + quota.min() + " tareas de " + listName
                        + " y la lista tiene " + list.tasks().size());
            }
            quotas.put(listName, quota);
            minimums += quota.min();
            maximums += Math.min(quota.max(), list.tasks().size());
        }

        int cells = size * size;
        if (minimums > cells) {
            throw error(where, "los mínimos suman " + minimums + " y la cartilla tiene " + cells + " casillas");
        }
        if (maximums < cells) {
            throw error(where, "la cartilla tiene " + cells + " casillas y sus listas dan como máximo "
                    + maximums + " tareas");
        }
        return new CardSpec(name, size, quotas);
    }

    // `basicos: 5` (exactamente 5) o `basicos: { min: 3, max: 8 }` (cualquiera de los dos puede faltar).
    private static CardSpec.Quota quota(Object raw, String where) {
        if (raw instanceof Number) {
            int exact = nonNegative(raw, where);
            return new CardSpec.Quota(exact, exact);
        }
        Map<String, Object> map = map(raw, where);
        checkKeys(map, QUOTA_KEYS, where);
        int min = map.containsKey("min") ? nonNegative(map.get("min"), where + ": min") : 0;
        int max = map.containsKey("max") ? nonNegative(map.get("max"), where + ": max") : Integer.MAX_VALUE;
        if (min > max) throw error(where, "min (" + min + ") es mayor que max (" + max + ")");
        return new CardSpec.Quota(min, max);
    }

    // "item" + "minecraft:oak_log" → "item:oak_log"; con otro namespace queda entero.
    static String defaultId(String type, String key) {
        return type + ":" + (key.startsWith(MINECRAFT) ? key.substring(MINECRAFT.length()) : key);
    }

    // `oak_log` → `minecraft:oak_log`; en minúsculas y con la forma de una clave de Minecraft.
    private static String key(Object raw, String where) {
        String key = text(raw, where).toLowerCase(Locale.ROOT);
        if (!key.contains(":")) key = MINECRAFT + key;
        if (!KEY.matcher(key).matches()) {
            throw error(where, "\"" + raw + "\" no es una clave de Minecraft (ej.: oak_log o minecraft:oak_log)");
        }
        return key;
    }

    private static Tier tier(Object raw, String where) {
        String text = text(raw, where).toUpperCase(Locale.ROOT);
        for (Tier tier : Tier.values()) {
            if (tier.name().equals(text)) return tier;
        }
        throw error(where, "tiene que ser S, A, B, C o D (vino " + raw + ")");
    }

    private static String baseName(String fileName) {
        String name = fileName.substring(fileName.lastIndexOf('/') + 1);
        return name.endsWith(".yml") ? name.substring(0, name.length() - ".yml".length()) : name;
    }

    private static Object required(Map<String, Object> map, String key, String where) {
        Object value = map.get(key);
        if (value == null) throw error(where, "falta " + key);
        return value;
    }

    private static void checkKeys(Map<String, Object> map, Set<String> allowed, String where) {
        for (String key : map.keySet()) {
            if (!allowed.contains(key)) {
                throw error(where, "no conozco \"" + key + "\" (se puede usar: " + String.join(", ", new TreeSet<>(allowed)) + ")");
            }
        }
    }

    private static Map<String, Object> map(Object raw, String where) {
        if (raw == null) throw error(where, "está vacío");
        if (!(raw instanceof Map<?, ?> rawMap)) throw error(where, "tiene que ser un mapa (clave: valor)");
        Map<String, Object> map = new LinkedHashMap<>();
        rawMap.forEach((key, value) -> map.put(String.valueOf(key), value));
        return map;
    }

    private static List<?> list(Object raw, String where) {
        if (!(raw instanceof List<?> list)) throw error(where, "tiene que ser una lista (líneas con -)");
        if (list.isEmpty()) throw error(where, "está vacía");
        return list;
    }

    private static String text(Object raw, String where) {
        if (!(raw instanceof String) && !(raw instanceof Number)) throw error(where, "tiene que ser un texto");
        String text = raw.toString().trim();
        if (text.isEmpty()) throw error(where, "está vacío");
        return text;
    }

    private static int positive(Object raw, String where) {
        int value = integer(raw, where);
        if (value < 1) throw error(where, "tiene que ser mayor que 0 (vino " + value + ")");
        return value;
    }

    private static int nonNegative(Object raw, String where) {
        int value = integer(raw, where);
        if (value < 0) throw error(where, "no puede ser negativo (vino " + value + ")");
        return value;
    }

    private static int integer(Object raw, String where) {
        if (raw instanceof Integer || raw instanceof Long) {
            long value = ((Number) raw).longValue();
            if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) return (int) value;
        }
        throw error(where, "tiene que ser un número entero (vino " + raw + ")");
    }

    private static CatalogException error(String where, String problem) {
        return new CatalogException(where + ": " + problem);
    }
}
