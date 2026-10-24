package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.condition.AllOf;
import com.shyNkindguy.bingo.core.condition.AnyOf;
import com.shyNkindguy.bingo.core.condition.BelowY;
import com.shyNkindguy.bingo.core.condition.InBiome;
import com.shyNkindguy.bingo.core.condition.InDimension;
import com.shyNkindguy.bingo.core.condition.NoDeath;
import com.shyNkindguy.bingo.core.condition.TimeOfDay;
import com.shyNkindguy.bingo.core.task.AdvancementTask;
import com.shyNkindguy.bingo.core.task.ConditionalTask;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.KillTask;
import com.shyNkindguy.bingo.core.task.SetTask;
import com.shyNkindguy.bingo.core.task.StatisticTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogParserTest {

    private static Object yaml(String text) {
        return new Yaml(new SafeConstructor(new LoaderOptions())).load(text);
    }

    private static TaskList lista(String archivo, String texto) {
        return CatalogParser.parseTaskList(archivo, yaml(texto));
    }

    private static ItemTask tarea(String linea) {
        return (ItemTask) cualquiera(linea);
    }

    private static Task cualquiera(String linea) {
        return CatalogParser.parseTask(yaml(linea), "prueba");
    }

    private static String error(Runnable accion) {
        return assertThrows(CatalogException.class, accion::run).getMessage();
    }

    private static final String BASICOS = """
            lista: basicos
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

    private static Catalog catalogo(String cartillas) {
        List<TaskList> listas = List.of(lista("tasks/basicos.yml", BASICOS), lista("tasks/dificiles.yml", DIFICILES));
        return CatalogParser.parseCatalog("cards.yml", yaml(cartillas), listas);
    }

    @Test
    void unaListaTraeNombreTierYTareas() {
        TaskList basicos = lista("tasks/basicos.yml", BASICOS);

        assertEquals("basicos", basicos.name());
        assertEquals(Tier.D, basicos.tier());
        assertEquals(7, basicos.tasks().size());
        assertEquals(new ItemTask("item:oak_log", "Tronco de roble", "minecraft:oak_log", 16), basicos.tasks().get(0));
    }

    @Test
    void sinNombreDeListaSeUsaElDelArchivo() {
        assertEquals("dificiles", lista("tasks/dificiles.yml", DIFICILES).name());
    }

    @Test
    void laTareaPorDefectoEsDeItemConCantidad1YClaveDeMinecraft() {
        ItemTask pan = tarea("{ item: bread, nombre: Pan }");

        assertEquals("minecraft:bread", pan.item());
        assertEquals(1, pan.amount());
        assertEquals("item:bread", pan.id());
    }

    @Test
    void elItemPuedeVenirConNamespaceYEnMayusculas() {
        assertEquals("minecraft:diamond", tarea("{ item: minecraft:DIAMOND, nombre: Diamante }").item());
        assertEquals("otro:cosa", tarea("{ tipo: item, item: otro:cosa, nombre: Cosa }").item());
        assertEquals("item:otro:cosa", tarea("{ item: otro:cosa, nombre: Cosa }").id());
    }

    @Test
    void elIdSePuedeDarAMano() {
        assertEquals("troncos64", tarea("{ id: troncos64, item: oak_log, nombre: Troncos, cantidad: 64 }").id());
    }

    @Test
    void erroresDeUnaTareaDicenDondeYQue() {
        assertTrue(error(() -> lista("tasks/x.yml", "tier: D\ntareas:\n  - { item: bread }"))
                .startsWith("tasks/x.yml, tarea 1: falta nombre"));
        assertTrue(error(() -> tarea("{ nombre: Pan }")).contains("falta item"));
        assertTrue(error(() -> tarea("{ item: bread, nombre: Pan, cantidad: 0 }")).contains("mayor que 0"));
        assertTrue(error(() -> tarea("{ item: bread, nombre: Pan, cantidad: diez }")).contains("número entero"));
        assertTrue(error(() -> tarea("{ item: bread, nombre: Pan, cantidad: 1.5 }")).contains("número entero"));
        assertTrue(error(() -> tarea("{ item: pan de molde, nombre: Pan }")).contains("no es una clave de Minecraft"));
        assertTrue(error(() -> tarea("{ tipo: volar, item: zombie, nombre: Zombi }")).contains("el tipo \"volar\" no existe"));
    }

    @Test
    void unaTareaDeMatar() {
        assertEquals(new KillTask("kill:zombie", "Zombi", "minecraft:zombie", 3),
                cualquiera("{ tipo: matar, mob: zombie, nombre: Zombi, cantidad: 3 }"));
        assertEquals(new KillTask("kill:creeper", "Creeper", "minecraft:creeper", 1, "minecraft:creeper_head"),
                cualquiera("{ tipo: kill, mob: creeper, nombre: Creeper, icono: creeper_head }"));
        assertTrue(error(() -> cualquiera("{ tipo: matar, nombre: Zombi }")).contains("falta mob"));
        assertTrue(error(() -> cualquiera("{ tipo: matar, mob: zombie, item: bread, nombre: Zombi }"))
                .contains("no conozco \"item\""));
    }

    @Test
    void unaTareaDeLogro() {
        assertEquals(new AdvancementTask("advancement:story/enter_the_nether", "Al Nether", "minecraft:story/enter_the_nether"),
                cualquiera("{ tipo: logro, logro: story/enter_the_nether, nombre: Al Nether }"));
        assertEquals("minecraft:diamond", ((AdvancementTask) cualquiera(
                "{ tipo: advancement, logro: minecraft:story/mine_diamond, nombre: Diamantes, icono: diamond }")).icon());
        assertTrue(error(() -> cualquiera("{ tipo: logro, logro: story/x, nombre: X, cantidad: 2 }"))
                .contains("no conozco \"cantidad\""));
    }

    @Test
    void unaTareaDeEstadistica() {
        assertEquals(new StatisticTask("statistic:jump", "Saltos", "minecraft:jump", null, 100),
                cualquiera("{ tipo: estadistica, estadistica: jump, nombre: Saltos, cantidad: 100 }"));
        assertEquals(new StatisticTask("statistic:mine_block/stone", "Piedra", "minecraft:mine_block", "minecraft:stone", 64),
                cualquiera("{ tipo: statistic, estadistica: mine_block, de: stone, nombre: Piedra, cantidad: 64 }"));
        assertTrue(error(() -> cualquiera("{ tipo: estadistica, estadistica: jump, nombre: Saltos }"))
                .contains("falta cantidad"));
    }

    @Test
    void conjuntos() {
        SetTask lanas = (SetTask) cualquiera("{ tipo: alguno, nombre: 2 lanas, necesita: 2, de: [red_wool, blue_wool, black_wool] }");
        assertEquals(List.of("minecraft:red_wool", "minecraft:blue_wool", "minecraft:black_wool"), lanas.items());
        assertEquals(2, lanas.needed());
        assertEquals("any_of:2:red_wool+blue_wool+black_wool", lanas.id());

        SetTask herramientas = (SetTask) cualquiera("{ tipo: todos, nombre: Hierro, de: [iron_pickaxe, iron_axe], cantidad: 1 }");
        assertTrue(herramientas.all());
        assertEquals("all_of:iron_pickaxe+iron_axe", herramientas.id());
        assertEquals(1, ((SetTask) cualquiera("{ tipo: any_of, nombre: X, de: [a, b] }")).needed());

        assertTrue(error(() -> cualquiera("{ tipo: alguno, nombre: X, de: [a] }")).contains("al menos 2"));
        assertTrue(error(() -> cualquiera("{ tipo: alguno, nombre: X, de: [a, a] }")).contains("está dos veces"));
        assertTrue(error(() -> cualquiera("{ tipo: alguno, nombre: X, necesita: 3, de: [a, b] }"))
                .contains("pide 3 y en de hay 2"));
        assertTrue(error(() -> cualquiera("{ tipo: todos, nombre: X, necesita: 1, de: [a, b] }"))
                .contains("no conozco \"necesita\""));
    }

    @Test
    void condiciones() {
        Task chuleta = cualquiera("{ item: cooked_porkchop, nombre: Chuleta, condiciones: [ { dimension: nether } ] }");
        ConditionalTask condicional = (ConditionalTask) chuleta;
        assertEquals("item:cooked_porkchop[dimension=minecraft:the_nether]", chuleta.id());
        assertEquals(new InDimension(Set.of("minecraft:the_nether")), condicional.condition());
        assertTrue(chuleta.base() instanceof ItemTask);

        // varias en la lista (o en un mapa) = todas
        assertEquals(new AllOf(List.of(new TimeOfDay(true), new BelowY(-10), new NoDeath())),
                ((ConditionalTask) cualquiera("{ tipo: matar, mob: zombie, nombre: Z, "
                        + "condiciones: [ { hora: noche }, { debajo_de_y: -10, sin_morir: true } ] }")).condition());
        assertEquals(new TimeOfDay(false),
                ((ConditionalTask) cualquiera("{ item: bread, nombre: Pan, condiciones: { hora: día } }")).condition());
        assertEquals(new AnyOf(List.of(new InBiome(Set.of("minecraft:desert")), new InBiome(Set.of("minecraft:beach")))),
                ((ConditionalTask) cualquiera("{ item: sand, nombre: Arena, "
                        + "condiciones: [ { alguna: [ { bioma: desert }, { bioma: beach } ] } ] }")).condition());
        assertEquals(new InDimension(Set.of("minecraft:the_end", "minecraft:overworld")),
                ((ConditionalTask) cualquiera("{ item: bread, nombre: Pan, condiciones: [ { dimension: [end, overworld] } ] }"))
                        .condition());

        // con id a mano, ese
        assertEquals("pan_noche", cualquiera("{ id: pan_noche, item: bread, nombre: Pan, condiciones: [ { hora: noche } ] }").id());
    }

    @Test
    void erroresDeCondiciones() {
        assertTrue(error(() -> cualquiera("{ item: bread, nombre: Pan, condiciones: [ { dimension: luna } ] }"))
                .contains("no es una dimensión"));
        assertTrue(error(() -> cualquiera("{ item: bread, nombre: Pan, condiciones: [ { hora: tarde } ] }"))
                .contains("dia o noche"));
        assertTrue(error(() -> cualquiera("{ item: bread, nombre: Pan, condiciones: [ { sin_morir: false } ] }"))
                .contains("sin_morir: true"));
        assertTrue(error(() -> cualquiera("{ item: bread, nombre: Pan, condiciones: [ { volando: true } ] }"))
                .contains("no conozco \"volando\""));
        assertTrue(error(() -> cualquiera("{ item: bread, nombre: Pan, condiciones: [] }")).contains("vacía"));
        assertTrue(error(() -> cualquiera("{ item: bread, nombre: Pan, condiciones: [ { debajo_de_y: abajo } ] }"))
                .contains("número entero"));
    }

    @Test
    void unaClaveMalEscritaEsError() {
        String mensaje = error(() -> tarea("{ item: bread, nombre: Pan, cantida: 3 }"));
        assertTrue(mensaje.contains("no conozco \"cantida\""), mensaje);
        assertTrue(mensaje.contains("cantidad"), mensaje);
    }

    @Test
    void erroresDeUnaLista() {
        assertTrue(error(() -> lista("tasks/x.yml", "tareas:\n  - { item: bread, nombre: Pan }")).contains("falta tier"));
        assertTrue(error(() -> lista("tasks/x.yml", "tier: Z\ntareas:\n  - { item: bread, nombre: Pan }"))
                .contains("S, A, B, C o D"));
        assertTrue(error(() -> lista("tasks/x.yml", "tier: D\ntareas: []")).contains("vacía"));
        assertTrue(error(() -> lista("tasks/x.yml", "")).contains("vacío"));
        assertTrue(error(() -> lista("tasks/x.yml", "- a\n- b")).contains("tiene que ser un mapa"));
    }

    @Test
    void elCatalogoTraeCartillasYLaPorDefecto() {
        Catalog catalogo = catalogo("""
                por_defecto: chica
                cartillas:
                  grande:
                    tamaño: 3
                    listas: { basicos: { min: 3 }, dificiles: { min: 1 } }
                  chica:
                    tamano: 3
                    listas: { basicos: { max: 7 }, dificiles: 2 }
                """);

        assertEquals(List.of("basicos", "dificiles"), List.copyOf(catalogo.lists().keySet()));
        assertEquals(List.of("grande", "chica"), List.copyOf(catalogo.cards().keySet()));
        assertEquals("chica", catalogo.defaultCard());
        assertEquals(9, catalogo.tasks().size());

        CardSpec grande = catalogo.card("grande").orElseThrow();
        assertEquals(3, grande.size());
        assertEquals(new CardSpec.Quota(3, Integer.MAX_VALUE), grande.lists().get("basicos"));
        assertEquals(new CardSpec.Quota(1, Integer.MAX_VALUE), grande.lists().get("dificiles"));
        assertEquals(new CardSpec.Quota(0, 7), catalogo.card("chica").orElseThrow().lists().get("basicos"));
    }

    @Test
    void sinPorDefectoEsLaPrimeraCartilla() {
        Catalog catalogo = catalogo("cartillas:\n  unica: { tamaño: 3, listas: { basicos: 7, dificiles: 2 } }");
        assertEquals("unica", catalogo.defaultCard());
    }

    @Test
    void erroresDeCartillas() {
        assertTrue(error(() -> catalogo("cartillas:\n  a: { tamaño: 3, listas: { medios: 3 } }"))
                .contains("la lista medios no existe (hay: basicos, dificiles)"));
        assertTrue(error(() -> catalogo("cartillas:\n  a: { tamaño: 8, listas: { basicos: 3 } }"))
                .contains("de 3 a 7"));
        assertTrue(error(() -> catalogo("cartillas:\n  a: { listas: { basicos: 3 } }")).contains("falta tamaño"));
        assertTrue(error(() -> catalogo("cartillas:\n  a: { tamaño: 3, listas: { basicos: { min: 3, max: 2 } } }"))
                .contains("min (3) es mayor que max (2)"));
        assertTrue(error(() -> catalogo("cartillas:\n  a: { tamaño: 3, listas: { basicos: 8, dificiles: 2 } }"))
                .contains("pide mínimo 8 tareas de basicos y la lista tiene 7"));
        assertTrue(error(() -> catalogo("cartillas:\n  a: { tamaño: 3, listas: { basicos: 3 } }"))
                .contains("la cartilla tiene 9 casillas y sus listas dan como máximo 3 tareas"));
        assertTrue(error(() -> catalogo("por_defecto: otra\ncartillas:\n  a: { tamaño: 3, listas: { basicos: 7, dificiles: 2 } }"))
                .contains("no existe la cartilla otra"));
        assertTrue(error(() -> catalogo("cartillas: {}")).contains("no hay ninguna cartilla"));
    }

    @Test
    void losMinimosNoPuedenPasarDeLasCasillas() {
        List<TaskList> listas = List.of(lista("tasks/a.yml", """
                tier: D
                tareas:
                  - { item: a, nombre: A }
                  - { item: b, nombre: B }
                  - { item: c, nombre: C }
                  - { item: d, nombre: D }
                  - { item: e, nombre: E }
                  - { item: f, nombre: F }
                  - { item: g, nombre: G }
                  - { item: h, nombre: H }
                  - { item: i, nombre: I }
                  - { item: j, nombre: J }
                """));
        String mensaje = error(() -> CatalogParser.parseCatalog("cards.yml",
                yaml("cartillas:\n  a: { tamaño: 3, listas: { a: 10 } }"), listas));
        assertTrue(mensaje.contains("los mínimos suman 10 y la cartilla tiene 9 casillas"), mensaje);
    }

    @Test
    void unaTareaRepetidaEsError() {
        TaskList otra = lista("tasks/otra.yml", "tier: C\ntareas:\n  - { item: bread, nombre: Más pan }");
        String entreListas = error(() -> CatalogParser.parseCatalog("cards.yml", yaml("cartillas: {}"),
                List.of(lista("tasks/basicos.yml", BASICOS), otra)));
        assertTrue(entreListas.contains("item:bread está en las listas basicos y otra"), entreListas);

        TaskList doble = lista("tasks/doble.yml", "tier: C\ntareas:\n  - { item: bread, nombre: Pan }\n  - { item: bread, nombre: Pan }");
        String enLaMisma = error(() -> CatalogParser.parseCatalog("cards.yml", yaml("cartillas: {}"), List.of(doble)));
        assertTrue(enLaMisma.contains("dos veces en la lista doble"), enLaMisma);
    }

    @Test
    void dosListasConElMismoNombreEsError() {
        TaskList copia = lista("tasks/copia.yml", "lista: basicos\ntier: C\ntareas:\n  - { item: apple, nombre: Manzana }");
        assertTrue(error(() -> CatalogParser.parseCatalog("cards.yml", yaml("cartillas: {}"),
                List.of(lista("tasks/basicos.yml", BASICOS), copia))).contains("dos listas que se llaman basicos"));
    }

    @Test
    void sinListasEsError() {
        assertTrue(error(() -> CatalogParser.parseCatalog("cards.yml", yaml("cartillas: {}"), List.of()))
                .contains("No hay listas"));
    }
}
