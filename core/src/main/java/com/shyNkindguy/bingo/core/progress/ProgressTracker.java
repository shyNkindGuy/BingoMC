package com.shyNkindguy.bingo.core.progress;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;
import com.shyNkindguy.bingo.core.event.EventContext;
import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.core.task.Task;

import java.util.ArrayList;
import java.util.List;

// Aplica un evento a todas las casillas pendientes (ni completas ni bloqueadas) de la cartilla de un equipo.
public final class ProgressTracker {

    private ProgressTracker() {
    }

    public static ProgressResult apply(BingoCard card, TaskEvent event) {
        return apply(card, event, EventContext.NONE);
    }

    // `context`: dónde estaba el jugador, para las tareas con condiciones.
    public static ProgressResult apply(BingoCard card, TaskEvent event, EventContext context) {
        List<BingoCell> advanced = new ArrayList<>();
        List<Task> completed = new ArrayList<>();

        for (BingoCell[] row : card.getGrid()) {
            for (BingoCell cell : row) {
                if (cell.isCompleted() || cell.isLocked()) continue;

                TaskProgress before = cell.getProgress();
                TaskProgress after = cell.getTask().apply(event, context, before);
                if (after.equals(before)) continue;

                cell.setProgress(after);
                advanced.add(cell);
                if (after.isComplete()) completed.add(cell.getTask());
            }
        }
        return advanced.isEmpty() ? ProgressResult.NONE : new ProgressResult(advanced, completed);
    }
}
