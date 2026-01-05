package com.shyNkindguy.bingo.commands;

import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class BingoCommand implements CommandExecutor {
    private final BingoGame game;
    private final BingoScoreBoard scoreboard;
    private final BingoMapManager mapManager;

    public BingoCommand(BingoGame game, BingoScoreBoard scoreboard, BingoMapManager mapManager){
        this.game = game; this.scoreboard = scoreboard; this.mapManager = mapManager;
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
                   mapManager.giveMap(p);
                   scoreboard.show(p);
               }
            } else {
                sender.sendMessage("§cEl Bingo ya está en curso o no hay jugadores.");
            }


        }
        if (args.length == 1 && args[0].equalsIgnoreCase("map")) {
            mapManager.giveMap((Player) sender);
            sender.sendMessage("§aCartilla de Bingo invocada");
            return true;
        }
        sender.sendMessage("§eUso: /bingo start");
        return true;
    }

    }
