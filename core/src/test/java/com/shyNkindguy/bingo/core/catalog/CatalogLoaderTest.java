package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.game.GameSettings;
import com.shyNkindguy.bingo.core.task.AdvancementTask;
import com.shyNkindguy.bingo.core.task.ConditionalTask;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.KillTask;
import com.shyNkindguy.bingo.core.task.SetTask;
import com.shyNkindguy.bingo.core.task.StatisticTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogLoaderTest {

    @TempDir
    Path carpeta;

    private void escribir(String archivo, String texto) throws IOException {
        Path destino = carpeta.resolve(archivo);
        Files.createDirectories(destino.getParent());
        Files.writeString(destino, texto, StandardCharsets.UTF_8);
    }

    private static final String CARTILLAS = """
            cartillas:
              chica:
                tamaño: 3
                listas: { faciles: { min: 5 }, dificiles: { min: 1 } }
            """;

    private static final String FACILES = """
            tier: D
            tareas:
              - { item: oak_log, nombre: Tronco de roble, cantidad: 16 }
              - { item: bread, nombre: Pan }
              - { item: coal, nombre: Carbón }
              - { item: apple, nombre: Manzana }
              - { item: torch, nombre: Antorcha }
              - { item: glass, nombre: Vidrio }
              - { item: flint, nombre: Pedernal }
            """;

    private static final String DIFICILES = """
            tier: S
            tareas:
              - { item: diamond, nombre: Diamante }
              - { item: obsidian, nombre: Obsidiana }
            """;

    @Test
    void leeLaCarpetaConCardsYTasks() throws IOException {
        escribir("cards.yml", CARTILLAS);
        escribir("tasks/faciles.yml", FACILES);
        escribir("tasks/dificiles.yml", DIFICILES);
        escribir("tasks/notas.txt", "esto no es una lista");

        Catalog catalogo = CatalogLoader.fromDirectory(carpeta);

        // los archivos se leen en orden alfabético
        assertEquals(List.of("dificiles", "faciles"), List.copyOf(catalogo.lists().keySet()));
        assertEquals("chica", catalogo.defaultCard());
        assertEquals("Carbón", ((ItemTask) catalogo.lists().get("faciles").tasks().get(2)).name());
        assertEquals(9, catalogo.generate("chica", new Random(1), tarea -> true).size());
    }

    @Test
    void sinCardsEsError() throws IOException {
        escribir("tasks/faciles.yml", FACILES);
        assertTrue(assertThrows(CatalogException.class, () -> CatalogLoader.fromDirectory(carpeta))
                .getMessage().startsWith("Falta cards.yml"));
    }

    @Test
    void sinCarpetaDeTareasEsError() throws IOException {
        escribir("cards.yml", CARTILLAS);
        assertTrue(assertThrows(CatalogException.class, () -> CatalogLoader.fromDirectory(carpeta))
                .getMessage().contains("No hay listas"));
    }

    @Test
    void unYamlMalEscritoDiceQueArchivoEs() throws IOException {
        escribir("cards.yml", CARTILLAS);
        escribir("tasks/faciles.yml", "tier: D\ntareas:\n  - { item: bread, nombre: Pan\n");

        String mensaje = assertThrows(CatalogException.class, () -> CatalogLoader.fromDirectory(carpeta)).getMessage();
        assertTrue(mensaje.startsWith("tasks/faciles.yml: YAML mal escrito"), mensaje);
    }

    @Test
    void unArchivoQueNoEstaEnUtf8SeAvisa() throws IOException {
        escribir("cards.yml", CARTILLAS);
        Files.createDirectories(carpeta.resolve("tasks"));
        Files.write(carpeta.resolve("tasks/faciles.yml"),
                "tier: D\ntareas:\n  - { item: coal, nombre: Carbón }\n".getBytes(StandardCharsets.ISO_8859_1));

        String mensaje = assertThrows(CatalogException.class, () -> CatalogLoader.fromDirectory(carpeta)).getMessage();
        assertTrue(mensaje.contains("UTF-8"), mensaje);
    }

    @Test
    void laPrimeraVezCopiaLosArchivosPorDefecto() throws IOException {
        List<String> copiados = CatalogLoader.copyDefaults(carpeta);

        assertEquals(CatalogLoader.DEFAULT_FILES, copiados);
        assertEquals(CatalogLoader.defaults(), CatalogLoader.fromDirectory(carpeta));
        assertEquals(List.of(), CatalogLoader.copyDefaults(carpeta));
    }

    @Test
    void sinConfigSeJuegaConLaConfiguracionPorDefecto() throws IOException {
        escribir("cards.yml", CARTILLAS);
        escribir("tasks/faciles.yml", FACILES);
        escribir("tasks/dificiles.yml", DIFICILES);
        Catalog catalogo = CatalogLoader.fromDirectory(carpeta);
        assertEquals(GameSettings.DEFAULT, CatalogLoader.settingsFromDirectory(carpeta, catalogo));

        escribir("config.yml", "cartilla: chica\nduracion: 5");
        GameSettings config = CatalogLoader.settingsFromDirectory(carpeta, catalogo);
        assertEquals("chica", config.card());
        assertEquals(5, config.durationMinutes());
        assertEquals(GameSettings.DEFAULT.countdownSeconds(), config.countdownSeconds());
    }

    @Test
    void noPisaLoQueYaEstaNiDevuelveUnaListaBorrada() throws IOException {
        CatalogLoader.copyDefaults(carpeta);
        escribir("cards.yml", CARTILLAS);
        Files.delete(carpeta.resolve("tasks/dificiles.yml"));

        assertEquals(List.of(), CatalogLoader.copyDefaults(carpeta));
        assertEquals(CARTILLAS, Files.readString(carpeta.resolve("cards.yml")));
        assertFalse(Files.exists(carpeta.resolve("tasks/dificiles.yml")));

        Files.delete(carpeta.resolve("cards.yml"));
        assertEquals(List.of("cards.yml"), CatalogLoader.copyDefaults(carpeta));
    }

    // Lo que antes probaba ObjectivePoolTest, ahora sobre los YAML que vienen en el .jar.
    @Test
    void lasTareasPorDefectoAlcanzanYNoSeRepiten() {
        Catalog catalogo = CatalogLoader.defaults();
        List<Task> tareas = catalogo.tasks();

        assertEquals(List.of("basicos", "medios", "dificiles"), List.copyOf(catalogo.lists().keySet()));
        assertEquals(67, tareas.size());
        assertEquals("normal", catalogo.defaultCard());
        assertEquals(5, catalogo.card("normal").orElseThrow().size());
        assertEquals(3, catalogo.card("rapida").orElseThrow().size());

        Set<String> items = new HashSet<>();
        Set<String> nombres = new HashSet<>();
        for (Task tarea : tareas) {
            assertTrue(nombres.add(tarea.displayName()), "Nombre repetido: " + tarea.displayName());
            if (tarea.base() instanceof ItemTask item && !(tarea instanceof ConditionalTask)) {
                assertTrue(items.add(item.item()), "Ítem repetido: " + item.item());
                assertTrue(item.item().matches("minecraft:[a-z0-9_]+"), "No es una clave de ítem: " + item.item());
            }
        }
        assertTrue(tareas.stream().anyMatch(tarea -> tarea.target() > 1));
        // hay de cada tipo
        for (Class<?> tipo : List.of(ItemTask.class, KillTask.class, AdvancementTask.class, StatisticTask.class,
                SetTask.class)) {
            assertTrue(tareas.stream().anyMatch(tarea -> tipo.isInstance(tarea.base())),
                    "Falta una tarea de tipo " + tipo.getSimpleName());
        }
        assertTrue(tareas.stream().anyMatch(ConditionalTask.class::isInstance), "Falta una tarea con condiciones");
    }

    @Test
    void cadaCartillaPorDefectoSaleConCualquierSemilla() {
        Catalog catalogo = CatalogLoader.defaults();
        for (CardSpec cartilla : catalogo.cards().values()) {
            for (long semilla = 0; semilla < 200; semilla++) {
                List<Task> tareas = catalogo.generate(cartilla.name(), new Random(semilla), tarea -> true);
                assertEquals(cartilla.cells(), tareas.size());
                assertEquals(cartilla.cells(), tareas.stream().map(Task::id).distinct().count());
            }
        }
    }
}
