package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.item.ItemEffect;
import com.shyNkindguy.bingo.core.item.SpecialItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpecialItemParserTest {

    private static List<SpecialItem> items(String texto) {
        return SpecialItemParser.parse("items.yml", new Yaml(new SafeConstructor(new LoaderOptions())).load(texto));
    }

    private static String error(String texto) {
        return assertThrows(CatalogException.class, () -> items(texto)).getMessage();
    }

    @Test
    void losPorDefecto() {
        assertEquals(List.of(
                new SpecialItem("dado", "Dado del caos", "Cambia una casilla sin completar por otra tarea",
                        "minecraft:amethyst_shard", new ItemEffect.Reroll()),
                new SpecialItem("brujula", "Brújula del explorador", "Apunta al bioma que pide una casilla pendiente",
                        "minecraft:compass", new ItemEffect.BiomeCompass(2000)),
                new SpecialItem("escudo", "Escudo de la paz", "Ningún jugador te puede pegar durante 60 segundos",
                        "minecraft:heart_of_the_sea", new ItemEffect.PvpShield(60))), CatalogLoader.defaultItems());
    }

    @Test
    void sinRadioNiSegundosVanLosDeSiempre() {
        List<SpecialItem> leidos = items("items:\n"
                + "  - { id: b, nombre: B, descripcion: d, item: compass, efecto: brujula_bioma }\n"
                + "  - { id: e, nombre: E, descripcion: d, item: minecraft:heart_of_the_sea, efecto: ESCUDO_PVP }\n");
        assertEquals(new ItemEffect.BiomeCompass(2000), leidos.get(0).effect());
        assertEquals(new ItemEffect.PvpShield(60), leidos.get(1).effect());
    }

    @Test
    void validaConMensajesQueDicenDonde() {
        String base = "items:\n  - { id: a, nombre: A, descripcion: d, item: compass, ";
        assertEquals("items.yml, ítem 1: no conozco \"radio\" (se puede usar: descripcion, efecto, id, item, nombre)",
                error(base + "efecto: cambiar_casilla, radio: 5 }\n"));
        assertEquals("items.yml, ítem 1: radio: tiene que estar entre 100 y 5000 (vino 50)",
                error(base + "efecto: brujula_bioma, radio: 50 }\n"));
        assertEquals("items.yml, ítem 1: segundos: como mucho 600 (vino 900)", error(base + "efecto: escudo_pvp, segundos: 900 }\n"));
        assertEquals("items.yml, ítem 1: efecto: tiene que ser cambiar_casilla, brujula_bioma o escudo_pvp (vino volar)",
                error(base + "efecto: volar }\n"));
        assertEquals("items.yml, ítem 1: falta item", error("items:\n  - { id: a, nombre: A, descripcion: d, efecto: escudo_pvp }\n"));
        assertEquals("items.yml, ítem 2: el id a ya está en otro ítem",
                error(base + "efecto: escudo_pvp }\n  - { id: a, nombre: B, descripcion: d, item: compass, efecto: escudo_pvp }\n"));
        assertEquals("items.yml: no conozco \"item\" (se puede usar: items)", error("item: []\n"));
    }

    @Test
    void sinArchivoNoHayNinguno(@TempDir Path carpeta) {
        assertEquals(List.of(), CatalogLoader.itemsFromDirectory(carpeta));
        assertEquals(List.of(), items(""));
    }
}
