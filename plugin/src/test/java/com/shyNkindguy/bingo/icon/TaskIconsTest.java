package com.shyNkindguy.bingo.icon;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskIconsTest {

    private final ItemTask pan = new ItemTask("i", "Pan", "minecraft:bread", 1);
    private final KillTask zombi = new KillTask("k", "Zombi", "minecraft:zombie", 3, "minecraft:rotten_flesh");
    private final AdvancementTask nether = new AdvancementTask("a", "Nether", "minecraft:story/enter_the_nether", "minecraft:obsidian");
    private final StatisticTask piedra = new StatisticTask("s", "Piedra", "minecraft:mine_block", "minecraft:stone", 64);
    private final StatisticTask saltos = new StatisticTask("s", "Saltos", "minecraft:jump", null, 100);

    @Test
    void elIconoEsElItemQueDiceLaTarea() {
        assertEquals(Material.BREAD, TaskIcons.material(pan));
        assertEquals(Material.ROTTEN_FLESH, TaskIcons.material(zombi));
        assertEquals(Material.OBSIDIAN, TaskIcons.material(nether));
        assertEquals(Material.STONE, TaskIcons.material(piedra));
        assertNull(TaskIcons.material(saltos));
    }

    @Test
    void unConjuntoSinIconoDibujaSusDosPrimerosItems() {
        SetTask lanas = new SetTask("s", "Lanas", List.of("minecraft:red_wool", "minecraft:blue_wool",
                "minecraft:black_wool"), 2, 1, null);
        assertEquals(Material.RED_WOOL, TaskIcons.material(lanas));
        assertEquals(Material.BLUE_WOOL, TaskIcons.secondMaterial(lanas));
        assertNull(TaskIcons.badge(lanas));

        SetTask conIcono = new SetTask("s", "Lanas", lanas.items(), 2, 1, "minecraft:white_wool");
        assertEquals(Material.WHITE_WOOL, TaskIcons.material(conIcono));
        assertNull(TaskIcons.secondMaterial(conIcono));
        assertNull(TaskIcons.secondMaterial(pan));
    }

    @Test
    void conCondicionesSeDibujaComoLaDeAdentroYConPunto() {
        ConditionalTask deNoche = new ConditionalTask("c", zombi, new TimeOfDay(true));
        assertEquals(Material.ROTTEN_FLESH, TaskIcons.material(deNoche));
        assertSame(TaskIcons.badge(zombi), TaskIcons.badge(deNoche));
        assertTrue(TaskIcons.hasConditions(deNoche));
        assertFalse(TaskIcons.hasConditions(zombi));
    }

    @Test
    void cadaTipoTieneSuMarcaYLosItemsNinguna() {
        assertNull(TaskIcons.badge(pan));
        assertNotNull(TaskIcons.badge(zombi));
        assertNotNull(TaskIcons.badge(nether));
        assertNotNull(TaskIcons.badge(saltos));
        assertNotEquals(TaskIcons.badge(zombi), TaskIcons.badge(nether));
        assertNotEquals(TaskIcons.badge(nether), TaskIcons.badge(saltos));
        assertEquals(5, TaskIcons.badge(zombi).getWidth());
    }
}
