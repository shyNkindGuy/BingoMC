package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.game.GameSettings;
import com.shyNkindguy.bingo.core.win.WinMode;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsParserTest {

    private static final Catalog CATALOGO = CatalogLoader.defaults();

    private static GameSettings config(String texto) {
        Object yaml = new Yaml(new SafeConstructor(new LoaderOptions())).load(texto);
        return CatalogParser.parseSettings("config.yml", yaml, CATALOGO);
    }

    private static String error(String texto) {
        return assertThrows(CatalogException.class, () -> config(texto)).getMessage();
    }

    @Test
    void leeTodasLasClaves() {
        assertEquals(new GameSettings("rapida", 5, 30, false, 123L, new WinMode.Lines(2)),
                config("cartilla: rapida\ncuenta_regresiva: 5\nduracion: 30\npvp: false\nsemilla: 123\nmodo: 2 lineas"));
    }

    @Test
    void loQueFaltaQuedaPorDefecto() {
        assertEquals(GameSettings.DEFAULT, config(""));
        assertEquals(GameSettings.DEFAULT, config("# todo comentado"));
        assertEquals(GameSettings.DEFAULT.withSeed(9_000_000_000L), config("semilla: 9000000000"));
        assertEquals(0, config("duracion: 0").durationMinutes());
        assertEquals(false, config("pvp: no").pvp());
        assertEquals(new WinMode.Lockout(), config("modo: Lockout").mode());
        assertEquals(new WinMode.Blackout(), config("modo: carta_llena").mode());
    }

    @Test
    void errores() {
        assertTrue(error("cartilla: gigante").contains("no existe la cartilla gigante en cards.yml (hay: normal, rapida)"));
        assertTrue(error("cuenta_regresiva: 90").contains("como mucho 60 segundos"));
        assertTrue(error("duracion: -5").contains("no puede ser negativo"));
        assertTrue(error("pvp: quizas").contains("true o false"));
        assertTrue(error("semilla: abc").contains("número entero"));
        assertTrue(error("duración: 20").contains("no conozco \"duración\""));
        assertTrue(error("modo: volar").contains("no es un modo"));
        assertTrue(error("modo: tiempo\nduracion: 0").contains("hace falta una duracion"));
        assertTrue(error("cartilla: rapida\nmodo: 9 lineas").contains("una cartilla de 3×3 tiene 8 líneas"));
        assertTrue(error("cartilla: rapida\nmodo: 10 casillas").contains("tiene 9 casillas"));
    }

    @Test
    void elConfigPorDefectoDelJarSeLee() {
        assertEquals(GameSettings.DEFAULT, CatalogLoader.defaultSettings(CATALOGO));
    }
}
