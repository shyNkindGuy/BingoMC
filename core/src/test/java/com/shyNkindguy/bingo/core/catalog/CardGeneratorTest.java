package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardGeneratorTest {

    // lista con `cantidad` tareas de ítems "minecraft:<nombre>_<i>"
    private static TaskList lista(String nombre, Tier tier, int cantidad) {
        List<Task> tareas = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            String item = "minecraft:" + nombre + "_" + i;
            tareas.add(new ItemTask("item:" + nombre + "_" + i, nombre + " " + i, item, 1));
        }
        return new TaskList(nombre, tier, tareas);
    }

    private static final Map<String, TaskList> LISTAS = new LinkedHashMap<>();

    static {
        LISTAS.put("faciles", lista("faciles", Tier.D, 15));
        LISTAS.put("medias", lista("medias", Tier.C, 15));
        LISTAS.put("dificiles", lista("dificiles", Tier.S, 5));
    }

    private static CardSpec cartilla(int tamano, int minFaciles, int maxFaciles, int minMedias, int maxMedias,
                                     int minDificiles, int maxDificiles) {
        Map<String, CardSpec.Quota> cupos = new LinkedHashMap<>();
        cupos.put("faciles", new CardSpec.Quota(minFaciles, maxFaciles));
        cupos.put("medias", new CardSpec.Quota(minMedias, maxMedias));
        cupos.put("dificiles", new CardSpec.Quota(minDificiles, maxDificiles));
        return new CardSpec("prueba", tamano, cupos);
    }

    private static final CardSpec NORMAL = cartilla(5, 8, 12, 8, 14, 2, 4);

    private static long deLaLista(List<Task> tareas, String lista) {
        return tareas.stream().filter(tarea -> tarea.id().startsWith("item:" + lista + "_")).count();
    }

    @Test
    void llenaLasCasillasSinRepetir() {
        List<Task> tareas = CardGenerator.generate(NORMAL, LISTAS, new Random(1), tarea -> true);

        assertEquals(25, tareas.size());
        Set<String> ids = new HashSet<>();
        for (Task tarea : tareas) assertTrue(ids.add(tarea.id()), "Repetida: " + tarea.id());
    }

    @Test
    void respetaElMinimoYElMaximoDeCadaLista() {
        for (long semilla = 0; semilla < 300; semilla++) {
            List<Task> tareas = CardGenerator.generate(NORMAL, LISTAS, new Random(semilla), tarea -> true);
            long faciles = deLaLista(tareas, "faciles");
            long medias = deLaLista(tareas, "medias");
            long dificiles = deLaLista(tareas, "dificiles");

            assertTrue(faciles >= 8 && faciles <= 12, "fáciles: " + faciles + " (semilla " + semilla + ")");
            assertTrue(medias >= 8 && medias <= 14, "medias: " + medias + " (semilla " + semilla + ")");
            assertTrue(dificiles >= 2 && dificiles <= 4, "difíciles: " + dificiles + " (semilla " + semilla + ")");
        }
    }

    @Test
    void conMaximosJustosSaleExactamenteElMaximo() {
        CardSpec justa = cartilla(5, 0, 12, 0, 9, 0, 4);
        List<Task> tareas = CardGenerator.generate(justa, LISTAS, new Random(3), tarea -> true);

        assertEquals(12, deLaLista(tareas, "faciles"));
        assertEquals(9, deLaLista(tareas, "medias"));
        assertEquals(4, deLaLista(tareas, "dificiles"));
    }

    @Test
    void unaListaConMinimo0YMaximo0NoDaNada() {
        CardSpec sinDificiles = cartilla(3, 0, 9, 0, 9, 0, 0);
        assertEquals(0, deLaLista(CardGenerator.generate(sinDificiles, LISTAS, new Random(5), tarea -> true), "dificiles"));
    }

    @Test
    void laMismaSemillaDaLasMismasTareasEnElMismoOrden() {
        assertEquals(CardGenerator.generate(NORMAL, LISTAS, new Random(42), tarea -> true),
                CardGenerator.generate(NORMAL, LISTAS, new Random(42), tarea -> true));
        assertNotEquals(CardGenerator.generate(NORMAL, LISTAS, new Random(42), tarea -> true),
                CardGenerator.generate(NORMAL, LISTAS, new Random(43), tarea -> true));
    }

    @Test
    void salteaLasTareasQueNoEstanDisponibles() {
        List<Task> tareas = CardGenerator.generate(NORMAL, LISTAS, new Random(9),
                tarea -> !tarea.id().equals("item:dificiles_0"));

        assertEquals(25, tareas.size());
        assertTrue(tareas.stream().noneMatch(tarea -> tarea.id().equals("item:dificiles_0")));
    }

    @Test
    void sinSuficientesDisponiblesParaElMinimoEsError() {
        String mensaje = assertThrows(CatalogException.class, () -> CardGenerator.generate(NORMAL, LISTAS,
                new Random(), tarea -> !tarea.id().startsWith("item:dificiles_"))).getMessage();
        assertTrue(mensaje.contains("pide mínimo 2 tareas de dificiles y solo hay 0 disponibles"), mensaje);
    }

    @Test
    void sinSuficientesDisponiblesParaLlenarEsError() {
        // 12 + 9 + 4 = 25 justas; sin dos de las difíciles quedan 24
        CardSpec justa = cartilla(5, 0, 12, 0, 9, 0, 4);
        Set<String> faltan = Set.of("item:dificiles_0", "item:dificiles_1");
        String mensaje = assertThrows(CatalogException.class, () -> CardGenerator.generate(justa, LISTAS,
                new Random(), tarea -> !faltan.contains(tarea.id()))).getMessage();
        assertTrue(mensaje.contains("necesita 25 tareas y solo alcanzan"), mensaje);
    }

    @Test
    void minimosQuePasanDeLasCasillasEsError() {
        CardSpec imposible = cartilla(3, 5, 5, 5, 5, 0, 0);
        assertThrows(CatalogException.class, () -> CardGenerator.generate(imposible, LISTAS, new Random(), tarea -> true));
    }

    @Test
    void unaListaQueNoExisteEsError() {
        CardSpec rara = new CardSpec("rara", 3, Map.of("no_existe", new CardSpec.Quota(0, 9)));
        assertThrows(CatalogException.class, () -> CardGenerator.generate(rara, LISTAS, new Random(), tarea -> true));
    }
}
