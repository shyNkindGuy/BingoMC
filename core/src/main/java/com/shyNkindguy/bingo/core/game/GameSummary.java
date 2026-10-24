package com.shyNkindguy.bingo.core.game;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// El resumen del final: cada equipo con sus casillas (en el orden en que las completó, con el segundo y
// quién) de más a menos, y el MVP, el jugador que más casillas completó (si empatan, el que llegó antes).
public record GameSummary(List<TeamResult> teams, String mvp, int mvpCells) {

    public record TeamResult(String team, int completed, int lastCompletedAt, List<Completion> cells) {
    }

    public record Completion(String task, int second, String by) {
    }

    // `cardsByTeam`: nombre del equipo → su cartilla, en el orden en que se quiera mostrar si empatan.
    public static GameSummary of(Map<String, BingoCard> cardsByTeam) {
        List<TeamResult> teams = new ArrayList<>();
        Map<String, int[]> byPlayer = new HashMap<>(); // jugador → {casillas, último segundo}
        for (Map.Entry<String, BingoCard> entry : cardsByTeam.entrySet()) {
            List<Completion> cells = new ArrayList<>();
            int last = -1;
            for (BingoCell[] row : entry.getValue().getGrid()) {
                for (BingoCell cell : row) {
                    if (!cell.isCompleted()) continue;
                    cells.add(new Completion(cell.getTask().displayName(), cell.completedAt(), cell.completedBy()));
                    last = Math.max(last, cell.completedAt());
                    if (cell.completedBy() != null) {
                        int[] score = byPlayer.computeIfAbsent(cell.completedBy(), name -> new int[]{0, -1});
                        score[0]++;
                        score[1] = Math.max(score[1], cell.completedAt());
                    }
                }
            }
            cells.sort(Comparator.comparingInt(Completion::second));
            teams.add(new TeamResult(entry.getKey(), cells.size(), last, cells));
        }
        teams.sort(Comparator.comparingInt(TeamResult::completed).reversed()
                .thenComparingInt(TeamResult::lastCompletedAt));

        String mvp = null;
        int mvpCells = 0;
        int mvpLast = Integer.MAX_VALUE;
        for (Map.Entry<String, int[]> entry : byPlayer.entrySet()) {
            int cells = entry.getValue()[0];
            int last = entry.getValue()[1];
            if (cells > mvpCells || (cells == mvpCells && last < mvpLast)) {
                mvp = entry.getKey();
                mvpCells = cells;
                mvpLast = last;
            }
        }
        return new GameSummary(teams, mvp, mvpCells);
    }
}
