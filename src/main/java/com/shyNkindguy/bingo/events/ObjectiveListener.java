package com.shyNkindguy.bingo.events;

import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class ObjectiveListener implements Listener {

    private final BingoGame game;
    private final BingoScoreBoard scoreboard;

    public ObjectiveListener(BingoGame game, BingoScoreBoard scoreboard) {
        this.game = game;
        this.scoreboard = scoreboard;
    }
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event){
            if(!(event.getEntity() instanceof Player player)) return;
            if (!game.isRunning()) return;

            String itemName = event.getItem().getItemStack()
                            .getType().name();
            boolean completed = game.completeObjective(
                    player.getUniqueId(),
                    "PICKUP_" + itemName
            );
            if (completed){
                player.sendMessage("§a✔ Objetivo completado: §f" + itemName);
                scoreboard.update(player);
            }

    }
}
