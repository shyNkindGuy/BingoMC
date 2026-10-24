package com.shyNkindguy.bingo.core.bus;

// Lo que pasa en una partida, para quien quiera enterarse (estadísticas, logros…) sin que el bingo sepa que
// existe. Los jugadores van por id (en el plugin, el UUID en texto) y nombre.
public sealed interface DomainEvent permits CellCompleted, GameEnded {
}
