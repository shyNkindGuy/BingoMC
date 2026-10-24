package com.shyNkindguy.bingo.adapter;

import com.shyNkindguy.bingo.core.catalog.CatalogLoader;
import com.shyNkindguy.bingo.core.task.Task;
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
    void cadaItemDeLasTareasPorDefectoVaYVuelveIgual() {
        for (Task task : CatalogLoader.defaults().tasks()) {
            assertEquals(task.icon(), Keys.of(Keys.toMaterial(task.icon())));
        }
    }

    @Test
    void unaClaveQueNoExisteDaNull() {
        assertNull(Keys.toMaterial("minecraft:no_existe"));
    }
}
