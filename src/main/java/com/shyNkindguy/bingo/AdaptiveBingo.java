package com.shyNkindguy.bingo;

import com.shyNkindguy.bingo.commands.BingoCommand;
import com.shyNkindguy.bingo.events.ObjectiveListener;
import com.shyNkindguy.bingo.game.BingoGame;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdaptiveBingo extends JavaPlugin {
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
        BingoGame game = new BingoGame();

        if (getCommand("bingo") != null) {
            getCommand("bingo").setExecutor(new BingoCommand(game));
        }
    }

    }
