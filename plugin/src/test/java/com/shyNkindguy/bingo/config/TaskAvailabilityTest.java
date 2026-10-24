package com.shyNkindguy.bingo.config;

import com.shyNkindguy.bingo.core.condition.TimeOfDay;
import com.shyNkindguy.bingo.core.task.AdvancementTask;
import com.shyNkindguy.bingo.core.task.ConditionalTask;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.KillTask;
import com.shyNkindguy.bingo.core.task.SetTask;
import com.shyNkindguy.bingo.core.task.StatisticTask;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Corre contra el paper-api real del CI. Lo que pide un servidor (logros, si un Material es bloque) va con
// listas fijas.
class TaskAvailabilityTest {

    private final TaskAvailability availability = new TaskAvailability(
            Set.of("minecraft:story/enter_the_nether")::contains,
            Set.of(Material.STONE, Material.DIORITE)::contains,
            material -> true);

    private static StatisticTask estadistica(String statistic, String subject) {
        return new StatisticTask("s", "S", statistic, subject, 1);
    }

    @Test
    void items() {
        assertNull(availability.problem(new ItemTask("i", "Pan", "minecraft:bread", 1)));
        assertEquals("el ítem no existe", availability.problem(new ItemTask("i", "X", "minecraft:no_existe", 1)));
    }

    @Test
    void mobs() {
        assertNull(availability.problem(new KillTask("k", "Zombi", "minecraft:zombie", 1)));
        assertEquals("el mob no existe", availability.problem(new KillTask("k", "X", "minecraft:no_existe", 1)));
        assertEquals("no es un mob", availability.problem(new KillTask("k", "Ítem", "minecraft:item", 1)));
    }

    @Test
    void logros() {
        assertTrue(availability.isAvailable(new AdvancementTask("a", "Nether", "minecraft:story/enter_the_nether")));
        assertEquals("el logro no existe",
                availability.problem(new AdvancementTask("a", "X", "minecraft:story/no_existe")));
    }

    @Test
    void conjuntosYCondiciones() {
        assertNull(availability.problem(new SetTask("s", "S", List.of("minecraft:bread", "minecraft:apple"), 1, 1, null)));
        assertEquals("el ítem minecraft:no_existe no existe", availability.problem(
                new SetTask("s", "S", List.of("minecraft:bread", "minecraft:no_existe"), 1, 1, null)));

        // con condiciones se revisa la tarea de adentro
        assertEquals("el mob no existe", availability.problem(new ConditionalTask("c",
                new KillTask("k", "X", "minecraft:no_existe", 1), new TimeOfDay(true))));
        assertNull(availability.problem(new ConditionalTask("c",
                new ItemTask("i", "Pan", "minecraft:bread", 1), new TimeOfDay(true))));
    }

    @Test
    void estadisticas() {
        assertNull(availability.problem(estadistica("minecraft:jump", null)));
        assertNull(availability.problem(estadistica("minecraft:mine_block", "minecraft:stone")));
        assertNull(availability.problem(estadistica("minecraft:kill_entity", "minecraft:zombie")));
        assertEquals("la estadística no existe", availability.problem(estadistica("minecraft:volar", null)));
        assertEquals("esta estadística no lleva de", availability.problem(estadistica("minecraft:jump", "minecraft:stone")));
        assertEquals("falta de (un bloque)", availability.problem(estadistica("minecraft:mine_block", null)));
        assertEquals("de tiene que ser un bloque",
                availability.problem(estadistica("minecraft:mine_block", "minecraft:rotten_flesh")));
        assertEquals("de tiene que ser un mob",
                availability.problem(estadistica("minecraft:kill_entity", "minecraft:stone")));
    }
}
