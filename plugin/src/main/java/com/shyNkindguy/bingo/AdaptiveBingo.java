package com.shyNkindguy.bingo;

import com.shyNkindguy.bingo.api.AdaptiveBingoApi;
import com.shyNkindguy.bingo.api.BingoApi;
import com.shyNkindguy.bingo.commands.BingoCommand;
import com.shyNkindguy.bingo.config.CatalogService;
import com.shyNkindguy.bingo.config.TaskAvailability;
import com.shyNkindguy.bingo.events.AdvancementReset;
import com.shyNkindguy.bingo.events.CardUpdater;
import com.shyNkindguy.bingo.events.ObjectiveListener;
import com.shyNkindguy.bingo.events.SessionListener;
import com.shyNkindguy.bingo.events.SpecialItemListener;
import com.shyNkindguy.bingo.events.StatisticPoller;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.game.EndActions;
import com.shyNkindguy.bingo.game.PlayerContexts;
import com.shyNkindguy.bingo.game.StartActions;
import com.shyNkindguy.bingo.game.StatusApplier;
import com.shyNkindguy.bingo.hooks.BingoExpansion;
import com.shyNkindguy.bingo.items.ChaosDealer;
import com.shyNkindguy.bingo.items.SpecialItems;
import com.shyNkindguy.bingo.core.stats.YamlStatsRepository;
import com.shyNkindguy.bingo.storage.StatsService;
import com.shyNkindguy.bingo.ui.AchievementAnnouncer;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import com.shyNkindguy.bingo.ui.GameBossBar;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
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
        StatsService stats = new StatsService(getLogger(), new YamlStatsRepository(getDataFolder().toPath()));
        stats.start(game.events(), catalogs::achievements);
        AchievementAnnouncer.subscribeTo(game.events(), this);
        Bukkit.getServicesManager().register(AdaptiveBingoApi.class, new BingoApi(game, stats), this, ServicePriority.Normal);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BingoExpansion(getDescription().getVersion(), game, stats).register();
            getLogger().info("PlaceholderAPI: %adaptivebingo_...% registrados");
        }
        scoreboard = new BingoScoreBoard(game);
        mapManager = new BingoMapManager(this, game);
        PlayerContexts contexts = new PlayerContexts();
        game.countDeathsWith(id -> {
            Player player = Bukkit.getPlayer(id);
            return player == null ? -1 : contexts.of(player).deaths();
        });
        CardUpdater updater = new CardUpdater(game, scoreboard, mapManager, contexts);
        StatisticPoller statistics = new StatisticPoller(this, game, updater);
        StartActions startActions = new StartActions(game);
        EndActions endActions = new EndActions(this, game, startActions);
        SpecialItems specialItems = new SpecialItems(this, catalogs::items, catalogs::sets);
        StatusApplier statuses = new StatusApplier(game, specialItems);
        ChaosDealer chaos = new ChaosDealer(game, specialItems);
        SpecialItemListener itemListener = new SpecialItemListener(game, specialItems, catalogs, mapManager, scoreboard);
        game.onDeal(chaos::reset);
        game.onDeal(itemListener::reset);
        game.onDeal(statuses::clear);
        game.onDeal(endActions::cancel);
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
        game.onTick(chaos::tick);
        game.onTick(statuses::tick);
        game.onEnd(() -> {
            endActions.finish();
            startActions.restore();
            bossBar.hide();
            statistics.stop();
            scoreboard.hideAll();
            mapManager.removeMaps();
            specialItems.removeAll();
            statuses.clear();
        });

        Bukkit.getPluginManager().registerEvents(
                new ObjectiveListener(this, game, mapManager, updater, specialItems),
                this
        );
        Bukkit.getPluginManager().registerEvents(itemListener, this);
        Bukkit.getPluginManager().registerEvents(new SessionListener(this, game, mapManager, scoreboard, startActions), this);
        BingoCommand command = new BingoCommand(game, scoreboard, mapManager, catalogs, stats, specialItems);
        PluginCommand bingo = getCommand("bingo");
        bingo.setExecutor(command);
        bingo.setTabCompleter(command);
    }

    }
