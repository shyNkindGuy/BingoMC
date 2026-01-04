package com.shyNkindguy.bingo.commands;

import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class BingoCommand implements CommandExecutor {
    private final BingoGame game;
    private final BingoScoreBoard scoreboard;

    public BingoCommand(BingoGame game, BingoScoreBoard scoreboard){
        this.game = game; this.scoreboard = scoreboard;
    }


    @Override

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player)) return true;

        if (args.length == 1 && args[0].equalsIgnoreCase("start")) {
            boolean started = game.start(Bukkit.getOnlinePlayers());

            if (started) {
                Bukkit.broadcastMessage("§a§lBINGO INICIADO!");
               for (Player p : Bukkit.getOnlinePlayers()){
                   scoreboard.show(p);
               }
            } else {
                sender.sendMessage("§cEl Bingo ya está en curso o no hay jugadores.");
            }
        }

        sender.sendMessage("§eUso: /bingo start");
        return true;

    }

    }
