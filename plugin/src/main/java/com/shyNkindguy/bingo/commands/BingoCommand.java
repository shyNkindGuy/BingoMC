package com.shyNkindguy.bingo.commands;

import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class BingoCommand implements CommandExecutor {
    private static final String ADMIN_PERMISSION = "bingo.admin";
    private static final String USAGE = "§eUso: /bingo <start|stop|map>";

    private final BingoGame game;
    private final BingoScoreBoard scoreboard;
    private final BingoMapManager mapManager;

    public BingoCommand(BingoGame game, BingoScoreBoard scoreboard, BingoMapManager mapManager){
        this.game = game; this.scoreboard = scoreboard; this.mapManager = mapManager;
    }


    @Override

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length != 1) {
            sender.sendMessage(USAGE);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "start" -> start(sender);
            case "stop" -> stop(sender);
            case "map" -> giveMap(sender);
            default -> sender.sendMessage(USAGE);
        }
        return true;
    }

    private void start(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage("§cNo tienes permiso para iniciar el Bingo.");
            return;
        }
        if (!game.start(Bukkit.getOnlinePlayers())) {
            sender.sendMessage("§cEl Bingo ya está en curso o no hay jugadores.");
            return;
        }
        Bukkit.broadcastMessage("§a§lBINGO INICIADO!");
        for (Player p : Bukkit.getOnlinePlayers()){
            mapManager.giveMap(p);
            scoreboard.show(p);
        }
    }

    private void stop(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage("§cNo tienes permiso para detener el Bingo.");
            return;
        }
        if (!game.stop(sender.getName())) {
            sender.sendMessage("§cNo hay ninguna partida en curso.");
        }
    }

    private void giveMap(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cSolo un jugador puede recibir la cartilla.");
            return;
        }
        mapManager.giveMap(player);
        player.sendMessage("§aCartilla de Bingo invocada");
    }

    }
