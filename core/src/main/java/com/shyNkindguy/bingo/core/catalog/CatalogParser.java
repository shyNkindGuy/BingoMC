package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.condition.AllOf;
import com.shyNkindguy.bingo.core.condition.AnyOf;
import com.shyNkindguy.bingo.core.condition.BelowY;
import com.shyNkindguy.bingo.core.condition.Condition;
import com.shyNkindguy.bingo.core.condition.InBiome;
import com.shyNkindguy.bingo.core.condition.InDimension;
import com.shyNkindguy.bingo.core.condition.NoDeath;
import com.shyNkindguy.bingo.core.condition.TimeOfDay;
import com.shyNkindguy.bingo.core.task.AdvancementTask;
import com.shyNkindguy.bingo.core.task.ConditionalTask;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.KillTask;
import com.shyNkindguy.bingo.core.task.SetTask;
import com.shyNkindguy.bingo.core.task.StatisticTask;
import com.shyNkindguy.bingo.core.task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// Pasa lo que devuelve SnakeYAML (mapas, listas, textos y números) a listas, cartillas y catálogo.
// Valida todo al leer, así un error se ve en /bingo reload y no a mitad de partida. Los mensajes dicen
// dónde está el problema: "tasks/basicos.yml, tarea 3: falta nombre".
public final class CatalogParser {

    private static final Pattern KEY = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    private static final String MINECRAFT = "minecraft:";

    private static final Set<String> LIST_KEYS = Set.of("lista", "tier", "tareas");
    // tipo de tarea en el YAML (en inglés o en español) → tipo
    private static final Map<String, String> TYPES = Map.ofEntries(
            Map.entry("item", "item"),
            Map.entry("kill", "kill"), Map.entry("matar", "kill"),
            Map.entry("advancement", "advancement"), Map.entry("logro", "advancement"),
            Map.entry("statistic", "statistic"), Map.entry("estadistica", "statistic"),
            Map.entry("any_of", "any_of"), Map.entry("alguno", "any_of"),
            Map.entry("all_of", "all_of"), Map.entry("todos", "all_of"));
    private static final String TYPE_NAMES = "item, kill (o matar), advancement (o logro), statistic (o estadistica), "
            + "any_of (o alguno), all_of (o todos)";

    private static final Set<String> ITEM_KEYS = keys("item", "cantidad");
    private static final Set<String> KILL_KEYS = keys("mob", "cantidad");
    private static final Set<String> ADVANCEMENT_KEYS = keys("logro");
    private static final Set<String> STATISTIC_KEYS = keys("estadistica", "de", "cantidad");
    private static final Set<String> ANY_OF_KEYS = keys("de", "necesita", "cantidad");
    private static final Set<String> ALL_OF_KEYS = keys("de", "cantidad");
    private static final Set<String> CONDITION_KEYS = Set.of("dimension", "hora", "time", "bioma", "biome",
            "debajo_de_y", "below_y", "sin_morir", "no_death", "alguna", "any_of");
    private static final Map<String, String> DIMENSIONS = Map.of(
            "overworld", "minecraft:overworld", "superficie", "minecraft:overworld",
            "nether", "minecraft:the_nether", "the_nether", "minecraft:the_nether",
            "end", "minecraft:the_end", "the_end", "minecraft:the_end", "fin", "minecraft:the_end");
    private static final Set<String> CARDS_KEYS = Set.of("por_defecto", "cartillas");
    private static final Set<String> CARD_KEYS = Set.of("tamaño", "tamano", "listas");
    private static final Set<String> QUOTA_KEYS = Set.of("min", "max");

    private CatalogParser() {
    }

    // Las claves que puede tener una tarea de un tipo: las comunes a todas más las propias.
    private static Set<String> keys(String... own) {
        Set<String> keys = new HashSet<>(Set.of("tipo", "id", "nombre", "icono", "condiciones"));
        keys.addAll(List.of(own));
        return Set.copyOf(keys);
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
        String written = map.containsKey("tipo")
                ? text(map.get("tipo"), where + ": tipo").toLowerCase(Locale.ROOT)
                : "item";
        String type = TYPES.get(written);
        if (type == null) throw error(where, "el tipo \"" + written + "\" no existe (se puede usar: " + TYPE_NAMES + ")");
        Task task = switch (type) {
            case "item" -> parseItem(map, where);
            case "kill" -> parseKill(map, where);
            case "advancement" -> parseAdvancement(map, where);
            case "statistic" -> parseStatistic(map, where);
            case "any_of" -> parseSet(map, where, false);
            default -> parseSet(map, where, true);
        };
        if (!map.containsKey("condiciones")) return task;

        // con condiciones el id por defecto las lleva, así no choca con la misma tarea sin condición
        Condition condition = conditions(map.get("condiciones"), where + ": condiciones");
        String id = map.containsKey("id") ? task.id() : task.id() + "[" + condition.key() + "]";
        return new ConditionalTask(id, task, condition);
    }

