package com.shyNkindguy.bingo.ui;

import com.shyNkindguy.bingo.core.team.TeamColor;
import org.bukkit.ChatColor;

// El color de chat de cada color de equipo.
public final class TeamColors {

    private TeamColors() {
    }

    public static ChatColor chatColor(TeamColor color) {
        return switch (color) {
            case RED -> ChatColor.RED;
            case BLUE -> ChatColor.BLUE;
            case GREEN -> ChatColor.GREEN;
            case YELLOW -> ChatColor.YELLOW;
            case AQUA -> ChatColor.AQUA;
            case PURPLE -> ChatColor.DARK_PURPLE;
            case ORANGE -> ChatColor.GOLD;
            case PINK -> ChatColor.LIGHT_PURPLE;
            case WHITE -> ChatColor.WHITE;
            case GRAY -> ChatColor.GRAY;
        };
    }

    // "§c" para escribirlo delante de un texto.
    public static String chat(TeamColor color) {
        return chatColor(color).toString();
    }
}
