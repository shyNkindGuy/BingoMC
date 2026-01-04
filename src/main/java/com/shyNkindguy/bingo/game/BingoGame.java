package com.shyNkindguy.bingo.game;

import com.shyNkindguy.bingo.player.PlayerProgress;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BingoGame {

    private GameState state = GameState.WAITING;
    private final Map<UUID, PlayerProgress> players = new HashMap<>();
    private final BingoCard card = new BingoCard();

    public boolean start(Collection<? extends Player> onlinePlayers){
        if (state != GameState.WAITING || onlinePlayers.size() < 1) return false;
        state = GameState.RUNNING;

        for (Player p : onlinePlayers){
            players.put(p.getUniqueId(), new PlayerProgress());
            card.initPlayer(p.getUniqueId());
        }
        return true;
    }
    public int getProgress(UUID uuid){
        return players.containsKey(uuid)
                ? players.get(uuid).getcompleted()
                : 0;
    }
    public boolean isRunning(){
        return state == GameState.RUNNING;
    }
    public boolean completeObjective(UUID uuid, String objective){
        if (!players.containsKey(uuid)) return false;
        card.complete(uuid, objective);
        players.get(uuid).increment();
        return true;
    }

    public GameState getState(){
        return state;
    }
}
