package com.shyNkindguy.bingo.core.task;

import com.shyNkindguy.bingo.core.event.AdvancementDone;
import com.shyNkindguy.bingo.core.progress.TaskProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancementTaskTest {

    private final AdvancementTask nether =
            new AdvancementTask("advancement:story/enter_the_nether", "Entrar al Nether", "minecraft:story/enter_the_nether");

    @Test
    void elLogroLaCompleta() {
        TaskProgress p = nether.apply(new AdvancementDone("minecraft:story/enter_the_nether"), TaskProgress.start(1));
        assertTrue(p.isComplete());
    }

    @Test
    void otroLogroNoLaMueve() {
        TaskProgress inicio = TaskProgress.start(1);
        assertSame(inicio, nether.apply(new AdvancementDone("minecraft:story/mine_diamond"), inicio));
    }

    @Test
    void sinIconoElPluginUsaElDelLogro() {
        assertNull(nether.icon());
        assertEquals(1, nether.target());
        assertEquals("Entrar al Nether", nether.displayName());
    }
}
