package com.shyNkindguy.bingo.game;

import com.shyNkindguy.bingo.core.event.EventContext;
import com.shyNkindguy.bingo.core.progress.StatisticBaseline;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

// Dónde y cómo está un jugador ahora, para las condiciones de las tareas: dimensión, bioma, hora del mundo,
// altura y cuántas veces murió desde que empezó la partida.
public class PlayerContexts {
    private static final String DEATHS = "minecraft:deaths";

    private final StatisticBaseline<UUID> deaths = new StatisticBaseline<>();

    // Al empezar la partida: desde acá se cuentan las muertes.
    public void reset(Collection<? extends Player> players) {
        deaths.clear();
        for (Player player : players) of(player);
    }

    public EventContext of(Player player) {
        Location location = player.getLocation();
        World world = location.getWorld();
        int deathsSinceStart = deaths.sinceStart(player.getUniqueId(), DEATHS, player.getStatistic(Statistic.DEATHS));
        if (world == null) return new EventContext(null, null, null, location.getBlockY(), deathsSinceStart);

        return new EventContext(dimension(world), biome(location), world.getTime(), location.getBlockY(),
                deathsSinceStart);
    }

    // El mundo normal, el Nether y el End de cualquier servidor se llaman igual que en vanilla, aunque la
    // carpeta del mundo tenga otro nombre.
    private static String dimension(World world) {
        return switch (world.getEnvironment()) {
            case NORMAL -> "minecraft:overworld";
            case NETHER -> "minecraft:the_nether";
            case THE_END -> "minecraft:the_end";
            default -> world.getKey().toString();
        };
    }

    private static String biome(Location location) {
        try {
            return location.getBlock().getBiome().getKey().toString();
        } catch (RuntimeException e) {
            return null; // bioma de un datapack sin clave
        }
    }
}
