package com.shyNkindguy.bingo.icon;

import com.shyNkindguy.bingo.core.task.AdvancementTask;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.KillTask;
import com.shyNkindguy.bingo.core.task.StatisticTask;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
