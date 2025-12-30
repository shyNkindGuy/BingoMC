package com.shyNkindguy.bingo.events;

import com.shyNkindguy.bingo.game.BingoGame;
import org.bukkit.event.Listener;

public class ObjectiveListener implements Listener {

    private final BingoGame game;

    public ObjectiveListener(BingoGame game) {
        this.game = game;
    }
}
