package com.shyNkindguy.bingo.events;

import com.shyNkindguy.bingo.config.CatalogService;
import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.game.Timers;
import com.shyNkindguy.bingo.core.item.BiomeTargets;
import com.shyNkindguy.bingo.core.item.ItemEffect;
import com.shyNkindguy.bingo.core.item.Reroll;
import com.shyNkindguy.bingo.core.item.SpecialItem;
import com.shyNkindguy.bingo.core.task.AdvancementTask;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.items.SpecialItems;
import com.shyNkindguy.bingo.ui.BingoMapManager;
import com.shyNkindguy.bingo.ui.BingoScoreBoard;
import com.shyNkindguy.bingo.ui.Texts;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.util.BiomeSearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

// Los ítems especiales al usarlos (clic derecho con el ítem en la mano): dado del caos, brújula al bioma y
// escudo anti-PvP. Solo sirven con la partida en curso y para quien juega.
public class SpecialItemListener implements Listener {
    private static final int COMPASS_WAIT_SECONDS = 10;
    private static final int SEARCH_STEP = 32;
    private static final int SEARCH_STEP_Y = 64;

    private final BingoGame game;
    private final SpecialItems items;
    private final CatalogService catalogs;
    private final BingoMapManager mapManager;
    private final BingoScoreBoard scoreboard;
    private final Timers<UUID> shields = new Timers<>();
    private final Timers<UUID> compassWait = new Timers<>();

    public SpecialItemListener(BingoGame game, SpecialItems items, CatalogService catalogs, BingoMapManager mapManager,
                               BingoScoreBoard scoreboard) {
        this.game = game;
        this.items = items;
        this.catalogs = catalogs;
        this.mapManager = mapManager;
        this.scoreboard = scoreboard;
    }

    // Al repartir una partida nueva: sin escudos ni esperas de la anterior.
    public void reset() {
        shields.clear();
        compassWait.clear();
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        ItemStack item = event.getItem();
        Optional<SpecialItem> special = items.of(item);
        if (special.isEmpty()) return;
        event.setCancelled(true);

        Player player = event.getPlayer();
        if (!game.isRunning() || game.teamInGame(player).isEmpty()) {
            player.sendMessage(Texts.get("especiales.solo_en_partida"));
            return;
        }
        ItemEffect effect = special.get().effect();
        boolean used = switch (effect) {
            case ItemEffect.Reroll dice -> reroll(player);
            case ItemEffect.BiomeCompass compass -> point(player, item, compass);
            case ItemEffect.PvpShield shield -> shield(player, shield);
        };
        if (used && effect.consumed()) item.setAmount(item.getAmount() - 1);
    }

    // El escudo: a quien lo tiene no le pega ningún jugador (ni con proyectiles).
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!game.isRunning() || !(event.getEntity() instanceof Player victim)) return;
        Player attacker = SessionListener.attacker(event.getDamager());
        if (attacker == null || attacker.equals(victim)) return;
        int left = shields.remaining(victim.getUniqueId(), game.elapsedSeconds());
        if (left == 0) return;
        event.setCancelled(true);
        attacker.sendMessage(Texts.get("especiales.escudo_bloquea", "jugador", victim.getName(), "segundos", left));
    }

    // Las de logro no salen: el jugador ya podría tenerlo y no se le puede volver a quitar a mitad de partida.
    private boolean reroll(Player player) {
        Optional<Reroll.Choice> choice = game.reroll(player, catalogs.get(),
                task -> catalogs.isAvailable(task) && !(task.base() instanceof AdvancementTask));
        if (choice.isEmpty()) {
            player.sendMessage(Texts.get("especiales.sin_cambio"));
            return false;
        }
        String message = Texts.get("especiales.cambio", "jugador", player.getName(), "antes", choice.get().before().displayName(),
                "despues", choice.get().replacement().displayName());
        if (game.getSettings().mode().lockout()) {
            Bukkit.broadcastMessage(message);
            mapManager.invalidateAll();
        } else {
            for (Player mate : game.teammates(player)) {
                mate.sendMessage(message);
                mapManager.invalidate(mate);
            }
        }
        scoreboard.updateAll();
        return true;
    }

    private boolean point(Player player, ItemStack item, ItemEffect.BiomeCompass compass) {
        UUID id = player.getUniqueId();
        int now = game.elapsedSeconds();
        if (compassWait.active(id, now)) {
            player.sendMessage(Texts.get("especiales.brujula_espera", "segundos", compassWait.remaining(id, now)));
            return false;
        }
        BingoCard card = game.getCard(player);
        Set<String> wanted = card == null ? Set.of() : BiomeTargets.of(card);
        List<Biome> biomes = new ArrayList<>();
        for (String key : wanted) {
            Biome biome = biome(key);
            if (biome != null) biomes.add(biome);
        }
        if (biomes.isEmpty()) {
            player.sendMessage(Texts.get("especiales.brujula_sin_bioma"));
            return false;
        }

        compassWait.add(id, now, COMPASS_WAIT_SECONDS);
        Location from = player.getLocation();
        BiomeSearchResult found = player.getWorld().locateNearestBiome(from, compass.radius(), SEARCH_STEP, SEARCH_STEP_Y,
                biomes.toArray(new Biome[0]));
        if (found == null) {
            player.sendMessage(Texts.get("especiales.brujula_lejos", "biomas", String.join(", ", shortKeys(wanted)),
                    "radio", compass.radius()));
            return false;
        }
        if (item.getItemMeta() instanceof CompassMeta meta) {
            meta.setLodestone(found.getLocation());
            meta.setLodestoneTracked(false);
            item.setItemMeta(meta);
        }
        player.sendMessage(Texts.get("especiales.brujula", "bioma", shortKey(found.getBiome().getKey().toString()),
                "distancia", (int) Math.round(from.distance(found.getLocation()))));
        return true;
    }

    private boolean shield(Player player, ItemEffect.PvpShield shield) {
        UUID id = player.getUniqueId();
        int now = game.elapsedSeconds();
        shields.add(id, now, shield.seconds());
        player.sendMessage(Texts.get("especiales.escudo", "segundos", shields.remaining(id, now)));
        player.playSound(player.getLocation(), "minecraft:item.shield.block", 1f, 1f);
        return true;
    }

    // Un bioma de un datapack o de otra versión puede no estar.
    private static Biome biome(String key) {
        try {
            NamespacedKey namespaced = NamespacedKey.fromString(key);
            return namespaced == null ? null : Registry.BIOME.get(namespaced);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static List<String> shortKeys(Set<String> keys) {
        return keys.stream().map(SpecialItemListener::shortKey).toList();
    }

    private static String shortKey(String key) {
        return key.startsWith("minecraft:") ? key.substring("minecraft:".length()) : key;
    }
}
