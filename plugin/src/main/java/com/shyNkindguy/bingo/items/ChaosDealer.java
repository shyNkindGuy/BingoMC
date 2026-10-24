package com.shyNkindguy.bingo.items;

import com.shyNkindguy.bingo.core.game.ChaosRules;
import com.shyNkindguy.bingo.core.item.SpecialItem;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.ui.Texts;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// Modo caos (config.yml `caos`): cada N minutos de juego, un ítem especial al azar de items.yml para cada
// jugador de la partida. Va en BingoGame.onTick.
public class ChaosDealer {
    private final BingoGame game;
    private final SpecialItems items;
    private int lastDealt = -1;

    public ChaosDealer(BingoGame game, SpecialItems items) {
        this.game = game;
        this.items = items;
    }

    public void tick() {
        if (!game.isRunning()) return;
        ChaosRules rules = game.getSettings().chaos();
        int now = game.elapsedSeconds();
        if (!rules.due(now, lastDealt)) return;
        lastDealt = now;

        List<SpecialItem> all = items.all();
        if (all.isEmpty()) return;
        for (Player player : game.playersInGame()) {
            SpecialItem item = all.get(ThreadLocalRandom.current().nextInt(all.size()));
            items.give(player, item);
            player.sendMessage(Texts.get("especiales.recibido", "item", SpecialItems.displayName(item)));
        }
    }

    // Al repartir una partida nueva.
    public void reset() {
        lastDealt = -1;
    }
}
