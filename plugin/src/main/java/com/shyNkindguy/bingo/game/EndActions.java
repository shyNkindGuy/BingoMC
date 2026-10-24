package com.shyNkindguy.bingo.game;

import com.shyNkindguy.bingo.core.game.EndRules;
import com.shyNkindguy.bingo.core.team.Team;
import com.shyNkindguy.bingo.ui.TeamColors;
import com.shyNkindguy.bingo.ui.Texts;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Lo que pasa al terminar, según config.yml: fuegos artificiales del color del equipo ganador durante
// `espera_final` segundos y después, si se pidió, cada jugador vuelve a donde estaba al empezar y se vacían
// los inventarios de quienes jugaron. Una partida nueva cancela lo que quedaba pendiente.
public class EndActions {
    private static final long TICKS_PER_SECOND = 20;

    private final Plugin plugin;
    private final BingoGame game;
    private final StartActions start;
    private BukkitTask pending;

    public EndActions(Plugin plugin, BingoGame game, StartActions start) {
        this.plugin = plugin;
        this.game = game;
        this.start = start;
    }

    public void finish() {
        cancel();
        EndRules rules = game.getSettings().end();
        List<Player> winners = new ArrayList<>();
        List<Color> colors = new ArrayList<>();
        for (Team team : game.lastWinners()) {
            for (UUID id : game.getTeams().members(team)) {
                Player player = Bukkit.getPlayer(id);
                if (player != null) {
                    winners.add(player);
                    colors.add(TeamColors.fireworkColor(team.color()));
                }
            }
        }
        Map<UUID, Location> origins = start.origins();
        Set<UUID> players = game.lastParticipants();

        if (rules.waitSeconds() > 0) {
            if (rules.returnToOrigin()) Bukkit.broadcastMessage(Texts.get("fin.aviso_volver", "segundos", rules.waitSeconds()));
            if (rules.clearInventory()) Bukkit.broadcastMessage(Texts.get("fin.aviso_limpiar", "segundos", rules.waitSeconds()));
        }

        int[] left = {rules.waitSeconds()};
        pending = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (left[0] <= 0) {
                cancel();
                afterParty(rules, origins, players);
                return;
            }
            if (left[0] % 2 == 0) {
                for (int i = 0; i < winners.size(); i++) {
                    if (winners.get(i).isOnline()) firework(winners.get(i), colors.get(i));
                }
            }
            left[0]--;
        }, 0, TICKS_PER_SECOND);
    }

    // Al repartir una partida nueva: que no vuelva ni limpie a nadie de la anterior.
    public void cancel() {
        if (pending != null) {
            pending.cancel();
            pending = null;
        }
    }

    private static void afterParty(EndRules rules, Map<UUID, Location> origins, Set<UUID> players) {
        for (UUID id : players) {
            Player player = Bukkit.getPlayer(id);
            if (player == null) continue;
            if (rules.clearInventory()) player.getInventory().clear();
            Location origin = origins.get(id);
            if (rules.returnToOrigin() && origin != null) player.teleport(origin);
        }
    }

    private static void firework(Player player, Color color) {
        Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().withColor(color).withFade(Color.WHITE)
                .with(FireworkEffect.Type.BALL_LARGE).withFlicker().build());
        meta.setPower(1);
        firework.setFireworkMeta(meta);
    }
}
