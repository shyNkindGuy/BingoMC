package com.shyNkindguy.bingo.events;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

// Las reglas de la partida que no son de las tareas: durante la cuenta regresiva nadie se mueve, rompe,
// pone, agarra ni recibe daño; sin PvP los jugadores no se pegan; y quien entra con la partida empezada
// recibe su cartilla.
public class SessionListener implements Listener {
    private final Plugin plugin;
    private final BingoGame game;
    private final BingoMapManager mapManager;
    private final BingoScoreBoard scoreboard;

    public SessionListener(Plugin plugin, BingoGame game, BingoMapManager mapManager, BingoScoreBoard scoreboard) {
        this.plugin = plugin;
        this.game = game;
        this.mapManager = mapManager;
        this.scoreboard = scoreboard;
    }

    private boolean frozen(Player player) {
        return game.inCountdown() && game.getCard(player) != null;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!frozen(event.getPlayer())) return;
        Location from = event.getFrom();
        Location to = event.getTo();
        // mirar alrededor sí; caminar o saltar no
        if (to != null && (from.getX() != to.getX() || from.getZ() != to.getZ() || to.getY() > from.getY())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (frozen(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (frozen(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && frozen(player)) event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && frozen(player)) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!game.isRunning() || game.getSettings().pvp()) return;
        if (event.getEntity() instanceof Player && attacker(event.getDamager()) != null) {
            event.setCancelled(true);
        }
    }

    // El jugador que pegó: directo o con un proyectil (flecha, tridente…).
    private static Player attacker(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) return player;
        return null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!game.inGame()) return;

        boolean isNew = game.getCard(player) == null;
        BingoCard card = game.join(player);
        if (card == null) return;

        // un tick después, cuando el jugador ya está en el mundo
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline() || game.getCard(player) != card) return;
            mapManager.giveMap(player);
            scoreboard.show(player);
            if (isNew) {
                player.sendMessage("§aEntraste a la partida de bingo en curso: tu cartilla está en la mano izquierda.");
                if (game.isRunning()) AdvancementReset.forPlayer(player, card);
            }
        });
    }
}
