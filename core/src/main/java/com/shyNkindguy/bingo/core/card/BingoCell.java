package com.shyNkindguy.bingo.core.card;

import com.shyNkindguy.bingo.core.progress.TaskProgress;
import com.shyNkindguy.bingo.core.task.Task;

// Una casilla: su tarea, el avance, en qué segundo de la partida se completó y quién (para desempatar y el
// resumen del final) y, en lockout, qué equipo se la quedó antes (bloqueada: ya no se puede completar).
public class BingoCell {
    private Task task;
    private TaskProgress progress;
    private int completedAt = -1;
    private String completedBy;
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

    // Quién la completó (null si no se anotó).
    public String completedBy() {
        return completedBy;
    }

    // Anota el segundo la primera vez que se ve completa.
    public void markCompletedAt(int second) {
        markCompleted(second, null);
    }

    // Anota el segundo y quién la completó, la primera vez que se ve completa.
    public void markCompleted(int second, String by) {
        if (!isCompleted() || completedAt >= 0) return;
        completedAt = second;
        completedBy = by;
    }

    public boolean isLocked() {
        return lockedBy != null;
    }

    public String lockedBy() {
        return lockedBy;
    }

    // El dado del caos: otra tarea, desde cero (sin avance, tiempo, autor ni bloqueo).
    public void replaceTask(Task newTask) {
        task = newTask;
        progress = TaskProgress.start(newTask.target());
        completedAt = -1;
        completedBy = null;
        lockedBy = null;
    }

    // Lockout: otro equipo (`by`) completó esta tarea primero. Una casilla ya completa no se bloquea.
    public void lock(String by) {
        if (!isCompleted()) lockedBy = by;
    }
}
