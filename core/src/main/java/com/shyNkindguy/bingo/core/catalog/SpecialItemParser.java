package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.item.ItemEffect;
import com.shyNkindguy.bingo.core.item.SpecialItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.shyNkindguy.bingo.core.catalog.CatalogParser.checkKeys;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.error;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.key;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.list;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.map;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.positive;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.required;
import static com.shyNkindguy.bingo.core.catalog.CatalogParser.text;

// items.yml: `items:` es una lista; cada ítem especial tiene id, nombre, descripcion, item (el de Minecraft que
// lo lleva) y efecto (cambiar_casilla, brujula_bioma con `radio`, escudo_pvp con `segundos`). Todo se valida con
// "archivo, ítem N: problema".
final class SpecialItemParser {
    private static final Pattern ID = Pattern.compile("[a-z0-9_]+");
    private static final int DEFAULT_RADIUS = 2000;
    private static final int DEFAULT_SECONDS = 60;
    private static final Set<String> TOP_KEYS = Set.of("items");
    private static final Set<String> COMMON_KEYS = Set.of("id", "nombre", "descripcion", "item", "efecto");
    private static final Set<String> COMPASS_KEYS = with(COMMON_KEYS, "radio");
    private static final Set<String> SHIELD_KEYS = with(COMMON_KEYS, "segundos");

    private SpecialItemParser() {
    }

    static List<SpecialItem> parse(String file, Object root) {
        if (root == null) return List.of();
        Map<String, Object> top = map(root, file);
        checkKeys(top, TOP_KEYS, file);
        if (top.get("items") == null) return List.of();

        List<?> entries = list(top.get("items"), file + ": items");
        List<SpecialItem> items = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < entries.size(); i++) {
            String where = file + ", ítem " + (i + 1);
            SpecialItem item = item(map(entries.get(i), where), where);
            if (!ids.add(item.id())) throw error(where, "el id " + item.id() + " ya está en otro ítem");
            items.add(item);
        }
        return List.copyOf(items);
    }

    private static SpecialItem item(Map<String, Object> map, String where) {
        String id = text(required(map, "id", where), where + ": id");
        if (!ID.matcher(id).matches()) throw error(where + ": id", "solo minúsculas, números y _ (vino " + id + ")");
        String name = text(required(map, "nombre", where), where + ": nombre");
        String description = text(required(map, "descripcion", where), where + ": descripcion");
        String material = key(required(map, "item", where), where + ": item");
        String effectName = text(required(map, "efecto", where), where + ": efecto").toLowerCase(Locale.ROOT);

        ItemEffect effect = switch (effectName) {
            case "cambiar_casilla" -> {
                checkKeys(map, COMMON_KEYS, where);
                yield new ItemEffect.Reroll();
            }
            case "brujula_bioma" -> {
                checkKeys(map, COMPASS_KEYS, where);
                int radius = map.containsKey("radio") ? positive(map.get("radio"), where + ": radio") : DEFAULT_RADIUS;
                if (radius < ItemEffect.BiomeCompass.MIN_RADIUS || radius > ItemEffect.BiomeCompass.MAX_RADIUS) {
                    throw error(where + ": radio", "tiene que estar entre " + ItemEffect.BiomeCompass.MIN_RADIUS + " y "
                            + ItemEffect.BiomeCompass.MAX_RADIUS + " (vino " + radius + ")");
                }
                yield new ItemEffect.BiomeCompass(radius);
            }
            case "escudo_pvp" -> {
                checkKeys(map, SHIELD_KEYS, where);
                int seconds = map.containsKey("segundos") ? positive(map.get("segundos"), where + ": segundos") : DEFAULT_SECONDS;
                if (seconds > ItemEffect.PvpShield.MAX_SECONDS) {
                    throw error(where + ": segundos", "como mucho " + ItemEffect.PvpShield.MAX_SECONDS + " (vino " + seconds + ")");
                }
                yield new ItemEffect.PvpShield(seconds);
            }
            default -> throw error(where + ": efecto",
                    "tiene que ser cambiar_casilla, brujula_bioma o escudo_pvp (vino " + map.get("efecto") + ")");
        };
        return new SpecialItem(id, name, description, material, effect);
    }

    private static Set<String> with(Set<String> base, String... more) {
        Set<String> all = new HashSet<>(base);
        all.addAll(List.of(more));
        return Set.copyOf(all);
    }
}
