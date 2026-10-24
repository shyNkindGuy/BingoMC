package com.shyNkindguy.bingo.core.status;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

// Un estado del jugador mientras dura la partida o mientras lleva un conjunto: un efecto de Minecraft con su
// nivel (1 = I). `name` es como se escribe en los YAML ("vision_nocturna").
public record PlayerStatus(String name, String effect, int level) {

    public static final int MAX_LEVEL = 5;
    private static final String[] ROMAN = {"I", "II", "III", "IV", "V"};

    private record Kind(String effect, String display) {
    }

    // nombre en los YAML → efecto de Minecraft y cómo se le muestra al jugador
    private static final Map<String, Kind> KINDS = new LinkedHashMap<>();

    static {
        KINDS.put("velocidad", new Kind("minecraft:speed", "velocidad"));
        KINDS.put("vision_nocturna", new Kind("minecraft:night_vision", "visión nocturna"));
        KINDS.put("sin_hambre", new Kind("minecraft:saturation", "sin hambre"));
        KINDS.put("prisa", new Kind("minecraft:haste", "prisa minera"));
        KINDS.put("salto", new Kind("minecraft:jump_boost", "salto"));
        KINDS.put("resistencia_al_fuego", new Kind("minecraft:fire_resistance", "resistencia al fuego"));
        KINDS.put("respirar_bajo_el_agua", new Kind("minecraft:water_breathing", "respiración acuática"));
        KINDS.put("gracia_del_delfin", new Kind("minecraft:dolphins_grace", "gracia del delfín"));
        KINDS.put("fuerza", new Kind("minecraft:strength", "fuerza"));
        KINDS.put("resistencia", new Kind("minecraft:resistance", "resistencia"));
        KINDS.put("regeneracion", new Kind("minecraft:regeneration", "regeneración"));
    }

    public PlayerStatus {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(effect, "effect");
        if (level < 1 || level > MAX_LEVEL) throw new IllegalArgumentException("nivel fuera de rango: " + level);
    }

    // "velocidad", "velocidad 2" o "Visión nocturna" (sin importar mayúsculas, tildes ni espacios). Lanza
    // IllegalArgumentException con el problema en español.
    public static PlayerStatus parse(String text) {
        String plain = Normalizer.normalize(text.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("\\s+", " ");
        int level = 1;
        int space = plain.lastIndexOf(' ');
        if (space > 0 && plain.substring(space + 1).matches("\\d+")) {
            level = Integer.parseInt(plain.substring(space + 1));
            plain = plain.substring(0, space);
            if (level < 1 || level > MAX_LEVEL) {
                throw new IllegalArgumentException("el nivel tiene que ser de 1 a " + MAX_LEVEL + " (vino " + text + ")");
            }
        }
        String name = plain.replace(' ', '_');
        Kind kind = KINDS.get(name);
        if (kind == null) {
            throw new IllegalArgumentException("\"" + text + "\" no es un estado (se puede usar: " + String.join(", ", KINDS.keySet()) + ")");
        }
        return new PlayerStatus(name, kind.effect(), level);
    }

    // "visión nocturna", "prisa minera II"
    public String description() {
        Kind kind = KINDS.get(name);
        String display = kind != null ? kind.display() : name;
        return level == 1 ? display : display + " " + ROMAN[level - 1];
    }
}
