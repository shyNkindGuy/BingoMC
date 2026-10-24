package com.shyNkindguy.bingo.ui;

import com.shyNkindguy.bingo.core.team.TeamColor;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeamColorsTest {

    @Test
    void cadaColorDeEquipoTieneSuColorDeChat() {
        Set<ChatColor> usados = EnumSet.noneOf(ChatColor.class);
        for (TeamColor color : TeamColor.values()) usados.add(TeamColors.chatColor(color));
        assertEquals(TeamColor.values().length, usados.size());
    }

    @Test
    void cadaColorDeEquipoTieneSuColorDeFuegosArtificiales() {
        Set<Integer> usados = new HashSet<>();
        for (TeamColor color : TeamColor.values()) usados.add(TeamColors.fireworkColor(color).asRGB());
        assertEquals(TeamColor.values().length, usados.size());
        assertEquals(Color.RED, TeamColors.fireworkColor(TeamColor.RED));
    }

    @Test
    void elCodigoVaDelanteDelTexto() {
        assertEquals("§c", TeamColors.chat(TeamColor.RED));
        assertEquals("§9", TeamColors.chat(TeamColor.BLUE));
    }
}
