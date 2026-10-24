package com.shyNkindguy.bingo.events;

import com.shyNkindguy.bingo.adapter.Keys;
import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;
import com.shyNkindguy.bingo.core.task.AdvancementTask;
import com.shyNkindguy.bingo.game.BingoGame;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.util.List;

// Al empezar, cada jugador pierde los logros que hay en su cartilla (solo esos): si ya los tenía, Minecraft
// no se los vuelve a dar y la casilla quedaría imposible.
public final class AdvancementReset {

    private AdvancementReset() {
    }

    public static void forGame(BingoGame game) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            BingoCard card = game.getCard(player);
            if (card == null) continue;
            int revoked = revoke(player, card);
            if (revoked > 0) {
                player.sendMessage("§7Se reiniciaron " + revoked + " logros de tu cartilla para que cuenten en esta partida.");
            }
        }
    }

    private static int revoke(Player player, BingoCard card) {
        int revoked = 0;
        for (BingoCell[] row : card.getGrid()) {
            for (BingoCell cell : row) {
                if (!(cell.getTask().base() instanceof AdvancementTask task)) continue;
                Advancement advancement = Keys.toAdvancement(task.advancement());
                if (advancement == null) continue;

                AdvancementProgress progress = player.getAdvancementProgress(advancement);
                List<String> awarded = List.copyOf(progress.getAwardedCriteria());
                if (awarded.isEmpty()) continue;
                awarded.forEach(progress::revokeCriteria);
                revoked++;
            }
        }
        return revoked;
    }
}
