package com.shyNkindguy.bingo.ui;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.team.Team;
import com.shyNkindguy.bingo.game.BingoGame;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import java.util.*;

// El scoreboard lateral de cada jugador: su equipo, sus casillas, cómo se gana y la tabla de equipos (los
// primeros MAX_TEAMS), con los nombres de los jugadores pintados del color de su equipo. Sin parpadeo: cada
// línea es una entrada fija e invisible ("§0", "§1"…) y el texto va en el prefijo de un Team, así que
// actualizar es cambiar prefijos, no borrar y volver a escribir.
public class BingoScoreBoard {
        private static final int MAX_TEAMS = 8;
        private static final String TAG_PREFIX = "bingo";
        private static final String LINE_PREFIX = "linea";
        private static final String[] ENTRIES = Arrays.stream(ChatColor.values()).limit(15)
                .map(color -> color + "§r").toArray(String[]::new);

        private final BingoGame game;
        private final ScoreboardManager manager;
        private final Map<UUID, Scoreboard> boards = new HashMap<>();
        private final Map<UUID, Objective> objectives = new HashMap<>();

        public BingoScoreBoard(BingoGame game){
            this.game = game;
            this.manager = Bukkit.getScoreboardManager();
        }
        public void show(Player player){
            Scoreboard board = manager.getNewScoreboard();
            Objective objective = board.registerNewObjective(
                    "bingo",
                        Criteria.DUMMY,
                    Texts.get("marcador.titulo")
            );
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            boards.put(player.getUniqueId(), board);
            objectives.put(player.getUniqueId(), objective);

            update(player, board, objective);
            player.setScoreboard(board);
        }
        public void update(Player player){
            Scoreboard board = boards.get(player.getUniqueId());
            Objective objective = objectives.get(player.getUniqueId());

            if (board == null || objective == null) return;

            update(player, board, objective);
        }
        // La tabla muestra a todos los equipos: cuando uno avanza, cambia la de todos.
        public void updateAll() {
            for (UUID id : boards.keySet()) {
                Player player = Bukkit.getPlayer(id);
                if (player != null) update(player);
            }
        }
        // Al terminar la partida: cada jugador vuelve al scoreboard principal del servidor.
        public void hideAll(){
            Scoreboard main = manager.getMainScoreboard();
            for (UUID id : boards.keySet()) {
                Player player = Bukkit.getPlayer(id);
                if (player != null) player.setScoreboard(main);
            }
            boards.clear();
            objectives.clear();
        }
        public void update(Player player, Scoreboard board, Objective objective){
            List<String> lines = lines(player);
            for (int i = 0; i < ENTRIES.length; i++) {
                String entry = ENTRIES[i];
                if (i >= lines.size()) {
                    board.resetScores(entry);
                    continue;
                }
                org.bukkit.scoreboard.Team line = board.getTeam(LINE_PREFIX + i);
                if (line == null) {
                    line = board.registerNewTeam(LINE_PREFIX + i);
                    line.addEntry(entry);
                }
                line.setPrefix(lines.get(i));
                objective.getScore(entry).setScore(lines.size() - i);
            }
            colorNames(board);
        }

        private List<String> lines(Player player) {
            List<String> lines = new ArrayList<>();
            lines.add("§7──────────");
            String own = game.teamInGame(player)
                    .map(team -> TeamColors.chat(team.color()) + (team.solo() ? Texts.get("marcador.solo") : team.name()))
                    .orElse(Texts.get("marcador.sin_equipo"));
            lines.add(Texts.get("marcador.equipo", "equipo", own));
            lines.add(Texts.get("marcador.casillas", "casillas", game.getProgress(player.getUniqueId())));
            lines.add(Texts.get("marcador.gana", "modo", game.getSettings().mode().description()));
            lines.add("");

            List<Map.Entry<Team, BingoCard>> ranking = new ArrayList<>(game.teamCards().entrySet());
            ranking.sort(Comparator.comparingInt((Map.Entry<Team, BingoCard> e) -> e.getValue().countCompleted()).reversed());
            for (Map.Entry<Team, BingoCard> entry : ranking.subList(0, Math.min(MAX_TEAMS, ranking.size()))) {
                Team team = entry.getKey();
                lines.add(Texts.get("marcador.fila_equipo", "equipo", TeamColors.chat(team.color()) + team.name(),
                        "casillas", entry.getValue().countCompleted()));
            }

            lines.add(Texts.get(game.inCountdown() ? "marcador.estado_cuenta" : "marcador.estado_juego"));
            return lines;
        }

        // Nombres sobre la cabeza y en la lista de jugadores con el color de su equipo.
        private void colorNames(Scoreboard board) {
            for (org.bukkit.scoreboard.Team tag : new ArrayList<>(board.getTeams())) {
                if (tag.getName().startsWith(TAG_PREFIX)) tag.unregister();
            }
            int index = 0;
            for (Team team : game.teamCards().keySet()) {
                org.bukkit.scoreboard.Team tag = board.registerNewTeam(TAG_PREFIX + index++);
                tag.setColor(TeamColors.chatColor(team.color()));
                for (UUID id : game.getTeams().members(team)) {
                    Player member = Bukkit.getPlayer(id);
                    if (member != null) tag.addEntry(member.getName());
                }
            }
        }
}
