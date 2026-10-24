package com.shyNkindguy.bingo.commands;

import com.shyNkindguy.bingo.core.team.Team;
import com.shyNkindguy.bingo.core.team.TeamColor;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.ui.TeamColors;
import com.shyNkindguy.bingo.ui.Texts;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

// /bingo team: armar los equipos antes de la partida. Cada equipo es un color; quien no tiene equipo al
// empezar juega solo.
public class TeamCommand {
    private static final List<String> SUBCOMMANDS = List.of("unir", "crear", "salir", "random", "lista");
    private static final String COLORS = String.join(", ",
            Arrays.stream(TeamColor.values()).map(color -> color.displayName().toLowerCase(Locale.ROOT)).toList());

    private final BingoGame game;
    private final String adminPermission;

    public TeamCommand(BingoGame game, String adminPermission) {
        this.game = game;
        this.adminPermission = adminPermission;
    }

    // `args[0]` es "team".
    public void run(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Texts.get("equipos.uso"));
            return;
        }
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "unir", "crear" -> join(sender, args);
            case "salir" -> leave(sender);
            case "random" -> random(sender, args);
            case "lista" -> list(sender);
            default -> sender.sendMessage(Texts.get("equipos.uso"));
        }
    }

    public List<String> complete(String[] args) {
        if (args.length == 2) return BingoCommand.matching(SUBCOMMANDS, args[1]);
        if (args.length == 3 && (args[1].equalsIgnoreCase("unir") || args[1].equalsIgnoreCase("crear"))) {
            return BingoCommand.matching(Arrays.asList(COLORS.split(", ")), args[2]);
        }
        return List.of();
    }

    private boolean locked(CommandSender sender) {
        if (!game.inGame()) return false;
        sender.sendMessage(Texts.get("equipos.bloqueados"));
        return true;
    }

    private void join(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Texts.get("comando.solo_jugador"));
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(Texts.get("equipos.uso_unir", "colores", COLORS));
            return;
        }
        Optional<TeamColor> color = TeamColor.parse(args[2]);
        if (color.isEmpty()) {
            sender.sendMessage(Texts.get("equipos.color_no_existe", "color", args[2], "colores", COLORS));
            return;
        }
        if (locked(sender)) return;

        Team team = game.getTeams().join(player.getUniqueId(), color.get());
        player.sendMessage(Texts.get("equipos.unido", "equipo", TeamColors.chat(team.color()) + team.label()));
    }

    private void leave(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Texts.get("comando.solo_jugador"));
            return;
        }
        if (locked(sender)) return;
        Optional<Team> left = game.getTeams().leave(player.getUniqueId());
        player.sendMessage(left.map(team -> Texts.get("equipos.salio", "equipo", TeamColors.chat(team.color()) + team.label()))
                .orElse(Texts.get("equipos.sin_equipo")));
    }

    private void random(CommandSender sender, String[] args) {
        if (!sender.hasPermission(adminPermission)) {
            sender.sendMessage(Texts.get("comando.sin_permiso_equipos"));
            return;
        }
        if (locked(sender)) return;

        int count = 2;
        if (args.length >= 3) {
            try {
                count = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Texts.get("equipos.uso_random", "max", TeamColor.values().length));
                return;
            }
        }
        List<UUID> players = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player -> players.add(player.getUniqueId()));
        try {
            game.getTeams().randomize(players, count, new Random());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Texts.get("inicio.error", "error", e.getMessage()));
            return;
        }
        Bukkit.broadcastMessage(Texts.get("equipos.al_azar"));
        broadcastTeams();
    }

    private void list(CommandSender sender) {
        List<String> lines = teamLines();
        if (lines.isEmpty()) {
            sender.sendMessage(Texts.get("equipos.ninguno", "colores", COLORS));
            return;
        }
        lines.forEach(sender::sendMessage);
    }

    private void broadcastTeams() {
        teamLines().forEach(Bukkit::broadcastMessage);
    }

    // "Equipo Rojo: Ana, Beto" por cada equipo de color (con quienes estén conectados).
    private List<String> teamLines() {
        List<String> lines = new ArrayList<>();
        for (Team team : game.getTeams().teams()) {
            if (team.solo()) continue;
            List<String> names = new ArrayList<>();
            for (UUID id : game.getTeams().members(team)) {
                Player member = Bukkit.getPlayer(id);
                names.add(member != null ? member.getName() : Texts.get("equipos.desconectado"));
            }
            lines.add(Texts.get("equipos.linea", "equipo", TeamColors.chat(team.color()) + team.label(),
                    "miembros", String.join(", ", names)));
        }
        return lines;
    }
}
