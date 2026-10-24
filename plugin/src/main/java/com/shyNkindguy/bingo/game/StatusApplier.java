package com.shyNkindguy.bingo.game;

import com.shyNkindguy.bingo.core.status.ItemSet;
import com.shyNkindguy.bingo.core.status.PlayerStatus;
import com.shyNkindguy.bingo.core.status.StatusResolver;
import com.shyNkindguy.bingo.items.SpecialItems;
import com.shyNkindguy.bingo.ui.Texts;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Los estados de cada jugador con la partida en curso: los de config.yml `estados` más los de los conjuntos de
// los que lleva suficientes piezas (StatusResolver). Va en BingoGame.onTick: cada segundo vuelve a poner los
// efectos (duran 20 s, así la visión nocturna no parpadea) y saca los que ya no le tocan. Avisa cuando un
// conjunto se activa o se apaga.
public class StatusApplier {
    private static final int DURATION_TICKS = 400;

    private final BingoGame game;
    private final SpecialItems items;
    private final Map<UUID, Set<String>> applied = new HashMap<>();     // efectos que le puso
    private final Map<UUID, Set<String>> activeSets = new HashMap<>();

    public StatusApplier(BingoGame game, SpecialItems items) {
        this.game = game;
        this.items = items;
    }

    public void tick() {
        if (!game.isRunning()) return;
        List<PlayerStatus> base = game.getSettings().start().statuses();
        List<ItemSet> sets = items.sets();
        if (base.isEmpty() && sets.isEmpty() && applied.isEmpty()) return;
        for (Player player : game.playersInGame()) {
            apply(player, StatusResolver.resolve(base, sets, items.piecesCarried(player)));
        }
    }

    private void apply(Player player, StatusResolver.Result result) {
        UUID id = player.getUniqueId();
        Set<String> now = new HashSet<>();
        for (PlayerStatus status : result.statuses()) {
            PotionEffectType type = effect(status.effect());
            if (type == null) continue;
            player.addPotionEffect(new PotionEffect(type, DURATION_TICKS, status.level() - 1, true, false, true));
            now.add(status.effect());
        }
        for (String effect : applied.getOrDefault(id, Set.of())) {
            if (now.contains(effect)) continue;
            PotionEffectType type = effect(effect);
            if (type != null) player.removePotionEffect(type);
        }
        applied.put(id, now);

        Set<String> before = activeSets.getOrDefault(id, Set.of());
        Set<String> after = new HashSet<>();
        for (ItemSet set : result.activeSets()) {
            after.add(set.id());
            if (!before.contains(set.id())) {
                player.sendMessage(Texts.get("conjuntos.activo", "conjunto", set.name(), "estados",
                        String.join(", ", set.statuses().stream().map(PlayerStatus::description).toList())));
            }
        }
        for (String setId : before) {
            if (!after.contains(setId)) {
                String name = items.findSet(setId).map(ItemSet::name).orElse(setId);
                player.sendMessage(Texts.get("conjuntos.inactivo", "conjunto", name));
            }
        }
        activeSets.put(id, after);
    }

    // Al terminar (y al repartir otra): saca los efectos que puso a los que están conectados.
    public void clear() {
        for (Map.Entry<UUID, Set<String>> entry : applied.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;
            for (String effect : entry.getValue()) {
                PotionEffectType type = effect(effect);
                if (type != null) player.removePotionEffect(type);
            }
        }
        applied.clear();
        activeSets.clear();
    }

    private static PotionEffectType effect(String key) {
        try {
            NamespacedKey namespaced = NamespacedKey.fromString(key);
            return namespaced == null ? null : Registry.EFFECT.get(namespaced);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
