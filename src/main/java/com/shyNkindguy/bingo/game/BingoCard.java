package com.shyNkindguy.bingo.game;

import java.util.*;

public class BingoCard {
    private final Map<UUID, Set<String>> objectives = new HashMap<>();

    public void initPlayer(UUID uuid){
        objectives.put(uuid, new HashSet<>());
    }

    public void complete(UUID uuid, String objective){
        objectives.get(uuid).add(objective);
    }

    public int getProgress(UUID uuid){
        return objectives.get(uuid).size();
    }

}
