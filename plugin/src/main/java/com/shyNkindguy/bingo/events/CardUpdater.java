package com.shyNkindguy.bingo.events;

import com.shyNkindguy.bingo.core.event.EventContext;
import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.core.progress.ProgressResult;
import com.shyNkindguy.bingo.core.task.Task;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.game.PlayerContexts;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import com.shyNkindguy.bingo.ui.TeamColors;
import com.shyNkindguy.bingo.ui.Texts;
import org.bukkit.entity.Player;

import java.util.List;

// Aplica eventos a la cartilla del equipo de un jugador (con dónde está él ahora, para las condiciones) y,
// si algo avanzó, avisa lo completado a todo el equipo, actualiza los scoreboards y los mapas del equipo y
// revisa si ganó. Lo usan los listeners y el sondeo de estadísticas.
public class CardUpdater {
    private final BingoGame game;
    private final BingoScoreBoard scoreboard;
    private final BingoMapManager mapManager;
    private final PlayerContexts contexts;

    public CardUpdater(BingoGame game, BingoScoreBoard scoreboard, BingoMapManager mapManager, PlayerContexts contexts) {
        this.game = game;
        this.scoreboard = scoreboard;
        this.mapManager = mapManager;
        this.contexts = contexts;
    }

    // A los demás jugadores de la partida, con el color del equipo: qué completó (en lockout, qué casilla
    // perdieron ellos).
    private void announce(Player player, Task task, boolean lockout) {
        game.teamInGame(player).ifPresent(owner -> {
            String team = TeamColors.chat(owner.color()) + owner.label();
            String message = lockout
                    ? Texts.get("progreso.lockout", "equipo", team, "tarea", task.displayName())
                    : Texts.get("progreso.anuncio", "equipo", team, "tarea", task.displayName(),
                    "casillas", game.getProgress(player.getUniqueId()), "total", cellsOf(player));
            List<Player> mates = game.teammates(player);
            for (Player other : game.playersInGame()) {
                if (!mates.contains(other)) other.sendMessage(message);
            }
        });
    }

    private int cellsOf(Player player) {
        int size = game.getCard(player).getSize();
        return size * size;
    }

    public void apply(Player player, List<? extends TaskEvent> events) {
        if (!game.isRunning() || game.getCard(player) == null) return;

        EventContext context = contexts.of(player);
        List<Player> team = game.teammates(player);
        boolean lockout = game.getSettings().mode().lockout();
        boolean changed = false;
        for (TaskEvent event : events) {
            ProgressResult result = game.applyEvent(player, event, context);
            changed |= result.changed();
            for (Task task : result.completed()) {
                String by = team.size() > 1 ? Texts.get("progreso.por", "jugador", player.getName()) : "";
                for (Player mate : team) {
                    mate.sendMessage(Texts.get("progreso.completado", "tarea", task.displayName(), "por", by));
                    mate.sendTitle(Texts.get("progreso.titulo"), Texts.get("progreso.subtitulo", "tarea", task.displayName()),
                            0, 30, 10);
                    mate.playSound(mate.getLocation(), "minecraft:entity.player.levelup", 1f, 1.2f);
                }
                announce(player, task, lockout);
            }
        }
        if (!changed) return;

        scoreboard.updateAll();
        if (lockout) {
            mapManager.invalidateAll(); // las casillas bloqueadas cambian en las cartillas de todos
        } else {
            team.forEach(mapManager::invalidate);
        }
        if (game.checkWin(player)) {
            game.end(player);
        } else if (game.allClaimed()) {
            game.endByPoints(Texts.get("puntos.sin_libres"));
        }
    }
}
