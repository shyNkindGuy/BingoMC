package com.shyNkindguy.bingo.game;

import org.bukkit.entity.Player;

public interface BingoObjective {
   String getDescription();
   ObjectiveType getType();
   boolean checkCompletion(Player player);

}
