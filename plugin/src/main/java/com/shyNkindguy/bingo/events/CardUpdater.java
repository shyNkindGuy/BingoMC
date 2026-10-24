package com.shyNkindguy.bingo.events;

import com.shyNkindguy.bingo.core.event.TaskEvent;
import com.shyNkindguy.bingo.core.progress.ProgressResult;
import com.shyNkindguy.bingo.core.task.Task;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import org.bukkit.entity.Player;

import java.util.List;

// Aplica eventos a la cartilla de un jugador y, si algo avanzó, avisa lo completado, actualiza el
// scoreboard y el mapa y revisa si ganó. Lo usan los listeners y el sondeo de estadísticas.
public class CardUpdater {
    private final BingoGame game;
    private final BingoScoreBoard scoreboard;
    private final BingoMapManager mapManager;

    public CardUpdater(BingoGame game, BingoScoreBoard scoreboard, BingoMapManager mapManager) {
        this.game = game;
        this.scoreboard = scoreboard;
        this.mapManager = mapManager;
    }

    public void apply(Player player, List<? extends TaskEvent> events) {
        if (!game.isRunning()) return;

        boolean changed = false;
        for (TaskEvent event : events) {
            ProgressResult result = game.applyEvent(player, event);
            changed |= result.changed();
            for (Task task : result.completed()) {
                player.sendMessage("§a✔ Objetivo completado: §f" + task.displayName());
            }
        }
        if (!changed) return;

        scoreboard.update(player);
        mapManager.invalidate(player);
        if (game.checkWin(player)) {
            game.end(player);
        }
    }
}
