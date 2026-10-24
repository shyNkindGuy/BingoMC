package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.task.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

// Arma las tareas de una cartilla: de cada lista sale entre su mínimo y su máximo, sin repetir (los ids
// son únicos en el catálogo), hasta llenar las N² casillas. El mismo Random (la misma semilla) da las
// mismas tareas en el mismo orden.
public final class CardGenerator {

    private CardGenerator() {
    }

    public static List<Task> generate(CardSpec spec, Map<String, TaskList> lists, Random random,
                                      Predicate<Task> available) {
        List<Task> chosen = new ArrayList<>();
        // lo que cada lista todavía puede dar por encima de su mínimo, y las tareas que quedaron sin salir
        Map<String, Integer> room = new HashMap<>();
        List<Map.Entry<String, Task>> extras = new ArrayList<>();

        for (Map.Entry<String, CardSpec.Quota> entry : spec.lists().entrySet()) {
            String name = entry.getKey();
            CardSpec.Quota quota = entry.getValue();
            TaskList list = lists.get(name);
            if (list == null) {
                throw new CatalogException("La cartilla " + spec.name() + " usa la lista " + name + ", que no existe");
            }

            List<Task> candidates = new ArrayList<>();
            for (Task task : list.tasks()) {
                if (available.test(task)) candidates.add(task);
            }
            if (candidates.size() < quota.min()) {
                throw new CatalogException("La cartilla " + spec.name() + " pide mínimo " + quota.min()
                        + " tareas de " + name + " y solo hay " + candidates.size() + " disponibles");
            }

            Collections.shuffle(candidates, random);
            chosen.addAll(candidates.subList(0, quota.min()));
            room.put(name, Math.min(quota.max(), candidates.size()) - quota.min());
            for (Task task : candidates.subList(quota.min(), candidates.size())) {
                extras.add(Map.entry(name, task));
            }
        }

        if (chosen.size() > spec.cells()) {
            throw new CatalogException("La cartilla " + spec.name() + " tiene " + spec.cells()
                    + " casillas y sus mínimos suman " + chosen.size());
        }

        // el resto, al azar entre todas las que quedaron, sin pasar el máximo de ninguna lista
        Collections.shuffle(extras, random);
        for (Map.Entry<String, Task> extra : extras) {
            if (chosen.size() == spec.cells()) break;
            int left = room.get(extra.getKey());
            if (left == 0) continue;
            room.put(extra.getKey(), left - 1);
            chosen.add(extra.getValue());
        }

        if (chosen.size() < spec.cells()) {
            throw new CatalogException("La cartilla " + spec.name() + " necesita " + spec.cells()
                    + " tareas y solo alcanzan " + chosen.size() + " disponibles");
        }

        Collections.shuffle(chosen, random);
        return chosen;
    }
}
