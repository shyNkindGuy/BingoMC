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
import org.bukkit.Bukkit;
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

    // Lockout: a los demás se les avisa qué casilla perdieron.
    private void announceClaim(Player player, Task task) {
        game.teamInGame(player).ifPresent(owner -> {
            List<Player> mates = game.teammates(player);
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (mates.contains(other) || game.getCard(other) == null) continue;
                other.sendMessage("§7[Lockout] " + TeamColors.chat(owner.color()) + owner.label()
                        + " §7se quedó con §f" + task.displayName());
            }
        });
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
                String by = team.size() > 1 ? " §7(" + player.getName() + ")" : "";
                team.forEach(mate -> mate.sendMessage("§a✔ Objetivo completado: §f" + task.displayName() + by));
                if (lockout) announceClaim(player, task);
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
            game.endByPoints("§eNo quedan casillas libres");
        }
    }
}
