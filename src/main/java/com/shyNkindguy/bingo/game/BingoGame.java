package com.shyNkindguy.bingo.game;

import com.shyNkindguy.bingo.player.PlayerProgress;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BingoGame {

    private final Map<UUID, PlayerProgress> players = new HashMap<>();
    private GameState state = GameState.WAITING;
    private final BingoCard card;

    public BingoGame (JavaPlugin plugin){
        card = new BingoCard();
    }

    public void startGame(Collection<Player> onlineplayers){
        state = GameState.RUNNING;
        for (Player p : onlineplayers){
            players.put(p.getUniqueId(), new PlayerProgress());
            card. initPlayer(p.getUniqueId());
        }

    }
}
