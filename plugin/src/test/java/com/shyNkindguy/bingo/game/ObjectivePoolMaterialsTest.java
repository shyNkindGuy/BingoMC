package com.shyNkindguy.bingo.game;

import com.shyNkindguy.bingo.core.card.ObjectivePool;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// Corre contra el paper-api real del CI: atrapa nombres de ítem mal escritos en el pool.
class ObjectivePoolMaterialsTest {

    @Test
    void cadaObjetivoEsUnItemDeMinecraft() {
        for (ObjectivePool.Entry entry : ObjectivePool.DEFAULT) {
            assertNotNull(Material.matchMaterial(entry.item()), "No existe en Minecraft: " + entry.item());
        }
    }
}