    private static ItemTask parseItem(Map<String, Object> map, String where) {
        checkKeys(map, ITEM_KEYS, where);
        String item = key(required(map, "item", where), where + ": item");
        String name = text(required(map, "nombre", where), where + ": nombre");
        int amount = map.containsKey("cantidad") ? positive(map.get("cantidad"), where + ": cantidad") : 1;
        String id = map.containsKey("id") ? text(map.get("id"), where + ": id") : defaultId("item", item);
        return new ItemTask(id, name, item, amount, icon(map, where));
    }

    private static KillTask parseKill(Map<String, Object> map, String where) {
        checkKeys(map, KILL_KEYS, where);
        String entity = key(required(map, "mob", where), where + ": mob");
        String name = text(required(map, "nombre", where), where + ": nombre");
        int amount = map.containsKey("cantidad") ? positive(map.get("cantidad"), where + ": cantidad") : 1;
        String id = map.containsKey("id") ? text(map.get("id"), where + ": id") : defaultId("kill", entity);
        return new KillTask(id, name, entity, amount, icon(map, where));
    }

    private static AdvancementTask parseAdvancement(Map<String, Object> map, String where) {
        checkKeys(map, ADVANCEMENT_KEYS, where);
        String advancement = key(required(map, "logro", where), where + ": logro");
        String name = text(required(map, "nombre", where), where + ": nombre");
        String id = map.containsKey("id") ? text(map.get("id"), where + ": id") : defaultId("advancement", advancement);
        return new AdvancementTask(id, name, advancement, icon(map, where));
    }

    private static StatisticTask parseStatistic(Map<String, Object> map, String where) {
        checkKeys(map, STATISTIC_KEYS, where);
        String statistic = key(required(map, "estadistica", where), where + ": estadistica");
        String subject = map.containsKey("de") ? key(map.get("de"), where + ": de") : null;
        String name = text(required(map, "nombre", where), where + ": nombre");
        int amount = positive(required(map, "cantidad", where), where + ": cantidad");
        String id = map.containsKey("id") ? text(map.get("id"), where + ": id")
                : defaultId("statistic", statistic) + (subject == null ? "" : "/" + shortKey(subject));
        return new StatisticTask(id, name, statistic, subject, amount, icon(map, where));
    }

    // alguno: `{ tipo: alguno, de: [white_wool, red_wool, blue_wool], necesita: 2, nombre: … }`;
    // todos: `{ tipo: todos, de: [iron_pickaxe, iron_axe], nombre: … }`. `cantidad` es de cada ítem.
    private static SetTask parseSet(Map<String, Object> map, String where, boolean all) {
        checkKeys(map, all ? ALL_OF_KEYS : ANY_OF_KEYS, where);
        List<?> rawItems = list(required(map, "de", where), where + ": de");
        List<String> items = new ArrayList<>();
        for (Object rawItem : rawItems) {
            String item = key(rawItem, where + ": de");
            if (items.contains(item)) throw error(where + ": de", item + " está dos veces");
            items.add(item);
        }
        if (items.size() < 2) throw error(where + ": de", "hacen falta al menos 2 ítems");

        String name = text(required(map, "nombre", where), where + ": nombre");
        int needed = all ? items.size()
                : map.containsKey("necesita") ? positive(map.get("necesita"), where + ": necesita") : 1;
        if (needed > items.size()) {
            throw error(where + ": necesita", "pide " + needed + " y en de hay " + items.size() + " ítems");
        }
        int each = map.containsKey("cantidad") ? positive(map.get("cantidad"), where + ": cantidad") : 1;
        String joined = items.stream().map(CatalogParser::shortKey).collect(Collectors.joining("+"));
        String id = map.containsKey("id") ? text(map.get("id"), where + ": id")
                : (all ? "all_of:" : "any_of:" + needed + ":") + joined;
        return new SetTask(id, name, items, needed, each, icon(map, where));
    }

