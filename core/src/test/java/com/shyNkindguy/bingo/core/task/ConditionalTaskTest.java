package com.shyNkindguy.bingo.core.task;

import com.shyNkindguy.bingo.core.condition.InDimension;
import com.shyNkindguy.bingo.core.event.EventContext;
import com.shyNkindguy.bingo.core.event.ItemObtained;
import com.shyNkindguy.bingo.core.progress.TaskProgress;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionalTaskTest {

    private final ItemTask chuleta = new ItemTask("item:cooked_porkchop", "Chuleta", "minecraft:cooked_porkchop", 1);
    private final ConditionalTask enElNether = new ConditionalTask("item:cooked_porkchop[dimension=minecraft:the_nether]",
            chuleta, new InDimension(Set.of("minecraft:the_nether")));

    private static EventContext en(String dimension) {
        return new EventContext(dimension, null, null, null, 0);
    }

    @Test
    void soloAvanzaSiSeCumpleLaCondicion() {
        TaskProgress inicio = TaskProgress.start(1);
        ItemObtained chuletas = new ItemObtained("minecraft:cooked_porkchop", 1);

        assertSame(inicio, enElNether.apply(chuletas, en("minecraft:overworld"), inicio));
        assertSame(inicio, enElNether.apply(chuletas, inicio));
        assertTrue(enElNether.apply(chuletas, en("minecraft:the_nether"), inicio).isComplete());
    }

    @Test
    void seVeComoLaTareaDeAdentro() {
        assertEquals("Chuleta", enElNether.displayName());
        assertEquals("minecraft:cooked_porkchop", enElNether.icon());
        assertEquals(1, enElNether.target());
        assertSame(chuleta, enElNether.base());
        assertSame(chuleta, chuleta.base());
    }
}
