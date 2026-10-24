package com.shyNkindguy.bingo.core.text;

import com.shyNkindguy.bingo.core.catalog.CatalogException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Los textos del juego (messages.yml): una clave con puntos ("partida.ganador") → una plantilla con
// {variables} y colores con & (&a, &l…). Lo que falte en el archivo del servidor sale de los por defecto,
// así una versión nueva del plugin nunca deja un mensaje vacío.
public final class Messages {

    private static final Pattern COLOR = Pattern.compile("&([0-9a-fk-orA-FK-OR])");

    private final Map<String, String> templates;

    public Messages(Map<String, String> templates) {
        this.templates = Collections.unmodifiableMap(new LinkedHashMap<>(templates));
    }

    // `root`: lo que devuelve SnakeYAML (mapas anidados de textos). Con `defaults`, las claves que falten
    // salen de ahí y una clave que no existe en `defaults` es un error (casi siempre, una mal escrita).
    public static Messages parse(String file, Object root, Messages defaults) {
        Map<String, String> read = new LinkedHashMap<>();
        if (root != null) {
            if (!(root instanceof Map<?, ?> map)) throw new CatalogException(file + ": tiene que ser un mapa (clave: texto)");
            flatten(file, "", map, read);
        }
        if (defaults == null) return new Messages(read);

        for (String key : read.keySet()) {
            if (!defaults.templates.containsKey(key)) throw new CatalogException(file + ": no conozco la clave " + key);
        }
        Map<String, String> merged = new LinkedHashMap<>(defaults.templates);
        merged.putAll(read);
        return new Messages(merged);
    }

    private static void flatten(String file, String prefix, Map<?, ?> map, Map<String, String> out) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = prefix + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nested) {
                flatten(file, key + ".", nested, out);
            } else if (value instanceof String || value instanceof Number) {
                out.put(key, value.toString());
            } else if (value instanceof List<?> lines) {
                out.put(key, String.join("\n", lines.stream().map(String::valueOf).toList()));
            } else {
                throw new CatalogException(file + ": " + key + " tiene que ser un texto");
            }
        }
    }

    public Set<String> keys() {
        return templates.keySet();
    }

    // El texto de `key` con las variables reemplazadas (`args`: nombre, valor, nombre, valor…) y los & como
    // colores. Una clave que no existe sale como "[clave]", para que se note sin romper nada.
    public String get(String key, Object... args) {
        String template = templates.get(key);
        if (template == null) return "[" + key + "]";
        String text = template;
        for (int i = 0; i + 1 < args.length; i += 2) {
            text = text.replace("{" + args[i] + "}", String.valueOf(args[i + 1]));
        }
        return colors(text);
    }

    // &a → §a (solo los códigos de color y formato de Minecraft; otros & quedan igual).
    public static String colors(String text) {
        Matcher matcher = COLOR.matcher(text);
        StringBuilder out = new StringBuilder();
        while (matcher.find()) matcher.appendReplacement(out, "§" + matcher.group(1).toLowerCase());
        matcher.appendTail(out);
        return out.toString();
    }
}
