package com.shyNkindguy.bingo.core.task;

import com.shyNkindguy.bingo.core.event.InventoryScanned;
import com.shyNkindguy.bingo.core.event.ItemObtained;
import com.shyNkindguy.bingo.core.progress.TaskProgress;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetTaskTest {

    private static final List<String> LANAS = List.of("minecraft:red_wool", "minecraft:blue_wool",
            "minecraft:yellow_wool", "minecraft:lime_wool", "minecraft:black_wool");
    private final SetTask tresLanas = new SetTask("any_of:3", "3 lanas", LANAS, 3, 1, null);
    private final SetTask herramientas = new SetTask("all_of", "Herramientas",
            List.of("minecraft:iron_pickaxe", "minecraft:iron_axe"), 2, 1, null);

    private static InventoryScanned inventario(Map<String, Integer> totales) {
        return new InventoryScanned(totales);
    }

    @Test
    void cuentaCuantosDistintosTieneALaVez() {
        TaskProgress p = tresLanas.apply(inventario(Map.of("minecraft:red_wool", 5, "minecraft:dirt", 64)),
                TaskProgress.start(3));
        assertEquals(1, p.current());

        p = tresLanas.apply(inventario(Map.of("minecraft:red_wool", 1, "minecraft:blue_wool", 1,
                "minecraft:black_wool", 2)), p);
        assertTrue(p.isComplete());
    }

    @Test
    void elAvanceNoBajaAunqueSuelteLasLanas() {
        TaskProgress p = tresLanas.apply(inventario(Map.of("minecraft:red_wool", 1, "minecraft:blue_wool", 1)),
                TaskProgress.start(3));
        p = tresLanas.apply(inventario(Map.of()), p);
        assertEquals(2, p.current());
    }

    @Test
    void todosPideTodosYCantidadEsDeCadaUno() {
        assertTrue(herramientas.all());
        assertFalse(tresLanas.all());
        assertEquals(1, herramientas.apply(inventario(Map.of("minecraft:iron_axe", 1)), TaskProgress.start(2)).current());

        SetTask dosDeCada = new SetTask("x", "X", List.of("minecraft:apple", "minecraft:bread"), 2, 2, null);
        assertEquals(1, dosDeCada.apply(inventario(Map.of("minecraft:apple", 2, "minecraft:bread", 1)),
                TaskProgress.start(2)).current());
    }

    @Test
    void soloLeImportaElInventarioEntero() {
        TaskProgress inicio = TaskProgress.start(3);
        assertSame(inicio, tresLanas.apply(new ItemObtained("minecraft:red_wool", 1), inicio));
    }

    @Test
    void iconoYNombre() {
        assertEquals("minecraft:red_wool", tresLanas.icon());
        assertEquals("minecraft:white_wool",
                new SetTask("x", "X", LANAS, 1, 1, "minecraft:white_wool").icon());
        assertEquals("3 lanas", tresLanas.displayName());
        assertEquals(3, tresLanas.target());
    }

    @Test
    void conjuntosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new SetTask("x", "X", List.of("minecraft:a"), 1, 1, null));
        assertThrows(IllegalArgumentException.class,
                () -> new SetTask("x", "X", List.of("minecraft:a", "minecraft:a"), 1, 1, null));
        assertThrows(IllegalArgumentException.class, () -> new SetTask("x", "X", LANAS, 6, 1, null));
        assertThrows(IllegalArgumentException.class, () -> new SetTask("x", "X", LANAS, 0, 1, null));
    }
}
