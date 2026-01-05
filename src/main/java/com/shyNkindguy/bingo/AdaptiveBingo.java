package com.shyNkindguy.bingo;

import com.shyNkindguy.bingo.commands.BingoCommand;
import com.shyNkindguy.bingo.events.ObjectiveListener;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import org.bukkit.Bukkit;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdaptiveBingo extends JavaPlugin {
    private BingoGame game;
    private BingoScoreBoard scoreboard;
    private BingoMapManager mapManager;
    @Override

    public void onEnable(){

        getLogger().info("Mi Bingo Habilitado :)))");
        if(getCommand("bingotest") != null){
            getCommand("bingotest").setExecutor((sender, command, label, args) ->{
                sender.sendMessage("§aBingo funciona, mira mi bingo funciona mamaaaaaaaaaaaa, shyNkindguy :v!");
                return true;
            });
        }else {
            getLogger().severe("El comando /bingotest no está definido en plugin.yml");
        }
        game = new BingoGame(this);
        scoreboard = new BingoScoreBoard(game);
        mapManager = new BingoMapManager(game);

        Bukkit.getPluginManager().registerEvents(
                new ObjectiveListener(this,game,scoreboard,mapManager),
                this
        );
        getCommand("bingo").setExecutor(
                new BingoCommand(game, scoreboard, mapManager)
        );
    }

    }
