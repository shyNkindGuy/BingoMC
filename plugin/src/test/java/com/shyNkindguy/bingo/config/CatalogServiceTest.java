package com.shyNkindguy.bingo.config;

import com.shyNkindguy.bingo.adapter.Keys;
import com.shyNkindguy.bingo.core.achievement.Achievement;
import com.shyNkindguy.bingo.core.catalog.Catalog;
import com.shyNkindguy.bingo.core.catalog.CatalogLoader;
import com.shyNkindguy.bingo.core.catalog.CatalogParser;
import com.shyNkindguy.bingo.core.catalog.TaskList;
import com.shyNkindguy.bingo.core.catalog.Tier;
import com.shyNkindguy.bingo.core.item.ItemEffect;
import com.shyNkindguy.bingo.core.item.SpecialItem;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

// Corre contra el paper-api real del CI: atrapa ítems, mobs y estadísticas mal escritos en los YAML por
// defecto. Lo que pide un servidor no se puede revisar: de los logros solo se mira que tengan forma de
// logro, y cualquier Material vale como bloque o ítem.
class CatalogServiceTest {

    private static final TaskAvailability SIN_SERVIDOR = new TaskAvailability(
            key -> key.matches("minecraft:(story|nether|end|adventure|husbandry)/[a-z_]+"),
            material -> true, material -> true);

    @Test
    void cadaTareaPorDefectoSePuedeHacer() {
        for (Task task : CatalogLoader.defaults().tasks()) {
            assertNull(SIN_SERVIDOR.problem(task), "No se puede hacer: " + task.id());
        }
    }

    @Test
    void elResumenCuentaListasTareasYCartillas() {
        assertEquals(List.of("3 listas, 67 tareas; cartillas: normal 5×5 (por defecto), rapida 3×3"),
                CatalogService.summary(CatalogLoader.defaults(), SIN_SERVIDOR));
    }

    @Test
    void elResumenAvisaLasTareasQueNoVanASalir() {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < 9; i++) tasks.add(new ItemTask("item:bread" + i, "Pan " + i, "minecraft:bread", 1));
        tasks.add(new ItemTask("item:no_existe", "Fantasma", "minecraft:no_existe", 1));
        Catalog catalog = CatalogParser.parseCatalog("cards.yml",
                Map.of("cartillas", Map.of("chica", Map.of("tamaño", 3, "listas", Map.of("pan", 9)))),
                List.of(new TaskList("pan", Tier.D, tasks)));

        List<String> summary = CatalogService.summary(catalog, SIN_SERVIDOR);
        assertEquals(2, summary.size());
        assertEquals("No se pueden hacer en esta versión y no van a salir: item:no_existe (el ítem no existe)",
                summary.get(1));
    }

    @Test
    void losItemsEspecialesYLosIconosDeLosLogrosPorDefectoExisten() {
        assertEquals(List.of(), CatalogService.itemProblems(CatalogLoader.defaultItems()));
        assertEquals(List.of(), CatalogService.pieceProblems(CatalogLoader.defaultSets()));
        for (Achievement achievement : CatalogLoader.defaultAchievements(CatalogLoader.defaults())) {
            if (achievement.icon() != null) {
                assertNotNull(Keys.toMaterial(achievement.icon()), "No existe el ícono de " + achievement.id());
            }
        }
    }

    @Test
    void avisaLosItemsEspecialesQueNoExisten() {
        SpecialItem fantasma = new SpecialItem("fantasma", "Fantasma", "No existe", "minecraft:no_existe",
                new ItemEffect.Reroll());
        assertEquals(List.of("§eitems.yml: fantasma usa minecraft:no_existe, que no existe en esta versión (sale como papel)"),
                CatalogService.itemProblems(List.of(fantasma)));
    }
}
