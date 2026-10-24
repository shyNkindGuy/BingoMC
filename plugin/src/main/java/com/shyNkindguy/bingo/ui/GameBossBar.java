package com.shyNkindguy.bingo.ui;

import com.shyNkindguy.bingo.game.BingoGame;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.List;

// Una barra arriba de la pantalla con la cuenta regresiva, el tiempo que queda (se pone roja en el último
// minuto) o, sin límite, cuánto va. BingoGame la actualiza cada segundo.
public class GameBossBar {
    private static final int LAST_MINUTE = 60;

    private final BingoGame game;
    private BossBar bar;
    private int countdownStart;

    public GameBossBar(BingoGame game) {
        this.game = game;
    }

    public void update() {
        if (!game.inGame()) {
            hide();
            return;
        }
        if (bar == null) {
            bar = Bukkit.createBossBar("", BarColor.YELLOW, BarStyle.SOLID);
            countdownStart = Math.max(1, game.countdownLeft());
        }
        List<Player> players = game.playersInGame();
        for (Player player : players) {
            if (!bar.getPlayers().contains(player)) bar.addPlayer(player);
        }

        String mode = game.getSettings().mode().description();
        int duration = game.durationSeconds();
        if (game.inCountdown()) {
            bar.setColor(BarColor.YELLOW);
            bar.setTitle(Texts.get("barra.cuenta", "segundos", game.countdownLeft()));
            bar.setProgress(clamp((double) game.countdownLeft() / countdownStart));
        } else if (duration > 0) {
            int left = Math.max(0, duration - game.elapsedSeconds());
            bar.setColor(left <= LAST_MINUTE ? BarColor.RED : BarColor.GREEN);
            bar.setTitle(Texts.get("barra.restante", "tiempo", clock(left), "modo", mode));
            bar.setProgress(clamp((double) left / duration));
        } else {
            bar.setColor(BarColor.BLUE);
            bar.setTitle(Texts.get("barra.sin_limite", "tiempo", clock(game.elapsedSeconds()), "modo", mode));
            bar.setProgress(1.0);
        }
    }

    public void hide() {
        if (bar != null) {
            bar.removeAll();
            bar = null;
        }
    }

    // 754 → "12:34"; con horas, "1:02:03".
    static String clock(int seconds) {
        int hours = seconds / 3600;
        int minutes = seconds % 3600 / 60;
        int secs = seconds % 60;
        return hours > 0 ? String.format("%d:%02d:%02d", hours, minutes, secs) : String.format("%d:%02d", minutes, secs);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
