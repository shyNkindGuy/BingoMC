package com.shyNkindguy.bingo.game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BingoCard {
    private final BingoObjective[][] grid = new BingoObjective[5][5];
    private final Map<UUID, boolean[][]> completed = new HashMap<>();

    public void initPlayer(UUID uuid){
        completed.put(uuid, new boolean[5][5]);
    }

    public boolean complete(UUID uuid, int x, int y){
        boolean[][] state = completed.get(uuid);
        if(!state[x][y]){
            state[x][y] = true;
            return true;
        }
        return false;
    }

}
