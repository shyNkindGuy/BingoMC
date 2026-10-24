package com.shyNkindguy.bingo.core.condition;

import com.shyNkindguy.bingo.core.event.EventContext;

// Algo que tiene que cumplirse en el momento del evento para que la tarea avance ("en el Nether", "de
// noche"). Si el contexto no lo sabe (null), no se cumple.
public interface Condition {

    boolean test(EventContext context);

    // Texto corto y estable ("dimension=minecraft:the_nether"): va en el id por defecto de la tarea.
    String key();
}
