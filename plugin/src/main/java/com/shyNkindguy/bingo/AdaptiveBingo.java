package com.shyNkindguy.bingo;

import com.shyNkindguy.bingo.commands.BingoCommand;
import com.shyNkindguy.bingo.config.CatalogService;
import com.shyNkindguy.bingo.config.TaskAvailability;
import com.shyNkindguy.bingo.events.AdvancementReset;
import com.shyNkindguy.bingo.events.CardUpdater;
import com.shyNkindguy.bingo.events.ObjectiveListener;
import com.shyNkindguy.bingo.events.SessionListener;
import com.shyNkindguy.bingo.events.StatisticPoller;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.game.PlayerContexts;
import com.shyNkindguy.bingo.game.StartActions;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import com.shyNkindguy.bingo.ui.GameBossBar;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdaptiveBingo extends JavaPlugin {
    private BingoGame game;
    private BingoScoreBoard scoreboard;
    private BingoMapManager mapManager;
    @Override

    public void onEnable(){

        getLogger().info("Mi Bingo Habilitado :)))");
        CatalogService catalogs = new CatalogService(this, TaskAvailability.ofServer());
        catalogs.load();
        game = new BingoGame(this);
        scoreboard = new BingoScoreBoard(game);
        mapManager = new BingoMapManager(this, game);
        PlayerContexts contexts = new PlayerContexts();
        CardUpdater updater = new CardUpdater(game, scoreboard, mapManager, contexts);
        StatisticPoller statistics = new StatisticPoller(this, game, updater);
        StartActions startActions = new StartActions(game);
        game.onDeal(startActions::prepare);
        game.onStart(() -> {
            startActions.giveKits();
            contexts.reset(Bukkit.getOnlinePlayers());
            AdvancementReset.forGame(game);
            statistics.start();
            scoreboard.updateAll();
        });
        GameBossBar bossBar = new GameBossBar(game);
        game.onTick(bossBar::update);
        game.onEnd(() -> {
            startActions.restore();
            bossBar.hide();
            statistics.stop();
            scoreboard.hideAll();
            mapManager.removeMaps();
        });

        Bukkit.getPluginManager().registerEvents(
                new ObjectiveListener(this, game, mapManager, updater),
                this
        );
        Bukkit.getPluginManager().registerEvents(new SessionListener(this, game, mapManager, scoreboard, startActions), this);
        BingoCommand command = new BingoCommand(game, scoreboard, mapManager, catalogs);
        PluginCommand bingo = getCommand("bingo");
        bingo.setExecutor(command);
        bingo.setTabCompleter(command);
    }

    }
