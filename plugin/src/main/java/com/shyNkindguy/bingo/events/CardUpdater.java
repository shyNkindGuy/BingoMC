package com.shyNkindguy.bingo.events;

import com.shyNkindguy.bingo.core.event.EventContext;
import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.core.progress.ProgressResult;
import com.shyNkindguy.bingo.core.task.Task;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.game.PlayerContexts;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
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

    public void apply(Player player, List<? extends TaskEvent> events) {
        if (!game.isRunning() || game.getCard(player) == null) return;

        EventContext context = contexts.of(player);
        List<Player> team = game.teammates(player);
        boolean changed = false;
        for (TaskEvent event : events) {
            ProgressResult result = game.applyEvent(player, event, context);
            changed |= result.changed();
            for (Task task : result.completed()) {
                String by = team.size() > 1 ? " §7(" + player.getName() + ")" : "";
                team.forEach(mate -> mate.sendMessage("§a✔ Objetivo completado: §f" + task.displayName() + by));
            }
        }
        if (!changed) return;

        scoreboard.updateAll();
        team.forEach(mapManager::invalidate);
        if (game.checkWin(player)) {
            game.end(player);
        }
    }
}
