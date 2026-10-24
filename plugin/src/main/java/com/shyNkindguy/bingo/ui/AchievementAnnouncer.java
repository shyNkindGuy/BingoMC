package com.shyNkindguy.bingo.ui;

import com.shyNkindguy.bingo.core.bus.AchievementUnlocked;
import com.shyNkindguy.bingo.core.bus.DomainEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

// Cuando alguien desbloquea un logro: aviso en el chat para todos y título + sonido para él. Va 2 segundos
// después, para no pisar el título de la casilla que lo desbloqueó.
public final class AchievementAnnouncer {
    private static final long DELAY_TICKS = 40;

    private AchievementAnnouncer() {
    }

    public static void subscribeTo(DomainEvents events, Plugin plugin) {
        events.subscribe(AchievementUnlocked.class,
                event -> Bukkit.getScheduler().runTaskLater(plugin, () -> announce(event), DELAY_TICKS));
    }

    private static void announce(AchievementUnlocked event) {
        Bukkit.broadcastMessage(Texts.get("logros.anuncio", "jugador", event.playerName(), "logro", event.name(),
                "descripcion", event.description()));
        Player player = Bukkit.getPlayer(UUID.fromString(event.playerId()));
        if (player == null) return;
        player.sendTitle(Texts.get("logros.titulo"), Texts.get("logros.subtitulo", "logro", event.name()), 10, 50, 10);
        player.playSound(player.getLocation(), "minecraft:ui.toast.challenge_complete", 1f, 1.2f);
    }
}
