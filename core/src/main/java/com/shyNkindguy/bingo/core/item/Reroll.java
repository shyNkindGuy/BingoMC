package com.shyNkindguy.bingo.core.item;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;
import com.shyNkindguy.bingo.core.catalog.Catalog;
import com.shyNkindguy.bingo.core.catalog.TaskList;
import com.shyNkindguy.bingo.core.task.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

// El dado del caos: una casilla sin completar ni bloquear de la cartilla, al azar, pasa a tener otra tarea de
// la misma lista del catálogo (el mismo nivel) que no esté en ninguna cartilla de la partida; si de esa lista
// no queda ninguna, de cualquiera.
public final class Reroll {

    // La casilla con la tarea `before` pasa a tener `replacement`.
    public record Choice(Task before, Task replacement) {
        public String taskId() {
            return before.id();
        }
    }

    private Reroll() {
    }

    // `own`: la cartilla de quien usa el dado; `cards`: todas las de la partida (para no repetir tareas);
    // `allowed`: qué tareas pueden salir.
    public static Optional<Choice> pick(BingoCard own, Collection<BingoCard> cards, Catalog catalog,
                                        Predicate<Task> allowed, Random random) {
        List<BingoCell> open = new ArrayList<>();
        for (BingoCell[] row : own.getGrid()) {
            for (BingoCell cell : row) {
                if (!cell.isCompleted() && !cell.isLocked()) open.add(cell);
            }
        }
        if (open.isEmpty()) return Optional.empty();
        Task before = open.get(random.nextInt(open.size())).getTask();

        Set<String> used = new HashSet<>();
        for (BingoCard card : cards) {
            for (BingoCell[] row : card.getGrid()) {
                for (BingoCell cell : row) used.add(cell.getTask().id());
            }
        }
        for (BingoCell[] row : own.getGrid()) {
            for (BingoCell cell : row) used.add(cell.getTask().id());
        }

        List<Task> pool = candidates(sameList(catalog, before), used, allowed);
        if (pool.isEmpty()) pool = candidates(catalog.tasks(), used, allowed);
        if (pool.isEmpty()) return Optional.empty();
        return Optional.of(new Choice(before, pool.get(random.nextInt(pool.size()))));
    }

    // Cambia la casilla de esa tarea en `card` (con avance, tiempo y bloqueo de cero). false si no la tenía.
    public static boolean apply(BingoCard card, Choice choice) {
        for (BingoCell[] row : card.getGrid()) {
            for (BingoCell cell : row) {
                if (cell.getTask().id().equals(choice.taskId())) {
                    cell.replaceTask(choice.replacement());
                    return true;
                }
            }
        }
        return false;
    }

    private static List<Task> sameList(Catalog catalog, Task task) {
        for (TaskList list : catalog.lists().values()) {
            for (Task candidate : list.tasks()) {
                if (candidate.id().equals(task.id())) return list.tasks();
            }
        }
        return List.of();
    }

    private static List<Task> candidates(List<Task> tasks, Set<String> used, Predicate<Task> allowed) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks) {
            if (!used.contains(task.id()) && allowed.test(task)) result.add(task);
        }
        return result;
    }
}
