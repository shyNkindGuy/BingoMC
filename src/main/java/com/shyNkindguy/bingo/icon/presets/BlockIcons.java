package com.shyNkindguy.bingo.icon.presets;

import com.shyNkindguy.bingo.ui.BingoPalette;

// Íconos 8x8; 0 = transparente.
public class BlockIcons {
    private static final byte o = BingoPalette.TRANSPARENT;
    private static final byte B = BingoPalette.BORDER;
    private static final byte P = BingoPalette.STONE;
    private static final byte D = BingoPalette.DIAMOND;
    private static final byte H = BingoPalette.IRON;
    private static final byte V = BingoPalette.COMPLETED_BG;
    private static final byte T = BingoPalette.DIRT;

    public static final byte[][] GENERIC_BLOCK = {
            {B, B, B, B, B, B, B, B},
            {B, P, P, P, P, P, P, B},
            {B, P, P, P, P, P, P, B},
            {B, P, P, P, P, P, P, B},
            {B, P, P, P, P, P, P, B},
            {B, P, P, P, P, P, P, B},
            {B, P, P, P, P, P, P, B},
            {B, B, B, B, B, B, B, B},
    };

    public static final byte[][] GENERIC_ORE = {
            {B, B, B, B, B, B, B, B},
            {B, P, P, P, P, P, P, B},
            {B, P, D, D, P, P, P, B},
            {B, P, D, P, P, D, P, B},
            {B, P, P, P, D, D, P, B},
            {B, P, D, P, P, P, P, B},
            {B, P, P, P, P, P, P, B},
            {B, B, B, B, B, B, B, B},
    };

    public static final byte[][] DIAMOND_GEM = {
            {o, o, D, D, D, D, o, o},
            {o, D, H, D, D, D, D, o},
            {D, H, D, D, D, D, D, D},
            {o, D, D, D, D, D, D, o},
            {o, o, D, D, D, D, o, o},
            {o, o, o, D, D, o, o, o},
            {o, o, o, o, o, o, o, o},
            {o, o, o, o, o, o, o, o},
    };

    public static final byte[][] INGOT = {
            {o, o, o, o, o, o, o, o},
            {o, o, o, o, o, o, o, o},
            {o, o, B, B, B, B, B, o},
            {o, B, H, H, H, H, B, o},
            {B, H, H, H, H, B, o, o},
            {B, B, B, B, B, o, o, o},
            {o, o, o, o, o, o, o, o},
            {o, o, o, o, o, o, o, o},
    };

    public static final byte[][] SAPLING = {
            {o, o, o, V, V, o, o, o},
            {o, V, V, V, V, V, o, o},
            {o, o, V, V, V, V, V, o},
            {o, V, V, T, V, V, o, o},
            {o, o, o, T, o, o, o, o},
            {o, o, o, T, o, o, o, o},
            {o, o, T, T, T, o, o, o},
            {o, o, o, o, o, o, o, o},
    };
}
