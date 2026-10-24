package com.shyNkindguy.bingo.core.catalog;

import com.shyNkindguy.bingo.core.task.Task;

import java.util.List;

// Una lista de tareas (un archivo de tasks/): nombre, dificultad y sus tareas.
public record TaskList(String name, Tier tier, List<Task> tasks) {

    public TaskList {
        tasks = List.copyOf(tasks);
    }
}
