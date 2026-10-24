package com.shyNkindguy.bingo.core.text;

import com.shyNkindguy.bingo.core.catalog.CatalogException;
import com.shyNkindguy.bingo.core.catalog.CatalogLoader;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagesTest {

    private static Object yaml(String texto) {
        return new Yaml(new SafeConstructor(new LoaderOptions())).load(texto);
    }

    private static final Messages BASE = new Messages(Map.of(
            "partida.ganador", "&eGanador: &a{ganador}",
            "partida.tiempo", "&7Tiempo: &f{tiempo}"));

    @Test
    void reemplazaVariablesYColores() {
        assertEquals("§eGanador: §aAna", BASE.get("partida.ganador", "ganador", "Ana"));
        assertEquals("§7Tiempo: §f{tiempo}", BASE.get("partida.tiempo"));
        assertEquals("[no.existe]", BASE.get("no.existe"));
    }

    @Test
    void soloLosCodigosDeMinecraftSonColores() {
        assertEquals("Pan & queso §lnegrita §r", Messages.colors("Pan & queso &lnegrita &r"));
        assertEquals("§cA", Messages.colors("&CA"));
    }

    @Test
    void lasClavesAnidadasSeJuntanConPuntoYLoQueFaltaSaleDeLosPorDefecto() {
        Messages servidor = Messages.parse("messages.yml", yaml("partida:\n  ganador: \"&bGanó {ganador}\""), BASE);
        assertEquals("§bGanó Ana", servidor.get("partida.ganador", "ganador", "Ana"));
        assertEquals("§7Tiempo: §f3m", servidor.get("partida.tiempo", "tiempo", "3m"));
        assertEquals(BASE.keys(), Messages.parse("messages.yml", yaml(""), BASE).keys());
    }

    @Test
    void unaClaveMalEscritaEsError() {
        String mensaje = assertThrows(CatalogException.class,
                () -> Messages.parse("messages.yml", yaml("partida:\n  ganadr: x"), BASE)).getMessage();
        assertEquals("messages.yml: no conozco la clave partida.ganadr", mensaje);
        assertThrows(CatalogException.class, () -> Messages.parse("messages.yml", yaml("- a"), BASE));
    }

    @Test
    void losTextosPorDefectoDelJarSeLeenYNoTienenHuecos() {
        Messages porDefecto = CatalogLoader.defaultMessages();
        assertTrue(porDefecto.keys().size() > 50, "Hay " + porDefecto.keys().size());
        for (String clave : porDefecto.keys()) {
            assertFalse(porDefecto.get(clave).isBlank(), "Vacío: " + clave);
        }
        assertTrue(porDefecto.get("partida.ganador", "ganador", "Ana").contains("Ana"));
    }
}
