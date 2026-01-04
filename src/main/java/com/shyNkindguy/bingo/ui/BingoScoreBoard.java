package com.shyNkindguy.bingo.ui;

import com.shyNkindguy.bingo.game.BingoGame;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import java.util.*;

public class BingoScoreBoard {
        private final BingoGame game;
        private final ScoreboardManager manager;
        private final Map<UUID, Scoreboard> boards = new HashMap<>();
        private final Map<UUID, Objective> objectives = new HashMap<>();

        public BingoScoreBoard(BingoGame game){
            this.game = game;
            this.manager = Bukkit.getScoreboardManager();
        }
        public void show(Player player){
            Scoreboard board = manager.getNewScoreboard();
            Objective objective = board.registerNewObjective(
                    "bingo",
                        Criteria.DUMMY,
                    "§a§lBINGO - shyNkindev"
            );
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            boards.put(player.getUniqueId(), board);
            objectives.put(player.getUniqueId(), objective);

            update(player, board, objective);
            player.setScoreboard(board);
        }
        public void update(Player player){
            Scoreboard board = boards.get(player.getUniqueId());
            Objective objective = objectives.get(player.getUniqueId());

            if (board == null || objective == null) return;

            update(player, board, objective);
        }
        public void update(Player player, Scoreboard board, Objective objective){
            board.getEntries().forEach(board::resetScores);
            int progress = game.getProgress(player.getUniqueId());
            objective.getScore("§7──────────").setScore(4);
            objective.getScore("§fJugador:").setScore(3);
            objective.getScore("§a" + player.getName()).setScore(2);
            objective.getScore("§fObjetivos: §a" + progress).setScore(1);
            objective.getScore("§fEstado: §aEN CURSO").setScore(0);
        }
}
