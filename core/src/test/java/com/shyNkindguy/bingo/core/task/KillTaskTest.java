package com.shyNkindguy.bingo.core.task;

import com.shyNkindguy.bingo.core.event.EntityKilled;
import com.shyNkindguy.bingo.core.event.ItemObtained;
import com.shyNkindguy.bingo.core.progress.TaskProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KillTaskTest {

    private final KillTask zombis = new KillTask("kill:zombie", "Zombi", "minecraft:zombie", 3);

    @Test
    void cadaMuerteSumaUno() {
        TaskProgress p = TaskProgress.start(3);
        p = zombis.apply(new EntityKilled("minecraft:zombie"), p);
        p = zombis.apply(new EntityKilled("minecraft:zombie"), p);
        assertEquals(2, p.current());

        p = zombis.apply(new EntityKilled("minecraft:zombie"), p);
        assertTrue(p.isComplete());
        assertEquals(3, zombis.apply(new EntityKilled("minecraft:zombie"), p).current());
    }

    @Test
    void otroMobUOtroEventoNoLaMueven() {
        TaskProgress inicio = TaskProgress.start(3);
        assertSame(inicio, zombis.apply(new EntityKilled("minecraft:skeleton"), inicio));
        assertSame(inicio, zombis.apply(new ItemObtained("minecraft:zombie", 5), inicio));
    }

    @Test
    void nombreEIconoPorDefecto() {
        assertEquals("3 × Zombi", zombis.displayName());
        assertEquals("minecraft:zombie_spawn_egg", zombis.icon());
        assertEquals("minecraft:zombie_head",
                new KillTask("kill:zombie", "Zombi", "minecraft:zombie", 1, "minecraft:zombie_head").icon());
    }

    @Test
    void cantidadCeroEsError() {
        assertThrows(IllegalArgumentException.class, () -> new KillTask("x", "X", "minecraft:x", 0));
    }
}
