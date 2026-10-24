package com.shyNkindguy.bingo.api;

import com.shyNkindguy.bingo.core.bus.DomainEvent;
import com.shyNkindguy.bingo.core.bus.DomainEvents;
import com.shyNkindguy.bingo.core.stats.PlayerStats;
import com.shyNkindguy.bingo.core.stats.Ranking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

// Lo que AdaptiveBingo ofrece a otros plugins (ejemplo en docs/API.md). Se pide al ServicesManager de Bukkit:
//     AdaptiveBingoApi bingo = Bukkit.getServicesManager().load(AdaptiveBingoApi.class);
// Todo se usa desde el hilo principal del servidor.
public interface AdaptiveBingoApi {

    // Hay una partida repartida (en cuenta regresiva, en curso o en pausa).
    boolean inGame();

    // La partida está en curso: las tareas cuentan.
    boolean isRunning();

    // Casillas completas de la cartilla del equipo del jugador (0 si no juega).
    int progress(UUID player);

    // Lo que lleva jugado (partidas, victorias, casillas, victoria más rápida, logros).
    Optional<PlayerStats> stats(UUID player);

    List<Ranking.Entry> top(Ranking.Metric metric, int limit);

    // Los eventos de la partida: CellCompleted, GameEnded y AchievementUnlocked (o DomainEvent para todos).
    // Cancelar la suscripción al apagar el plugin que escucha.
    <E extends DomainEvent> DomainEvents.Subscription subscribe(Class<E> type, Consumer<? super E> listener);
}
