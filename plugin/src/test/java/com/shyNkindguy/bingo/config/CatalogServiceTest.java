package com.shyNkindguy.bingo.config;

import com.shyNkindguy.bingo.core.catalog.Catalog;
import com.shyNkindguy.bingo.core.catalog.CatalogLoader;
import com.shyNkindguy.bingo.core.catalog.CatalogParser;
import com.shyNkindguy.bingo.core.catalog.TaskList;
import com.shyNkindguy.bingo.core.catalog.Tier;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Corre contra el paper-api real del CI: atrapa nombres de ítem mal escritos en los YAML por defecto.
class CatalogServiceTest {

    @Test
    void cadaTareaPorDefectoEsUnItemDeMinecraft() {
        for (Task task : CatalogLoader.defaults().tasks()) {
            assertTrue(CatalogService.isAvailable(task), "No existe en Minecraft: " + task.icon());
        }
    }

    @Test
    void unItemQueNoExisteNoEstaDisponible() {
        assertFalse(CatalogService.isAvailable(new ItemTask("item:no_existe", "Fantasma", "minecraft:no_existe", 1)));
    }

    @Test
    void elResumenCuentaListasTareasYCartillas() {
        assertEquals(List.of("3 listas, 43 tareas; cartillas: normal 5×5 (por defecto), rapida 3×3"),
                CatalogService.summary(CatalogLoader.defaults()));
    }

    @Test
    void elResumenAvisaLasTareasQueNoVanASalir() {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 9; i++) tasks.add(new ItemTask("item:bread" + i, "Pan " + i, "minecraft:bread", 1));
        tasks.add(new ItemTask("item:no_existe", "Fantasma", "minecraft:no_existe", 1));
        Catalog catalog = CatalogParser.parseCatalog("cards.yml",
                Map.of("cartillas", Map.of("chica", Map.of("tamaño", 3, "listas", Map.of("pan", 9)))),
                List.of(new TaskList("pan", Tier.D, tasks)));

        List<String> summary = CatalogService.summary(catalog);
        assertEquals(2, summary.size());
        assertEquals("No existen en esta versión y no van a salir: item:no_existe", summary.get(1));
    }
}
