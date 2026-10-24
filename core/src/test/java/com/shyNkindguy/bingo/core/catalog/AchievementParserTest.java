package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.achievement.Achievement;
import com.shyNkindguy.bingo.core.achievement.CellRule;
import com.shyNkindguy.bingo.core.achievement.GameRule;
import com.shyNkindguy.bingo.core.condition.TimeOfDay;
import com.shyNkindguy.bingo.core.win.WinMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AchievementParserTest {

    private static final Catalog CATALOGO = CatalogLoader.defaults();
    private static final String TAREA = CATALOGO.tasks().get(0).id();

    private static List<Achievement> logros(String texto) {
        Object yaml = new Yaml(new SafeConstructor(new LoaderOptions())).load(texto);
        return AchievementParser.parse("achievements.yml", yaml, CATALOGO);
    }

    private static String error(String texto) {
        return assertThrows(CatalogException.class, () -> logros(texto)).getMessage();
    }

    @Test
    void losPorDefectoSeLeenYNoRepitenIds() {
        List<Achievement> logros = CatalogLoader.defaultAchievements(CATALOGO);
        assertEquals(14, logros.size());
        Set<String> ids = new HashSet<>();
        for (Achievement logro : logros) assertTrue(ids.add(logro.id()), logro.id());
        assertInstanceOf(GameRule.class, logros.get(0).rule());
        assertEquals("minecraft:map", logros.get(0).icon());
    }

    @Test
    void leeUnLogroDePartidaConTodo() {
        Achievement logro = logros("""
                logros:
                  - id: todo
                    nombre: Todo
                    descripcion: Todo junto
                    icono: gold_ingot
                    gana: true
                    en_menos_de: 15
                    modo: lockout
                    casillas: 4
                    sin_morir: true
                    partidas: 3
                    victorias: 2
                    casillas_total: 50
                """).get(0);
        assertEquals("todo", logro.id());
        assertEquals("Todo", logro.name());
        assertEquals("Todo junto", logro.description());
        assertEquals("minecraft:gold_ingot", logro.icon());
        assertEquals(new GameRule(true, 900, new WinMode.Lockout(), 4, true, 3, 2, 50), logro.rule());
    }

    @Test
    void sinNadaEsUnLogroDePartidaQueNoPideNada() {
        Achievement logro = logros("logros:\n  - id: a\n    nombre: A\n    descripcion: B\n").get(0);
        assertNull(logro.icon());
        assertEquals(new GameRule(null, 0, null, 0, false, 0, 0, 0), logro.rule());
    }

    @Test
    void leeUnLogroDeCasilla() {
        Achievement logro = logros("logros:\n  - id: c\n    nombre: C\n    descripcion: D\n    al: casilla\n"
                + "    tarea: " + TAREA + "\n    en_menos_de: 2\n    condiciones:\n      hora: noche\n").get(0);
        CellRule regla = assertInstanceOf(CellRule.class, logro.rule());
        assertEquals(TAREA, regla.taskId());
        assertEquals(120, regla.withinSeconds());
        assertEquals(new TimeOfDay(true), regla.condition());
    }

    @Test
    void validaConMensajesQueDicenDonde() {
        String base = "logros:\n  - id: a\n    nombre: A\n    descripcion: B\n";
        assertEquals("achievements.yml, logro 1: no conozco \"gana\" (se puede usar: al, condiciones, descripcion, "
                + "en_menos_de, icono, id, nombre, tarea)", error(base + "    al: casilla\n    gana: true\n"));
        assertEquals("achievements.yml, logro 1: no conozco \"tarea\" (se puede usar: al, casillas, casillas_total, "
                + "descripcion, en_menos_de, gana, icono, id, modo, nombre, partidas, sin_morir, victorias)",
                error(base + "    tarea: " + TAREA + "\n"));
        assertEquals("achievements.yml, logro 1: al: tiene que ser partida o casilla (vino siempre)", error(base + "    al: siempre\n"));
        assertEquals("achievements.yml, logro 1: tarea: no hay ninguna tarea item:nada en tasks/",
                error(base + "    al: casilla\n    tarea: item:nada\n"));
        assertEquals("achievements.yml, logro 1: falta descripcion", error("logros:\n  - id: a\n    nombre: A\n"));
        assertEquals("achievements.yml, logro 1: id: solo minúsculas, números y _ (vino Mal-Id)",
                error("logros:\n  - id: Mal-Id\n    nombre: A\n    descripcion: B\n"));
        assertEquals("achievements.yml, logro 2: el id a ya está en otro logro", error(base + "  - id: a\n    nombre: C\n    descripcion: D\n"));
        assertEquals("achievements.yml, logro 1: en_menos_de: tiene que ser mayor que 0 (vino 0)", error(base + "    en_menos_de: 0\n"));
        assertEquals("achievements.yml, logro 1: sin_morir: se pone sin_morir: true (o se quita)", error(base + "    sin_morir: false\n"));
        assertTrue(error(base + "    modo: casi todo\n").startsWith("achievements.yml, logro 1: modo: \"casi todo\" no es un modo"));
        assertEquals("achievements.yml: no conozco \"logro\" (se puede usar: logros)", error("logro: []\n"));
    }

    @Test
    void vacioONoHayArchivoEsNinguno(@TempDir Path carpeta) {
        assertEquals(List.of(), logros(""));
        assertEquals(List.of(), logros("logros:\n"));
        assertEquals(List.of(), CatalogLoader.achievementsFromDirectory(carpeta, CATALOGO));
    }
}
