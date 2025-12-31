package com.shyNkindguy.bingo.commands;

import com.shyNkindguy.bingo.game.BingoGame;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class BingoCommand implements CommandExecutor {
    private final BingoGame game;

    public BingoCommand(BingoGame game){
        this.game = game;
    }

    @Override

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player)) return true;

        if (args.length == 1 && args[0].equalsIgnoreCase("start")) {
            boolean started = game.start(Bukkit.getOnlinePlayers());

            sender.sendMessage(started
                    ? "§aBingo iniciado!"
                    : "§cNo se pudo iniciar el Bingo.");

            return true;
        }

        sender.sendMessage("§eUso: /bingo start");
        return true;

    }

    }
