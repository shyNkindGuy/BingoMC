package com.shyNkindguy.bingo.icon.presets;

import com.shyNkindguy.bingo.ui.BingoPalette;
import org.bukkit.map.MapPalette;

// Marcas 5×5 del tipo de tarea, arriba a la izquierda de la casilla; o = transparente. Las tareas sin
// ícono propio (estadísticas sin `de` ni `icono`) muestran su marca en grande.
public class BadgeIcons {
    private static final byte o = BingoPalette.TRANSPARENT;
    private static final byte H = MapPalette.DARK_GRAY;
    private static final byte R = MapPalette.RED;
    private static final byte G = BingoPalette.BADGE_GOLD;
    private static final byte B = BingoPalette.BADGE_BLUE;

    // matar: espada
    public static final byte[][] KILL = {
            {o, o, o, o, H},
            {o, o, o, H, o},
            {R, o, H, o, o},
            {o, R, o, o, o},
            {R, o, R, o, o},
    };

    // logro: estrella
    public static final byte[][] ADVANCEMENT = {
            {o, o, G, o, o},
            {G, G, G, G, G},
            {o, G, G, G, o},
            {o, G, o, G, o},
            {G, o, o, o, G},
    };

    // estadística: barras
    public static final byte[][] STATISTIC = {
            {o, o, o, o, B},
            {o, o, B, o, B},
            {o, o, B, o, B},
            {B, o, B, o, B},
            {B, o, B, o, B},
    };
}
