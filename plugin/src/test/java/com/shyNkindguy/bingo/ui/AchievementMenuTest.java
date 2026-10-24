package com.shyNkindguy.bingo.ui;

import com.shyNkindguy.bingo.core.achievement.Achievement;
import com.shyNkindguy.bingo.core.achievement.GameRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AchievementMenuTest {
    private static final Achievement VICTORIA = new Achievement("primera_victoria", "¡Bingo!", "Gana una partida",
            "minecraft:gold_ingot", new GameRule(true, 0, null, 0, false, 0, 0, 0));

    @Test
    void desbloqueadoEnVerdeYBloqueadoEnGris() {
        assertEquals(new AchievementMenu.Entry("§a¡Bingo!", List.of("§fGana una partida", "§a✔ Desbloqueado")),
                AchievementMenu.entry(VICTORIA, true));
        assertEquals(new AchievementMenu.Entry("§7¡Bingo!", List.of("§fGana una partida", "§8✘ Todavía no")),
                AchievementMenu.entry(VICTORIA, false));
    }
}
