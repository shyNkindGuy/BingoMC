package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.task.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

// Lo que se leyó de los YAML: listas y cartillas por nombre (en el orden de los archivos) y la cartilla
// que usa /bingo start si no se nombra otra. Lo arma CatalogParser, que ya validó todo.
public record Catalog(Map<String, TaskList> lists, Map<String, CardSpec> cards, String defaultCard) {

    public Catalog {
        lists = Collections.unmodifiableMap(new LinkedHashMap<>(lists));
        cards = Collections.unmodifiableMap(new LinkedHashMap<>(cards));
        if (!cards.containsKey(defaultCard)) {
            throw new IllegalArgumentException("La cartilla por defecto no existe: " + defaultCard);
        }
    }

    public Optional<CardSpec> card(String name) {
        return Optional.ofNullable(cards.get(name));
    }

    public List<Task> tasks() {
        List<Task> all = new ArrayList<>();
        for (TaskList list : lists.values()) all.addAll(list.tasks());
        return all;
    }

    // Las tareas de una partida con la cartilla `cardName`; `available` dice si una tarea se puede hacer
    // (el plugin: "el ítem existe en esta versión").
    public List<Task> generate(String cardName, Random random, Predicate<Task> available) {
        CardSpec spec = card(cardName).orElseThrow(() -> new CatalogException("No existe la cartilla " + cardName));
        return CardGenerator.generate(spec, lists, random, available);
    }
}
