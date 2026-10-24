package com.shyNkindguy.bingo.core.task;

import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.core.progress.TaskProgress;

// Lo que pide una casilla. Cada tipo decide cómo un evento hace avanzar su progreso.
public interface Task {

    String id();

    // Texto para el jugador ("16 × Tronco de roble").
    String displayName();

    // Clave del ítem que se dibuja en la casilla ("minecraft:oak_log"), o null si la tarea no trae uno: el
    // plugin dibuja el del logro o el símbolo del tipo.
    String icon();

    // Cuánto hay que juntar para completarla (1 si es "conseguirlo una vez").
    int target();

    // El progreso después del evento; el mismo objeto si el evento no le importa a esta tarea.
    TaskProgress apply(TaskEvent event, TaskProgress progress);
}
