package com.shyNkindguy.bingo.core.achievement;

import com.shyNkindguy.bingo.core.bus.CellCompleted;
import com.shyNkindguy.bingo.core.condition.Condition;

// al: casilla. Se cumple cuando el jugador completa una casilla de esa tarea (`taskId`; null = cualquiera),
// en el contexto que pide `condition` (null = cualquiera) y antes de `withinSeconds` segundos de partida (0 =
// en cualquier momento).
public record CellRule(String taskId, Condition condition, int withinSeconds) implements AchievementRule {

    public CellRule {
        if (withinSeconds < 0) throw new IllegalArgumentException("withinSeconds < 0: " + withinSeconds);
    }

    public boolean matches(CellCompleted event) {
        if (taskId != null && !taskId.equals(event.taskId())) return false;
        if (withinSeconds > 0 && event.second() >= withinSeconds) return false;
        return condition == null || condition.test(event.context());
    }
}
