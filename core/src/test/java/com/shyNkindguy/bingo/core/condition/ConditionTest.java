package com.shyNkindguy.bingo.core.condition;

import com.shyNkindguy.bingo.core.event.EventContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionTest {

    // de día, en el desierto del overworld, a Y 64, sin morir
    private static final EventContext DESIERTO = new EventContext("minecraft:overworld", "minecraft:desert", 1000L, 64, 0);
    private static final EventContext NETHER_DE_NOCHE =
            new EventContext("minecraft:the_nether", "minecraft:nether_wastes", 18000L, 30, 2);

    @Test
    void dimension() {
        InDimension nether = new InDimension(Set.of("minecraft:the_nether"));
        assertTrue(nether.test(NETHER_DE_NOCHE));
        assertFalse(nether.test(DESIERTO));
        assertTrue(new InDimension(Set.of("minecraft:the_nether", "minecraft:overworld")).test(DESIERTO));
        assertFalse(nether.test(EventContext.NONE));
    }

    @Test
    void hora() {
        assertTrue(new TimeOfDay(false).test(DESIERTO));
        assertTrue(new TimeOfDay(true).test(NETHER_DE_NOCHE));
        assertFalse(new TimeOfDay(true).test(DESIERTO));
        assertFalse(new TimeOfDay(false).test(EventContext.NONE));

        assertFalse(TimeOfDay.isNight(12999));
        assertTrue(TimeOfDay.isNight(13000));
        assertTrue(TimeOfDay.isNight(22999));
        assertFalse(TimeOfDay.isNight(23000));
        assertTrue(TimeOfDay.isNight(24000 * 5 + 15000)); // el día 5 a la noche
    }

    @Test
    void bioma() {
        InBiome arena = new InBiome(Set.of("minecraft:desert", "minecraft:beach"));
        assertTrue(arena.test(DESIERTO));
        assertFalse(arena.test(NETHER_DE_NOCHE));
        assertFalse(arena.test(EventContext.NONE));
    }

    @Test
    void debajoDeY() {
        assertTrue(new BelowY(65).test(DESIERTO));
        assertFalse(new BelowY(64).test(DESIERTO));
        assertTrue(new BelowY(0).test(new EventContext(null, null, null, -12, 0)));
        assertFalse(new BelowY(0).test(EventContext.NONE));
    }

    @Test
    void sinMorir() {
        assertTrue(new NoDeath().test(DESIERTO));
        assertFalse(new NoDeath().test(NETHER_DE_NOCHE));
    }

    @Test
    void todasYAlguna() {
        Condition netherDeNoche = new AllOf(List.of(new InDimension(Set.of("minecraft:the_nether")), new TimeOfDay(true)));
        assertTrue(netherDeNoche.test(NETHER_DE_NOCHE));
        assertFalse(netherDeNoche.test(new EventContext("minecraft:the_nether", null, 1000L, 30, 0)));

        Condition desiertoONoche = new AnyOf(List.of(new InBiome(Set.of("minecraft:desert")), new TimeOfDay(true)));
        assertTrue(desiertoONoche.test(DESIERTO));
        assertTrue(desiertoONoche.test(NETHER_DE_NOCHE));
        assertFalse(desiertoONoche.test(new EventContext("minecraft:overworld", "minecraft:plains", 1000L, 64, 0)));

        assertThrows(IllegalArgumentException.class, () -> new AnyOf(List.of()));
    }

    @Test
    void lasClavesSonEstables() {
        assertEquals("dimension=minecraft:overworld,minecraft:the_nether",
                new InDimension(Set.of("minecraft:the_nether", "minecraft:overworld")).key());
        assertEquals("hora=noche&y<0", new AllOf(List.of(new TimeOfDay(true), new BelowY(0))).key());
        assertEquals("alguna(bioma=minecraft:desert|sin_morir)",
                new AnyOf(List.of(new InBiome(Set.of("minecraft:desert")), new NoDeath())).key());
    }
}
