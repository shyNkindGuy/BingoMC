package com.shyNkindguy.bingo.core.event;

// Lo que pasó en el juego, ya traducido por el plugin a valores simples. Las tareas solo ven esto, nunca
// eventos de Bukkit.
public sealed interface TaskEvent permits ItemObtained, EntityKilled, AdvancementDone, StatisticReached {
}
