package com.shyNkindguy.bingo.core.game;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameSummaryTest {

    private static BingoCard cartilla() {
        List<Task> tareas = new ArrayList<>();
        for (int i = 0; i < 9; i++) tareas.add(new ItemTask("t" + i, "Tarea " + i, "minecraft:t" + i, 1));
        return new BingoCard(tareas, new Random(0));
    }

    private static void completar(BingoCard cartilla, int indice, int segundo, String quien) {
        BingoCell celda = cartilla.getGrid()[indice / 3][indice % 3];
        celda.complete();
        celda.markCompleted(segundo, quien);
    }

    @Test
    void equiposDeMasAMenosConSusCasillasEnOrden() {
        BingoCard rojo = cartilla();
        completar(rojo, 0, 300, "Ana");
        completar(rojo, 4, 120, "Beto");
        BingoCard azul = cartilla();
        completar(azul, 1, 50, "Caro");
        completar(azul, 2, 100, "Caro");
        completar(azul, 3, 400, "Caro");

        Map<String, BingoCard> porEquipo = new LinkedHashMap<>();
        porEquipo.put("Rojo", rojo);
        porEquipo.put("Azul", azul);
        GameSummary resumen = GameSummary.of(porEquipo);

        assertEquals(List.of("Azul", "Rojo"), resumen.teams().stream().map(GameSummary.TeamResult::team).toList());
        GameSummary.TeamResult rojos = resumen.teams().get(1);
        assertEquals(2, rojos.completed());
        assertEquals(List.of(120, 300), rojos.cells().stream().map(GameSummary.Completion::second).toList());
        assertEquals("Beto", rojos.cells().get(0).by());
        assertEquals("Caro", resumen.mvp());
        assertEquals(3, resumen.mvpCells());
    }

    @Test
    void siEmpatanGanaElQueLlegoAntes() {
        BingoCard rojo = cartilla();
        completar(rojo, 0, 500, "Ana");
        BingoCard azul = cartilla();
        completar(azul, 0, 200, "Beto");

        Map<String, BingoCard> porEquipo = new LinkedHashMap<>();
        porEquipo.put("Rojo", rojo);
        porEquipo.put("Azul", azul);
        GameSummary resumen = GameSummary.of(porEquipo);

        assertEquals("Azul", resumen.teams().get(0).team());
        assertEquals("Beto", resumen.mvp());
    }

    @Test
    void sinCasillasNoHayMvp() {
        GameSummary resumen = GameSummary.of(Map.of("Rojo", cartilla()));
        assertNull(resumen.mvp());
        assertEquals(0, resumen.teams().get(0).completed());
    }
}
