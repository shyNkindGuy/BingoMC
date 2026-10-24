package com.shyNkindguy.bingo.game;

import com.shyNkindguy.bingo.adapter.Keys;
import com.shyNkindguy.bingo.core.game.ScatterPlanner;
import com.shyNkindguy.bingo.core.game.StartRules;
import com.shyNkindguy.bingo.core.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

// Lo que pasa con el mundo y los jugadores al empezar, según config.yml: vaciar inventarios, repartir a los
// equipos alrededor del spawn del mundo principal, poner el borde (y devolver el de antes al terminar) y
// dar el kit al "¡YA!". Quien entra con la partida empezada recibe lo mismo.
public class StartActions {
    private static final int SAFE_TRIES = 8;

    private final BingoGame game;
    private final Map<String, Location> spawns = new HashMap<>(); // equipo → dónde arrancó
    private final Random random = new Random();
    private World borderWorld;
    private double previousBorderSize;
    private Location previousBorderCenter;

    public StartActions(BingoGame game) {
        this.game = game;
    }

    // Al repartir las cartillas, antes de dar los mapas.
    public void prepare() {
        StartRules rules = game.getSettings().start();
        spawns.clear();
        if (rules.clearInventory()) game.playersInGame().forEach(player -> player.getInventory().clear());

        World world = Bukkit.getWorlds().get(0);
        Location center = world.getSpawnLocation();
        if (rules.scatterRadius() > 0) {
            List<Team> teams = new ArrayList<>(game.teamCards().keySet());
            List<ScatterPlanner.Offset> offsets = ScatterPlanner.positions(teams.size(), rules.scatterRadius(), random);
            for (int i = 0; i < teams.size(); i++) {
                Location spot = safeSpot(world, center, offsets.get(i));
                spawns.put(teams.get(i).id(), spot);
                for (Player member : membersOf(teams.get(i))) member.teleport(spot);
            }
        }
        if (rules.borderSize() > 0) {
            WorldBorder border = world.getWorldBorder();
            borderWorld = world;
            previousBorderSize = border.getSize();
            previousBorderCenter = border.getCenter();
            border.setCenter(center);
            border.setSize(rules.borderSize());
        }
    }

    // Al "¡YA!".
    public void giveKits() {
        game.playersInGame().forEach(this::giveKit);
    }

    // Quien entra con la partida empezada, antes de darle el mapa: inventario, lugar de su equipo y, si ya
    // arrancó, el kit.
    public void joinLate(Player player) {
        StartRules rules = game.getSettings().start();
        if (rules.clearInventory()) player.getInventory().clear();
        Optional<Team> team = game.teamInGame(player);
        if (rules.scatterRadius() > 0 && team.isPresent()) {
            World world = Bukkit.getWorlds().get(0);
            Location spot = spawns.computeIfAbsent(team.get().id(), id -> safeSpot(world, world.getSpawnLocation(),
                    ScatterPlanner.extra(rules.scatterRadius(), random)));
            player.teleport(spot);
        }
        if (game.isRunning()) giveKit(player);
    }

    // Al terminar: el borde de antes.
    public void restore() {
        if (borderWorld == null) return;
        WorldBorder border = borderWorld.getWorldBorder();
        border.setCenter(previousBorderCenter);
        border.setSize(previousBorderSize);
        borderWorld = null;
    }

    private void giveKit(Player player) {
        for (StartRules.KitItem kitItem : game.getSettings().start().kit()) {
            Material material = Keys.toMaterial(kitItem.item());
            if (material == null) continue; // CatalogService ya avisó al cargar
            for (ItemStack leftover : player.getInventory().addItem(new ItemStack(material, kitItem.amount())).values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
    }

    private List<Player> membersOf(Team team) {
        List<Player> members = new ArrayList<>();
        game.getTeams().members(team).forEach(id -> {
            Player player = Bukkit.getPlayer(id);
            if (player != null) members.add(player);
        });
        return members;
    }

    // Arriba del bloque más alto en ese punto; si es agua o lava, unos chunks más allá.
    private static Location safeSpot(World world, Location center, ScatterPlanner.Offset offset) {
        int x = center.getBlockX() + offset.x();
        int z = center.getBlockZ() + offset.z();
        for (int i = 0; i < SAFE_TRIES; i++) {
            Block top = world.getHighestBlockAt(x + i * 16, z);
            if (!top.isLiquid()) return top.getLocation().add(0.5, 1, 0.5);
        }
        return world.getHighestBlockAt(x, z).getLocation().add(0.5, 1, 0.5);
    }
}
