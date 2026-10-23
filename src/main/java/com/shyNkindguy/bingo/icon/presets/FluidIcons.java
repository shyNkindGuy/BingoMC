package com.shyNkindguy.bingo.icon.presets;

import com.shyNkindguy.bingo.ui.BingoPalette;

// Íconos 8x8; 0 = transparente.
public class FluidIcons {
    private static final byte o = BingoPalette.TRANSPARENT;
    private static final byte H = BingoPalette.IRON;
    private static final byte B = BingoPalette.BORDER;

    public static final byte[][] BUCKET_EMPTY = {
            {o, o, o, o, o, o, o, o},
            {B, B, B, B, B, B, B, B},
            {B, H, o, o, o, o, H, B},
            {B, H, o, o, o, o, H, B},
            {o, B, H, o, o, H, B, o},
            {o, B, H, H, H, H, B, o},
            {o, o, B, B, B, B, o, o},
            {o, o, o, o, o, o, o, o},
    };
}
