package com.shyNkindguy.bingo.core.task;

import com.shyNkindguy.bingo.core.event.EventContext;
import com.shyNkindguy.bingo.core.event.InventoryScanned;
import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.core.progress.TaskProgress;

import java.util.HashSet;
import java.util.List;

// "Tener a la vez `needed` de estos `items` distintos (`amountEach` de cada uno)": 3 lanas de 5 colores
// (alguno) o las 3 herramientas de hierro (todos: needed = cantidad de ítems). Ícono por defecto: el primer
// ítem (el plugin dibuja los dos primeros).
public record SetTask(String id, String name, List<String> items, int needed, int amountEach, String customIcon)
        implements Task {

    public SetTask {
        items = List.copyOf(items);
        if (items.size() < 2) throw new IllegalArgumentException("Un conjunto necesita al menos 2 ítems");
        if (new HashSet<>(items).size() != items.size()) {
            throw new IllegalArgumentException("Un conjunto no puede repetir ítems: " + items);
        }
        if (needed < 1 || needed > items.size()) {
            throw new IllegalArgumentException("Se pueden pedir de 1 a " + items.size() + " ítems: " + needed);
        }
        if (amountEach < 1) throw new IllegalArgumentException("La cantidad tiene que ser al menos 1: " + amountEach);
    }

    public boolean all() {
        return needed == items.size();
    }

    @Override
    public String displayName() {
        return name;
    }

    @Override
    public String icon() {
        return customIcon != null ? customIcon : items.get(0);
    }

    @Override
    public int target() {
        return needed;
    }

    @Override
    public TaskProgress apply(TaskEvent event, EventContext context, TaskProgress progress) {
        if (!(event instanceof InventoryScanned scanned)) return progress;

        int held = 0;
        for (String item : items) {
            if (scanned.totals().getOrDefault(item, 0) >= amountEach) held++;
        }
        return progress.reach(held);
    }
}
