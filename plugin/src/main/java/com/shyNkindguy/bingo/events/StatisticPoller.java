package com.shyNkindguy.bingo.events;

import com.shyNkindguy.bingo.adapter.Keys;
import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;
import com.shyNkindguy.bingo.core.event.StatisticReached;
import com.shyNkindguy.bingo.core.progress.StatisticBaseline;
import com.shyNkindguy.bingo.core.task.StatisticTask;
import com.shyNkindguy.bingo.game.BingoGame;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Las estadísticas no tienen evento: una vez por segundo se leen las de las casillas pendientes y se manda
// cuánto subieron desde que empezó la partida (StatisticReached).
public class StatisticPoller {
    private static final long PERIOD_TICKS = 20;

    private final Plugin plugin;
    private final BingoGame game;
    private final CardUpdater updater;
    private final StatisticBaseline<UUID> baseline = new StatisticBaseline<>();
    private BukkitTask task;

    public StatisticPoller(Plugin plugin, BingoGame game, CardUpdater updater) {
        this.plugin = plugin;
        this.game = game;
        this.updater = updater;
    }

    public void start() {
        stop();
        baseline.clear();
        poll(); // el punto de partida se toma ya, antes de que nadie salte
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::poll, PERIOD_TICKS, PERIOD_TICKS);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void poll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            BingoCard card = game.getCard(player);
            if (card == null) continue;

            List<StatisticReached> events = new ArrayList<>();
            for (BingoCell[] row : card.getGrid()) {
                for (BingoCell cell : row) {
                    if (cell.isCompleted() || !(cell.getTask() instanceof StatisticTask statistic)) continue;
                    Integer value = read(player, statistic);
                    if (value == null) continue;
                    int sinceStart = baseline.sinceStart(player.getUniqueId(), statistic.key(), value);
                    events.add(new StatisticReached(statistic.statistic(), statistic.subject(), sinceStart));
                }
            }
            if (!events.isEmpty()) updater.apply(player, events);
        }
    }

    // El valor de siempre de la estadística; null si no se puede leer (TaskAvailability ya las filtra).
    private static Integer read(Player player, StatisticTask task) {
        Statistic statistic = Keys.toStatistic(task.statistic());
        if (statistic == null) return null;
        try {
            return switch (statistic.getType()) {
                case UNTYPED -> player.getStatistic(statistic);
                case BLOCK, ITEM -> {
                    Material material = Keys.toMaterial(task.subject());
                    yield material == null ? null : player.getStatistic(statistic, material);
                }
                case ENTITY -> {
                    EntityType type = Keys.toEntityType(task.subject());
                    yield type == null ? null : player.getStatistic(statistic, type);
                }
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
