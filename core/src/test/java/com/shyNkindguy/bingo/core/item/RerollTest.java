package com.shyNkindguy.bingo.core.item;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;
import com.shyNkindguy.bingo.core.catalog.CardSpec;
import com.shyNkindguy.bingo.core.catalog.Catalog;
import com.shyNkindguy.bingo.core.catalog.TaskList;
import com.shyNkindguy.bingo.core.catalog.Tier;
import com.shyNkindguy.bingo.core.progress.TaskProgress;
import com.shyNkindguy.bingo.core.task.ItemTask;
import com.shyNkindguy.bingo.core.task.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RerollTest {

    private static Task task(String id) {
        return new ItemTask(id, "Tarea " + id, "minecraft:" + id, 3);
    }

    private static List<Task> tasks(String prefix, int count) {
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < count; i++) tasks.add(task(prefix + i));
        return tasks;
    }

    private static Catalog catalog(List<Task> easy, List<Task> hard) {
        return new Catalog(Map.of("faciles", new TaskList("faciles", Tier.D, easy), "dificiles", new TaskList("dificiles", Tier.A, hard)),
                Map.of("normal", new CardSpec("normal", 2, Map.of())), "normal");
    }

    private static Set<String> ids(BingoCard card) {
        Set<String> ids = new java.util.HashSet<>();
        for (BingoCell[] row : card.getGrid()) for (BingoCell cell : row) ids.add(cell.getTask().id());
        return ids;
    }

    @Test
    void cambiaUnaPendientePorOtraDeLaMismaListaQueNoEsteEnNingunaCartilla() {
        List<Task> easy = tasks("f", 6);
        List<Task> hard = tasks("d", 4);
        BingoCard own = new BingoCard(List.of(easy.get(0), easy.get(1), easy.get(2), hard.get(0)), new Random(1));
        BingoCard other = new BingoCard(List.of(easy.get(3), easy.get(1), easy.get(2), hard.get(0)), new Random(2));
        for (BingoCell[] row : own.getGrid()) {
            for (BingoCell cell : row) {
                if (!cell.getTask().id().equals("f0")) cell.complete();
            }
        }

        for (int seed = 0; seed < 20; seed++) {
            Reroll.Choice choice = Reroll.pick(own, List.of(own, other), catalog(easy, hard), t -> true, new Random(seed)).orElseThrow();
            assertEquals("f0", choice.taskId());
            assertTrue(Set.of("f4", "f5").contains(choice.replacement().id()), choice.replacement().id());
        }
    }

    @Test
    void siNoQuedaDeEsaListaSaleDeOtraYRespetaLoPermitido() {
        List<Task> easy = tasks("f", 4);
        List<Task> hard = tasks("d", 3);
        BingoCard own = new BingoCard(easy, new Random(1));
        Reroll.Choice choice = Reroll.pick(own, List.of(own), catalog(easy, hard), t -> !t.id().equals("d0"), new Random(3))
                .orElseThrow();
        assertTrue(Set.of("d1", "d2").contains(choice.replacement().id()));

        assertEquals(Optional.empty(), Reroll.pick(own, List.of(own), catalog(easy, hard), t -> false, new Random(3)));
    }

    @Test
    void sinCasillasPendientesNoHayCambio() {
        List<Task> easy = tasks("f", 8);
        BingoCard own = new BingoCard(easy.subList(0, 4), new Random(1));
        BingoCell[][] grid = own.getGrid();
        grid[0][0].complete();
        grid[0][1].complete();
        grid[1][0].complete();
        grid[1][1].lock("azul");
        assertEquals(Optional.empty(), Reroll.pick(own, List.of(own), catalog(easy, List.of()), t -> true, new Random(1)));
    }

    @Test
    void aplicarDejaLaCasillaDeCero() {
        List<Task> easy = tasks("f", 5);
        BingoCard card = new BingoCard(easy.subList(0, 4), new Random(1));
        BingoCell cell = null;
        for (BingoCell[] row : card.getGrid()) for (BingoCell c : row) if (c.getTask().id().equals("f2")) cell = c;
        cell.setProgress(new TaskProgress(2, 3));

        assertTrue(Reroll.apply(card, new Reroll.Choice(easy.get(2), easy.get(4))));
        assertEquals("f4", cell.getTask().id());
        assertEquals(0, cell.getProgress().current());
        assertEquals(-1, cell.completedAt());
        assertFalse(cell.isLocked());
        assertEquals(Set.of("f0", "f1", "f3", "f4"), ids(card));
        assertFalse(Reroll.apply(card, new Reroll.Choice(easy.get(2), easy.get(4))));
    }
}
