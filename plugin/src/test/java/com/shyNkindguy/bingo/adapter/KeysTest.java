package com.shyNkindguy.bingo.adapter;

import com.shyNkindguy.bingo.core.catalog.CatalogLoader;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.SetTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
            if (task.base() instanceof ItemTask item) {
                assertEquals(item.item(), Keys.of(Keys.toMaterial(item.item())));
            }
            if (task.base() instanceof SetTask set) {
                for (String item : set.items()) assertEquals(item, Keys.of(Keys.toMaterial(item)));
            }
        }
    }

    @Test
    void cadaIconoDeLasTareasPorDefectoEsUnItem() {
        for (Task task : CatalogLoader.defaults().tasks()) {
            if (task.icon() != null) assertNotNull(Keys.toMaterial(task.icon()), "Ícono que no existe: " + task.icon());
        }
    }

    @Test
    void unaClaveQueNoExisteDaNull() {
        assertNull(Keys.toMaterial("minecraft:no_existe"));
        assertNull(Keys.toEntityType("minecraft:no_existe"));
        assertNull(Keys.toStatistic("minecraft:no_existe"));
    }

    @Test
    void mobsYEstadisticasVanYVuelven() {
        assertEquals("minecraft:zombie", Keys.of(EntityType.ZOMBIE));
        assertEquals(EntityType.ZOMBIE, Keys.toEntityType("minecraft:zombie"));
        assertNull(Keys.of(EntityType.UNKNOWN));
        assertEquals(Statistic.JUMP, Keys.toStatistic("minecraft:jump"));
        assertEquals(Statistic.MINE_BLOCK, Keys.toStatistic("minecraft:mine_block"));
    }
}
