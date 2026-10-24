package com.shyNkindguy.bingo.core.card;

import com.shyNkindguy.bingo.core.progress.TaskProgress;
import com.shyNkindguy.bingo.core.task.Task;

// Una casilla: su tarea, el avance, en qué segundo de la partida se completó (para desempatar) y, en
// lockout, qué equipo se la quedó antes (bloqueada: ya no se puede completar).
public class BingoCell {
    private final Task task;
    private TaskProgress progress;
    private int completedAt = -1;
    private String lockedBy;

    public BingoCell(Task task){
        this.task = task;
        this.progress = TaskProgress.start(task.target());
    }

    public Task getTask(){
        return task;
    }

    public TaskProgress getProgress(){
        return progress;
    }

    public void setProgress(TaskProgress progress){
        this.progress = progress;
    }

    public boolean isCompleted(){
        return progress.isComplete();
    }

    // Marca la casilla como hecha sin pasar por un evento (tests y, más adelante, comandos de admin).
    public void complete(){
        this.progress = TaskProgress.done(task.target());
    }

    // Segundo de partida en que se completó; -1 si no se completó (o no se anotó).
    public int completedAt() {
        return completedAt;
    }

    // Anota el segundo la primera vez que se ve completa.
    public void markCompletedAt(int second) {
        if (isCompleted() && completedAt < 0) completedAt = second;
    }

    public boolean isLocked() {
        return lockedBy != null;
    }

    public String lockedBy() {
        return lockedBy;
    }

    // Lockout: otro equipo (`by`) completó esta tarea primero. Una casilla ya completa no se bloquea.
    public void lock(String by) {
        if (!isCompleted()) lockedBy = by;
    }
}
