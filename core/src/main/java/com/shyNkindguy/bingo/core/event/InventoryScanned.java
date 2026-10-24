package com.shyNkindguy.bingo.core.event;

import java.util.Map;

// Todo lo que el jugador tiene ahora en el inventario: ítem ("minecraft:white_wool") → cuántos. Lo usan los
// conjuntos ("3 lanas distintas"), que necesitan ver varios ítems a la vez.
public record InventoryScanned(Map<String, Integer> totals) implements TaskEvent {

    public InventoryScanned {
        totals = Map.copyOf(totals);
    }
}
