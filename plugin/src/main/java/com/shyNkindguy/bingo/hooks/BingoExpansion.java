package com.shyNkindguy.bingo.hooks;

import com.shyNkindguy.bingo.core.stats.Placeholders;
import com.shyNkindguy.bingo.core.stats.PlayerStats;
import com.shyNkindguy.bingo.core.team.Team;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.storage.StatsService;
import com.shyNkindguy.bingo.ui.GameBossBar;
import com.shyNkindguy.bingo.ui.Texts;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

// %adaptivebingo_...% para PlaceholderAPI, si está instalado (plugin.yml lo tiene en softdepend y AdaptiveBingo
// solo carga esta clase si lo encuentra). Las de estadísticas y el top salen de core/stats/Placeholders; las de la
// partida en curso son en_partida (sí/no), equipo y casillas_partida.
public class BingoExpansion extends PlaceholderExpansion {
    private final String version;
    private final BingoGame game;
    private final StatsService stats;

    public BingoExpansion(String version, BingoGame game, StatsService stats) {
        this.version = version;
        this.game = game;
        this.stats = stats;
    }

    @Override
    public String getIdentifier() {
        return "adaptivebingo";
    }

    @Override
    public String getAuthor() {
        return "shyNkindguy";
    }

    @Override
    public String getVersion() {
        return version;
    }

    // Que no se borre con /papi reload.
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        Player online = player == null ? null : player.getPlayer();
        switch (params) {
            case "en_partida" -> {
                boolean playing = online != null && game.inGame() && game.teamInGame(online).isPresent();
                return Texts.get(playing ? "placeholders.si" : "placeholders.no");
            }
            case "equipo" -> {
                Optional<Team> team = online == null ? Optional.empty() : game.teamInGame(online);
                return team.map(Team::label).orElse(Texts.get("placeholders.ninguno"));
            }
            case "casillas_partida" -> {
                return player == null ? "0" : String.valueOf(game.getProgress(player.getUniqueId()));
            }
            default -> {
                PlayerStats mine = player == null ? null : stats.book().get(player.getUniqueId().toString()).orElse(null);
                return Placeholders.resolve(params, mine, stats.book().snapshot(), GameBossBar::clock,
                        Texts.get("placeholders.ninguno"));
            }
        }
    }
}
