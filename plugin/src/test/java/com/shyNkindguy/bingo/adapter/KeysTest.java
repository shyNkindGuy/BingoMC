package com.shyNkindguy.bingo.adapter;

import com.shyNkindguy.bingo.core.card.ObjectivePool;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KeysTest {

    @Test
    void unMaterialSeVuelveClaveDeMinecraft() {
        assertEquals("minecraft:diamond", Keys.of(Material.DIAMOND));
        assertEquals("minecraft:oak_log", Keys.of(Material.OAK_LOG));
    }

    @Test
    void cadaItemDelPoolVaYVuelveIgual() {
        for (ObjectivePool.Entry entry : ObjectivePool.DEFAULT) {
            assertEquals(entry.item(), Keys.of(Keys.toMaterial(entry.item())));
        }
    }

    @Test
    void unaClaveQueNoExisteDaNull() {
        assertNull(Keys.toMaterial("minecraft:no_existe"));
    }
}
