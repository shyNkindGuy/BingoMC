package com.shyNkindguy.bingo.events;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.team.Team;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.game.StartActions;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import com.shyNkindguy.bingo.ui.ReadOnlyMenu;
import com.shyNkindguy.bingo.ui.TeamColors;
import com.shyNkindguy.bingo.ui.Texts;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

// Las reglas de la partida que no son de las tareas: durante la cuenta regresiva y en pausa nadie se mueve, rompe,
// pone, agarra ni recibe daño; sin PvP los jugadores no se pegan (y nunca a su equipo); quien entra con la
// partida empezada recibe su cartilla; y `#mensaje` en el chat va solo al equipo.
public class SessionListener implements Listener {
    private final Plugin plugin;
    private final BingoGame game;
    private final BingoMapManager mapManager;
    private final BingoScoreBoard scoreboard;
    private final StartActions startActions;

    public SessionListener(Plugin plugin, BingoGame game, BingoMapManager mapManager, BingoScoreBoard scoreboard,
                           StartActions startActions) {
        this.plugin = plugin;
        this.game = game;
        this.mapManager = mapManager;
        this.scoreboard = scoreboard;
        this.startActions = startActions;
    }

    private boolean frozen(Player player) {
        return game.isFrozen() && game.getCard(player) != null;
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

    // /bingo card y /bingo logros son para mirar: no se saca ni se mueve nada.
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof ReadOnlyMenu) event.setCancelled(true);
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof ReadOnlyMenu) event.setCancelled(true);
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

    // Sin PvP nadie le pega a otro jugador; con PvP, igual nadie le pega a su propio equipo.
    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!game.isRunning() || !(event.getEntity() instanceof Player victim)) return;
        Player attacker = attacker(event.getDamager());
        if (attacker == null || attacker.equals(victim)) return;

        boolean sameTeam = game.teamInGame(attacker).isPresent()
                && game.teamInGame(attacker).equals(game.teamInGame(victim));
        if (!game.getSettings().pvp() || sameTeam) event.setCancelled(true);
    }

    // El jugador que pegó: directo o con un proyectil (flecha, tridente…).
    static Player attacker(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) return player;
        return null;
    }

    // "#vamos al norte" → solo lo lee su equipo. El chat corre en otro hilo: solo se leen los equipos.
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        if (!message.startsWith("#") || message.length() < 2) return;

        Player player = event.getPlayer();
        Optional<Team> team = game.getTeams().teamOf(player.getUniqueId());
        if (team.isEmpty()) {
            player.sendMessage(Texts.get("chat.sin_equipo"));
            return;
        }
        Set<UUID> mates = new HashSet<>(game.getTeams().members(team.get()));
        event.getRecipients().removeIf(recipient -> !mates.contains(recipient.getUniqueId()));
        event.setMessage(message.substring(1).trim());
        event.setFormat(Texts.get("chat.equipo", "color", TeamColors.chat(team.get().color()), "equipo", team.get().name()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!game.inGame()) return;

        boolean isNew = !game.isParticipant(player);
        BingoCard card = game.join(player);
        if (card == null) return;

        // un tick después, cuando el jugador ya está en el mundo
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline() || game.getCard(player) != card) return;
            if (isNew) startActions.joinLate(player);
            mapManager.giveMap(player);
            scoreboard.show(player);
            if (isNew) {
                player.sendMessage(Texts.get("partida.entraste"));
                if (game.isRunning()) AdvancementReset.forPlayer(player, card);
            }
        });
    }
}
