package com.shyNkindguy.bingo.icon.presets;

import com.shyNkindguy.bingo.ui.BingoPalette;

// Íconos 8x8; 0 = transparente.
public class TooLIcons {
    private static final byte o = BingoPalette.TRANSPARENT;
    private static final byte M = BingoPalette.WOOD;
    private static final byte H = BingoPalette.IRON;

    public static final byte[][] PICKAXE = {
            {o, H, H, H, H, H, o, o},
            {H, o, o, M, o, o, H, o},
            {o, o, o, M, o, o, o, o},
            {o, o, o, M, o, o, o, o},
            {o, o, o, M, o, o, o, o},
            {o, o, o, M, o, o, o, o},
            {o, o, o, M, o, o, o, o},
            {o, o, o, o, o, o, o, o},
    };

    public static final byte[][] GENERIC_TOOL = {
            {o, o, o, o, o, H, H, o},
            {o, o, o, o, H, H, H, o},
            {o, o, o, o, o, M, H, o},
            {o, o, o, o, M, o, o, o},
            {o, o, o, M, o, o, o, o},
            {o, o, M, o, o, o, o, o},
            {o, M, o, o, o, o, o, o},
            {o, o, o, o, o, o, o, o},
    };

    public static final byte[][] GENERIC_WEAPON = {
            {o, o, o, o, o, o, H, H},
            {o, o, o, o, o, H, H, H},
            {o, o, o, o, H, H, H, o},
            {o, o, o, H, H, H, o, o},
            {o, M, H, H, H, o, o, o},
            {o, o, M, H, o, o, o, o},
            {o, M, o, M, o, o, o, o},
            {M, o, o, o, o, o, o, o},
    };
}
