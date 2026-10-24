package com.shyNkindguy.bingo.commands;

import com.shyNkindguy.bingo.config.CatalogService;
import com.shyNkindguy.bingo.core.catalog.Catalog;
import com.shyNkindguy.bingo.core.catalog.CatalogException;
import com.shyNkindguy.bingo.core.task.Task;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class BingoCommand implements CommandExecutor, TabCompleter {
    private static final String ADMIN_PERMISSION = "bingo.admin";
    private static final String USAGE = "§eUso: /bingo <start [cartilla] [semilla]|stop|map|reload>";
    private static final List<String> ADMIN_SUBCOMMANDS = List.of("start", "stop", "map", "reload");

    private final BingoGame game;
    private final BingoScoreBoard scoreboard;
    private final BingoMapManager mapManager;
    private final CatalogService catalogs;

    public BingoCommand(BingoGame game, BingoScoreBoard scoreboard, BingoMapManager mapManager, CatalogService catalogs){
        this.game = game; this.scoreboard = scoreboard; this.mapManager = mapManager; this.catalogs = catalogs;
    }


    @Override

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0) {
            sender.sendMessage(USAGE);
            return true;
        }
        String subcommand = args[0].toLowerCase(Locale.ROOT);
        if (!subcommand.equals("start") && args.length != 1) {
            sender.sendMessage(USAGE);
            return true;
        }
        switch (subcommand) {
            case "start" -> start(sender, args);
            case "stop" -> stop(sender);
            case "map" -> giveMap(sender);
            case "reload" -> reload(sender);
            default -> sender.sendMessage(USAGE);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        boolean admin = sender.hasPermission(ADMIN_PERMISSION);
        if (args.length == 1) {
            return matching(admin ? ADMIN_SUBCOMMANDS : List.of("map"), args[0]);
        }
        if (args.length == 2 && admin && args[0].equalsIgnoreCase("start")) {
            return matching(new ArrayList<>(catalogs.get().cards().keySet()), args[1]);
        }
        return List.of();
    }

    private static List<String> matching(List<String> options, String typed) {
        String prefix = typed.toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
    }

    // /bingo start [cartilla] [semilla]: sin cartilla, la por defecto de cards.yml; sin semilla, una al azar
    // que se anuncia para poder repetir la partida.
    private void start(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage("§cNo tienes permiso para iniciar el Bingo.");
            return;
        }
        if (game.isRunning()) {
            sender.sendMessage("§cEl Bingo ya está en curso.");
            return;
        }

        Catalog catalog = catalogs.get();
        String cardName = catalog.defaultCard();
        Long seed = null;
        for (int i = 1; i < args.length; i++) {
            if (catalog.cards().containsKey(args[i])) {
                cardName = args[i];
            } else if (seed == null && isNumber(args[i])) {
                seed = Long.parseLong(args[i]);
            } else {
                sender.sendMessage("§cNo existe la cartilla §f" + args[i] + "§c. Hay: §f"
                        + String.join(", ", catalog.cards().keySet()));
                return;
            }
        }
        long usedSeed = seed != null ? seed : ThreadLocalRandom.current().nextInt(1_000_000);

        List<Task> tasks;
        try {
            tasks = catalog.generate(cardName, new Random(usedSeed), CatalogService::isAvailable);
        } catch (CatalogException e) {
            sender.sendMessage("§c" + e.getMessage());
            return;
        }
        if (!game.start(Bukkit.getOnlinePlayers(), tasks)) {
            sender.sendMessage("§cNo hay jugadores conectados.");
            return;
        }

        int size = catalog.cards().get(cardName).size();
        Bukkit.broadcastMessage("§a§lBINGO INICIADO!");
        Bukkit.broadcastMessage("§7Cartilla §f" + cardName + " §7(" + size + "×" + size + ") · semilla §f" + usedSeed);
        for (Player p : Bukkit.getOnlinePlayers()){
            mapManager.giveMap(p);
            scoreboard.show(p);
        }
    }

    private static boolean isNumber(String text) {
        try {
            Long.parseLong(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
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

    // Vuelve a leer cards.yml y tasks/*.yml. Con un error, avisa dónde y sigue el catálogo anterior.
    private void reload(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage("§cNo tienes permiso para recargar el Bingo.");
            return;
        }
        try {
            List<String> summary = catalogs.reload();
            sender.sendMessage("§aTareas recargadas.");
            summary.forEach(line -> sender.sendMessage("§7" + line));
            if (game.isRunning()) {
                sender.sendMessage("§7La partida en curso sigue con sus tareas; los cambios salen en la próxima.");
            }
        } catch (CatalogException e) {
            sender.sendMessage("§cNo se recargó: §f" + e.getMessage());
            sender.sendMessage("§7Siguen las tareas de antes.");
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
