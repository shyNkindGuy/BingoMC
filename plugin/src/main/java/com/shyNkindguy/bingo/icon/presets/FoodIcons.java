package com.shyNkindguy.bingo.icon.presets;

import com.shyNkindguy.bingo.ui.BingoPalette;

// Íconos 8x8; 0 = transparente.
public class FoodIcons {
    private static final byte o = BingoPalette.TRANSPARENT;
    private static final byte C = BingoPalette.MEAT;
    private static final byte P = BingoPalette.BREAD;
    private static final byte T = BingoPalette.DIRT;
    private static final byte V = BingoPalette.COMPLETED_BG;

    public static final byte[][] GENERIC_FOOD = {
            {o, o, o, o, o, o, o, o},
            {o, o, C, C, C, C, o, o},
            {o, C, C, C, C, C, C, o},
            {o, C, C, C, C, C, C, o},
            {o, C, C, C, C, C, C, o},
            {o, o, C, C, C, C, o, o},
            {o, o, o, P, P, o, o, o},
            {o, o, o, o, o, o, o, o},
    };

    public static final byte[][] BREAD = {
            {o, o, o, o, o, o, o, o},
            {o, P, P, P, P, P, P, o},
            {P, P, T, P, T, P, P, P},
            {P, P, P, P, P, P, P, P},
            {P, P, P, P, P, P, P, P},
            {o, P, P, P, P, P, P, o},
            {o, o, o, o, o, o, o, o},
            {o, o, o, o, o, o, o, o},
    };

    public static final byte[][] BERRY = {
            {o, o, o, V, o, o, o, o},
            {o, o, V, V, V, o, o, o},
            {o, C, C, o, C, C, o, o},
            {C, C, C, C, C, C, C, o},
            {C, C, C, C, C, C, C, o},
            {o, C, C, o, C, C, o, o},
            {o, o, o, o, o, o, o, o},
            {o, o, o, o, o, o, o, o},
    };
}
