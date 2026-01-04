package com.shyNkindguy.bingo.events;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerJoinEvent;
public class JoinListener implements Listener {
    private final BingoGame game;
    private final BingoScoreBoard scoreboard;

    public JoinListener(BingoGame game, BingoScoreBoard scoreboard){
        this.game = game;
        this.scoreboard = scoreboard;
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        if (game.isRunning()){
            scoreboard.show(event.getPlayer());
        }
    }

}