    // `condiciones: [ { dimension: nether }, { hora: noche } ]`: se tienen que cumplir todas. Una entrada con
    // varias claves también pide todas; `alguna: [ … ]` pide una de su lista.
    private static Condition conditions(Object raw, String where) {
        List<Condition> all = new ArrayList<>();
        if (raw instanceof Map<?, ?>) {
            all.addAll(conditionEntry(map(raw, where), where));
        } else {
            List<?> entries = list(raw, where);
            for (int i = 0; i < entries.size(); i++) {
                String entryWhere = where + " " + (i + 1);
                all.addAll(conditionEntry(map(entries.get(i), entryWhere), entryWhere));
            }
        }
        return all.size() == 1 ? all.get(0) : new AllOf(all);
    }

    private static List<Condition> conditionEntry(Map<String, Object> map, String where) {
        checkKeys(map, CONDITION_KEYS, where);
        if (map.isEmpty()) throw error(where, "está vacía");
        List<Condition> conditions = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            conditions.add(condition(entry.getKey(), entry.getValue(), where + ": " + entry.getKey()));
        }
        return conditions;
    }

    private static Condition condition(String key, Object value, String where) {
        return switch (key) {
            case "dimension" -> new InDimension(texts(value, where).stream()
                    .map(text -> dimension(text, where)).collect(Collectors.toSet()));
            case "hora", "time" -> timeOfDay(value, where);
            case "bioma", "biome" -> new InBiome(texts(value, where).stream()
                    .map(text -> key(text, where)).collect(Collectors.toSet()));
            case "debajo_de_y", "below_y" -> new BelowY(integer(value, where));
            case "sin_morir", "no_death" -> {
                if (!isYes(value)) throw error(where, "se pone sin_morir: true (o se quita)");
                yield new NoDeath();
            }
            default -> {
                List<?> options = list(value, where);
                List<Condition> any = new ArrayList<>();
                for (int i = 0; i < options.size(); i++) {
                    String optionWhere = where + " " + (i + 1);
                    List<Condition> option = conditionEntry(map(options.get(i), optionWhere), optionWhere);
                    any.add(option.size() == 1 ? option.get(0) : new AllOf(option));
                }
                yield new AnyOf(any);
            }
        };
    }

    // nether, the_nether o minecraft:the_nether → "minecraft:the_nether"; con otro namespace, la que sea
    // (dimensiones de datapacks).
    private static String dimension(String text, String where) {
        String lower = text.toLowerCase(Locale.ROOT);
        String path = lower.startsWith(MINECRAFT) ? lower.substring(MINECRAFT.length()) : lower;
        if (DIMENSIONS.containsKey(path)) return DIMENSIONS.get(path);
        if (lower.contains(":") && !lower.startsWith(MINECRAFT)) return key(lower, where);
        throw error(where, "\"" + text + "\" no es una dimensión (se puede usar: overworld, nether, end)");
    }

    private static TimeOfDay timeOfDay(Object value, String where) {
        String text = text(value, where).toLowerCase(Locale.ROOT);
        return switch (text) {
            case "dia", "día", "day" -> new TimeOfDay(false);
            case "noche", "night" -> new TimeOfDay(true);
            default -> throw error(where, "tiene que ser dia o noche (vino " + value + ")");
        };
    }

    private static boolean isYes(Object value) {
        return Boolean.TRUE.equals(value) || (value instanceof String text
                && Set.of("si", "sí", "true").contains(text.trim().toLowerCase(Locale.ROOT)));
    }

    // Un texto o una lista de textos.
    private static List<String> texts(Object raw, String where) {
        if (!(raw instanceof List<?> list)) return List.of(text(raw, where));
        if (list.isEmpty()) throw error(where, "está vacía");
        List<String> texts = new ArrayList<>();
        for (Object item : list) texts.add(text(item, where));
        return texts;
    }

    // `icono: rotten_flesh`: el ítem que se dibuja en la casilla en vez del de por defecto del tipo.
    private static String icon(Map<String, Object> map, String where) {
        return map.containsKey("icono") ? key(map.get("icono"), where + ": icono") : null;
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
        return type + ":" + shortKey(key);
    }

    private static String shortKey(String key) {
        return key.startsWith(MINECRAFT) ? key.substring(MINECRAFT.length()) : key;
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
