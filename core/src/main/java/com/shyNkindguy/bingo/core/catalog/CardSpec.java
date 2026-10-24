package com.shyNkindguy.bingo.core.catalog;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

// Una cartilla de cards.yml: lado N y cuántas tareas (mínimo y máximo) salen de cada lista.
public record CardSpec(String name, int size, Map<String, Quota> lists) {

    public static final int MIN_SIZE = 3;
    public static final int MAX_SIZE = 7;

    public record Quota(int min, int max) {

        public Quota {
            if (min < 0 || max < min) {
                throw new IllegalArgumentException("Mínimo y máximo inválidos: " + min + ", " + max);
            }
        }
    }

    public CardSpec {
        lists = Collections.unmodifiableMap(new LinkedHashMap<>(lists));
    }

    public int cells() {
        return size * size;
    }
}
