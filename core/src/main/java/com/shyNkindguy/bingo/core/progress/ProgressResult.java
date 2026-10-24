package com.shyNkindguy.bingo.core.progress;

import com.shyNkindguy.bingo.core.card.BingoCell;
import com.shyNkindguy.bingo.core.task.Task;

import java.util.List;

// Qué cambió en una cartilla con un evento: las casillas que avanzaron y, de ellas, las que se completaron.
public record ProgressResult(List<BingoCell> advanced, List<Task> completed) {

    public static final ProgressResult NONE = new ProgressResult(List.of(), List.of());

    public boolean changed() {
        return !advanced.isEmpty();
    }
}
