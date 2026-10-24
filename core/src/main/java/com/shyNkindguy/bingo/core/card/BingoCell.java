package com.shyNkindguy.bingo.core.card;

import com.shyNkindguy.bingo.core.progress.TaskProgress;
import com.shyNkindguy.bingo.core.task.Task;

public class BingoCell {
    private final Task task;
    private TaskProgress progress;

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
}
