package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.status.ItemSet;
import com.shyNkindguy.bingo.core.status.PlayerStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetParserTest {

    private static List<ItemSet> sets(String texto) {
        return SetParser.parse("sets.yml", new Yaml(new SafeConstructor(new LoaderOptions())).load(texto));
    }

    private static String error(String texto) {
        return assertThrows(CatalogException.class, () -> sets(texto)).getMessage();
    }

    @Test
    void losPorDefecto() {
        List<ItemSet> sets = CatalogLoader.defaultSets();
        assertEquals(List.of("explorador", "minero", "nadador", "gloton"), sets.stream().map(ItemSet::id).toList());
        ItemSet explorador = sets.get(0);
        assertEquals(new ItemSet.Piece("minecraft:leather_boots", "Botas del explorador"), explorador.pieces().get(0));
        assertEquals(2, explorador.needed());
        assertEquals(List.of(PlayerStatus.parse("velocidad"), PlayerStatus.parse("vision_nocturna")), explorador.statuses());
    }

    @Test
    void sinNecesitaPideTodas() {
        ItemSet set = sets("conjuntos:\n  - id: a\n    nombre: A\n    estados: [salto]\n    piezas:\n"
                + "      - { item: stick, nombre: Palo }\n      - { item: bowl, nombre: Cuenco }\n").get(0);
        assertEquals(2, set.needed());
    }

    @Test
    void validaConMensajesQueDicenDonde() {
        String base = "conjuntos:\n  - id: a\n    nombre: A\n    piezas:\n      - { item: stick, nombre: Palo }\n";
        assertEquals("sets.yml, conjunto 1: falta estados", error(base));
        assertEquals("sets.yml, conjunto 1: estados: está vacía (un conjunto sin estados no hace nada)", error(base + "    estados: []\n"));
        assertTrue(error(base + "    estados: [volar]\n").startsWith("sets.yml, conjunto 1: estados 1: \"volar\" no es un estado"));
        assertEquals("sets.yml, conjunto 1: necesita: hay 1 piezas y pide 2", error(base + "    estados: [salto]\n    necesita: 2\n"));
        assertEquals("sets.yml, conjunto 1, pieza 1: falta nombre",
                error("conjuntos:\n  - id: a\n    nombre: A\n    estados: [salto]\n    piezas:\n      - { item: stick }\n"));
        assertEquals("sets.yml, conjunto 1: no conozco \"efecto\" (se puede usar: estados, id, necesita, nombre, piezas)",
                error(base + "    estados: [salto]\n    efecto: x\n"));
        assertEquals("sets.yml, conjunto 2: el id a ya está en otro conjunto",
                error(base + "    estados: [salto]\n  - id: a\n    nombre: B\n    estados: [salto]\n    piezas:\n      - { item: bowl, nombre: C }\n"));
    }

    @Test
    void sinArchivoNoHayNinguno(@TempDir Path carpeta) {
        assertEquals(List.of(), CatalogLoader.setsFromDirectory(carpeta));
    }
}
