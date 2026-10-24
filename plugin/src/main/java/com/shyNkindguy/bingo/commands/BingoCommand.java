package com.shyNkindguy.bingo.commands;

import com.shyNkindguy.bingo.config.CatalogService;
import com.shyNkindguy.bingo.core.catalog.Catalog;
import com.shyNkindguy.bingo.core.catalog.CatalogException;
import com.shyNkindguy.bingo.core.game.GameSettings;
import com.shyNkindguy.bingo.core.stats.PlayerStats;
import com.shyNkindguy.bingo.core.task.Task;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.storage.StatsService;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import com.shyNkindguy.bingo.ui.CardMenu;
import com.shyNkindguy.bingo.ui.GameBossBar;
import com.shyNkindguy.bingo.ui.Texts;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class BingoCommand implements CommandExecutor, TabCompleter {
    private static final String ADMIN_PERMISSION = "bingo.admin";
    private static final List<String> ADMIN_SUBCOMMANDS = List.of("start", "stop", "pause", "map", "card", "stats", "reload", "team");
    private static final List<String> PLAYER_SUBCOMMANDS = List.of("map", "card", "stats", "team");
    private static final Locale SPANISH = Locale.forLanguageTag("es");

    private final BingoGame game;
    private final BingoScoreBoard scoreboard;
    private final BingoMapManager mapManager;
    private final CatalogService catalogs;
    private final StatsService stats;
    private final TeamCommand teams;

    public BingoCommand(BingoGame game, BingoScoreBoard scoreboard, BingoMapManager mapManager, CatalogService catalogs,
                        StatsService stats){
        this.game = game; this.scoreboard = scoreboard; this.mapManager = mapManager; this.catalogs = catalogs;
        this.stats = stats;
        this.teams = new TeamCommand(game, ADMIN_PERMISSION);
    }


    @Override

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0) {
            sender.sendMessage(Texts.get("comando.uso"));
            return true;
        }
        String subcommand = args[0].toLowerCase(Locale.ROOT);
        if (subcommand.equals("team")) {
            teams.run(sender, args);
            return true;
        }
        if (subcommand.equals("stats") && args.length <= 2) {
            showStats(sender, args);
            return true;
        }
        if (!subcommand.equals("start") && args.length != 1) {
            sender.sendMessage(Texts.get("comando.uso"));
            return true;
        }
        switch (subcommand) {
            case "start" -> start(sender, args);
            case "stop" -> stop(sender);
            case "pause", "pausa" -> pause(sender);
            case "map" -> giveMap(sender);
            case "card", "cartilla" -> openCard(sender);
            case "reload" -> reload(sender);
            default -> sender.sendMessage(Texts.get("comando.uso"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        boolean admin = sender.hasPermission(ADMIN_PERMISSION);
        if (args.length == 1) {
            return matching(admin ? ADMIN_SUBCOMMANDS : PLAYER_SUBCOMMANDS, args[0]);
        }
        if (args[0].equalsIgnoreCase("team")) {
            return teams.complete(args);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("stats")) {
            return matching(stats.book().names(), args[1]);
        }
        if (args.length == 2 && admin && args[0].equalsIgnoreCase("start")) {
            return matching(new ArrayList<>(catalogs.get().cards().keySet()), args[1]);
        }
        return List.of();
    }

    static List<String> matching(List<String> options, String typed) {
        String prefix = typed.toLowerCase(Locale.ROOT);
        return options.stream().filter(option -> option.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
    }

    // /bingo start [cartilla] [semilla]: sin cartilla, la de config.yml o la por defecto de cards.yml; sin
    // semilla, la de config.yml o una al azar que se anuncia para poder repetir la partida.
    private void start(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(Texts.get("comando.sin_permiso_iniciar"));
            return;
        }
        if (game.inGame()) {
            sender.sendMessage(Texts.get("inicio.ya_en_curso"));
            return;
        }
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            sender.sendMessage(Texts.get("inicio.sin_jugadores"));
            return;
        }

        Catalog catalog = catalogs.get();
        GameSettings settings = catalogs.settings();
        String cardName = settings.card() != null ? settings.card() : catalog.defaultCard();
        Long seed = settings.seed();
        for (int i = 1; i < args.length; i++) {
            if (catalog.cards().containsKey(args[i])) {
                cardName = args[i];
            } else if (isNumber(args[i])) {
                seed = Long.parseLong(args[i]);
            } else {
                sender.sendMessage(Texts.get("inicio.cartilla_no_existe", "cartilla", args[i],
                        "cartillas", String.join(", ", catalog.cards().keySet())));
                return;
            }
        }
        long usedSeed = seed != null ? seed : ThreadLocalRandom.current().nextInt(1_000_000);

        List<Task> tasks;
        try {
            tasks = catalog.generate(cardName, new Random(usedSeed), catalogs::isAvailable);
        } catch (CatalogException e) {
            sender.sendMessage(Texts.get("inicio.error", "error", e.getMessage()));
            return;
        }
        GameSettings used = settings.withCard(cardName).withSeed(usedSeed);
        int size = catalog.cards().get(cardName).size();
        String problem = used.mode().problemWith(size);
        if (problem != null) {
            sender.sendMessage(Texts.get("inicio.modo_imposible", "modo", used.mode().description(),
                    "cartilla", cardName, "problema", problem));
            return;
        }
        String duration = used.durationMinutes() == 0 ? Texts.get("inicio.sin_limite")
                : Texts.get("inicio.minutos", "minutos", used.durationMinutes());
        Bukkit.broadcastMessage(Texts.get("inicio.anuncio"));
        Bukkit.broadcastMessage(Texts.get("inicio.detalle", "cartilla", cardName, "lado", size, "semilla", usedSeed,
                "modo", used.mode().description(), "duracion", duration, "pvp", used.pvp() ? "" : Texts.get("inicio.sin_pvp")));
        if (!game.start(Bukkit.getOnlinePlayers(), tasks, used)) {
            sender.sendMessage(Texts.get("inicio.no_se_pudo"));
            return;
        }
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
            sender.sendMessage(Texts.get("comando.sin_permiso_detener"));
            return;
        }
        if (!game.stop(sender.getName())) {
            sender.sendMessage(Texts.get("partida.no_hay"));
        }
    }

    private void pause(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(Texts.get("comando.sin_permiso_pausar"));
            return;
        }
        if (!game.togglePause(sender.getName())) {
            sender.sendMessage(Texts.get("pausa.no_se_puede"));
            return;
        }
        scoreboard.updateAll();
    }

    // Vuelve a leer cards.yml y tasks/*.yml. Con un error, avisa dónde y sigue el catálogo anterior.
    private void reload(CommandSender sender) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(Texts.get("comando.sin_permiso_recargar"));
            return;
        }
        try {
            List<String> summary = catalogs.reload();
            sender.sendMessage(Texts.get("recarga.ok"));
            summary.forEach(line -> sender.sendMessage(Texts.get("recarga.linea", "linea", line)));
            if (game.inGame()) {
                sender.sendMessage(Texts.get("recarga.partida_en_curso"));
            }
        } catch (CatalogException e) {
            sender.sendMessage(Texts.get("recarga.error", "error", e.getMessage()));
            sender.sendMessage(Texts.get("recarga.sigue"));
        }
    }

    // /bingo stats [jugador]: las suyas o las de otro (también de alguien desconectado, por nombre).
    private void showStats(CommandSender sender, String[] args) {
        String who;
        Optional<PlayerStats> found;
        if (args.length == 2) {
            who = args[1];
            found = stats.book().findByName(who);
        } else if (sender instanceof Player player) {
            who = player.getName();
            found = stats.book().get(player.getUniqueId().toString());
        } else {
            sender.sendMessage(Texts.get("estadisticas.uso_consola"));
            return;
        }
        if (found.isEmpty() || found.get().games() == 0) {
            sender.sendMessage(Texts.get("estadisticas.sin_datos", "jugador", who));
            return;
        }
        statsLines(found.get()).forEach(sender::sendMessage);
    }

    static List<String> statsLines(PlayerStats player) {
        List<String> lines = new ArrayList<>();
        lines.add(Texts.get("estadisticas.titulo", "jugador", player.name()));
        lines.add(Texts.get("estadisticas.partidas", "partidas", player.games(), "victorias", player.wins(),
                "porcentaje", player.winPercent()));
        lines.add(Texts.get("estadisticas.casillas", "casillas", player.cells(),
                "promedio", String.format(SPANISH, "%.1f", player.cellsPerGame())));
        lines.add(player.hasWon()
                ? Texts.get("estadisticas.mejor_tiempo", "tiempo", GameBossBar.clock(player.bestWinSeconds()))
                : Texts.get("estadisticas.sin_victorias"));
        return lines;
    }

    private void openCard(CommandSender sender) {
        if (sender instanceof Player player) {
            CardMenu.open(player, game);
        } else {
            sender.sendMessage(Texts.get("comando.solo_jugador"));
        }
    }

    private void giveMap(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Texts.get("comando.solo_jugador"));
            return;
        }
        mapManager.giveMap(player);
        player.sendMessage(Texts.get("mapa.dado"));
    }

    }
