package com.shyNkindguy.bingo.core.achievement;

import com.shyNkindguy.bingo.core.bus.AchievementUnlocked;
import com.shyNkindguy.bingo.core.bus.CellCompleted;
import com.shyNkindguy.bingo.core.bus.DomainEvents;
import com.shyNkindguy.bingo.core.bus.GameEnded;
import com.shyNkindguy.bingo.core.condition.TimeOfDay;
import com.shyNkindguy.bingo.core.event.EventContext;
import com.shyNkindguy.bingo.core.stats.PlayerStats;
import com.shyNkindguy.bingo.core.stats.StatsBook;
import com.shyNkindguy.bingo.core.win.WinMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AchievementTrackerTest {
    private static final WinMode LINE = new WinMode.Lines(1);
    private static final GameEnded.Participant ANA = new GameEnded.Participant("id-ana", "Ana", "rojo", 0);
    private static final GameEnded.Participant BETO = new GameEnded.Participant("id-beto", "Beto", "azul", 2);
    private static final EventContext NOCHE = new EventContext("minecraft:overworld", "minecraft:plains", 15000L, 64, 0);
    private static final EventContext DIA = new EventContext("minecraft:overworld", "minecraft:plains", 1000L, 64, 0);

    private final DomainEvents events = new DomainEvents();
    private final StatsBook book = new StatsBook(Map.of());
    private final List<String> unlocked = new ArrayList<>();
    private List<Achievement> achievements = List.of();

    private static Achievement game(String id, GameRule rule) {
        return new Achievement(id, id, "descripción de " + id, null, rule);
    }

    private static GameRule rule(Boolean won, int within, WinMode mode, int cells, boolean noDeath, int games, int wins, int total) {
        return new GameRule(won, within, mode, cells, noDeath, games, wins, total);
    }

    private static CellCompleted cell(GameEnded.Participant player, String task, int second, EventContext context) {
        return new CellCompleted(player.team(), player.id(), player.name(), task, task, second, context);
    }

    @BeforeEach
    void setUp() {
        book.subscribeTo(events);
        new AchievementTracker(() -> achievements, book, events).subscribe();
        events.subscribe(AchievementUnlocked.class, event -> unlocked.add(event.playerName() + ":" + event.achievementId()));
    }

    @Test
    void losDePartidaMiranGanarModoTiempoMuertesYCasillas() {
        achievements = List.of(
                game("primera", rule(null, 0, null, 0, false, 0, 0, 0)),
                game("victoria", rule(true, 0, null, 0, false, 0, 0, 0)),
                game("derrota", rule(false, 0, null, 0, false, 0, 0, 0)),
                game("rapida", rule(true, 600, null, 0, false, 0, 0, 0)),
                game("lockout", rule(true, 0, new WinMode.Lockout(), 0, false, 0, 0, 0)),
                game("intocable", rule(null, 0, null, 0, true, 0, 0, 0)),
                game("dos_casillas", rule(null, 0, null, 2, false, 0, 0, 0)));

        events.publish(cell(ANA, "a", 10, DIA));
        events.publish(cell(ANA, "b", 20, DIA));
        events.publish(cell(BETO, "c", 30, DIA));
        events.publish(new GameEnded(700, false, LINE, Set.of("rojo"), List.of(ANA, BETO)));

        assertEquals(List.of("Ana:primera", "Ana:victoria", "Ana:intocable", "Ana:dos_casillas",
                "Beto:primera", "Beto:derrota"), unlocked);
        assertEquals(Set.of("primera", "victoria", "intocable", "dos_casillas"), book.get("id-ana").orElseThrow().achievements());
    }

    @Test
    void unaVezPorJugadorYLosTotalesCuentanLaPartidaQueTermina() {
        achievements = List.of(game("dos_partidas", rule(null, 0, null, 0, false, 2, 0, 0)),
                game("tres_casillas", rule(null, 0, null, 0, false, 0, 0, 3)));

        events.publish(cell(ANA, "a", 10, DIA));
        events.publish(new GameEnded(100, false, LINE, Set.of(), List.of(ANA)));
        assertEquals(List.of(), unlocked);

        events.publish(cell(ANA, "a", 10, DIA));
        events.publish(cell(ANA, "b", 10, DIA));
        events.publish(new GameEnded(100, false, LINE, Set.of(), List.of(ANA)));
        events.publish(new GameEnded(100, false, LINE, Set.of(), List.of(ANA)));

        assertEquals(List.of("Ana:dos_partidas", "Ana:tres_casillas"), unlocked);
    }

    @Test
    void sinSaberLasMuertesNoEsIntocableYUnaDetenidaNoCuenta() {
        achievements = List.of(game("intocable", rule(null, 0, null, 0, true, 0, 0, 0)),
                game("una_casilla", rule(null, 0, null, 1, false, 0, 0, 0)));
        GameEnded.Participant desconectada = new GameEnded.Participant("id-ana", "Ana", "rojo", -1);

        events.publish(cell(ANA, "a", 10, DIA));
        events.publish(new GameEnded(100, true, LINE, Set.of(), List.of(ANA)));
        events.publish(new GameEnded(100, false, LINE, Set.of(), List.of(desconectada)));

        assertEquals(List.of(), unlocked);
    }

    @Test
    void losDeCasillaMiranTareaContextoYMinuto() {
        achievements = List.of(
                new Achievement("noche", "Noche", "de noche", null, new CellRule(null, new TimeOfDay(true), 0)),
                new Achievement("diamante", "Diamante", "diamante", null, new CellRule("item:diamond", null, 0)),
                new Achievement("rapido", "Rápido", "primer minuto", null, new CellRule(null, null, 60)));

        events.publish(cell(ANA, "item:dirt", 75, DIA));
        assertEquals(List.of(), unlocked);

        events.publish(cell(ANA, "item:diamond", 30, NOCHE));
        events.publish(cell(ANA, "item:stone", 40, NOCHE));

        assertEquals(List.of("Ana:noche", "Ana:diamante", "Ana:rapido"), unlocked);
        PlayerStats ana = book.get("id-ana").orElseThrow();
        assertEquals(0, ana.games());
        assertTrue(ana.hasAchievement("noche"));
    }

    @Test
    void losLogrosSeLeenEnCadaEvento() {
        events.publish(cell(ANA, "a", 10, DIA));
        achievements = List.of(new Achievement("cualquiera", "C", "c", null, new CellRule(null, null, 0)));
        events.publish(cell(ANA, "b", 10, DIA));
        assertEquals(List.of("Ana:cualquiera"), unlocked);
    }
}
