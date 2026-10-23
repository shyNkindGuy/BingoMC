package com.shyNkindguy.bingo.icon.presets;

import com.shyNkindguy.bingo.ui.BingoPalette;

// Íconos 8x8; 0 = transparente.
public class FluidIcons {
    private static final byte o = BingoPalette.TRANSPARENT;
    private static final byte H = BingoPalette.IRON;
    private static final byte B = BingoPalette.BORDER;
    private static final byte A = BingoPalette.WATER;
    private static final byte L = BingoPalette.LAVA;
    private static final byte K = BingoPalette.MILK;

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

    public static final byte[][] WATER_BUCKET = filled(A);
    public static final byte[][] LAVA_BUCKET = filled(L);
    public static final byte[][] MILK_BUCKET = filled(K);

    // El balde vacío con el interior (lo transparente entre las paredes, filas 2 a 4) del color del líquido.
    private static byte[][] filled(byte liquid) {
        byte[][] icon = new byte[BUCKET_EMPTY.length][];
        for (int y = 0; y < icon.length; y++) {
            icon[y] = BUCKET_EMPTY[y].clone();
        }
        for (int y = 2; y <= 4; y++) {
            int left = 0;
            while (icon[y][left] == o) left++;
            int right = icon[y].length - 1;
            while (icon[y][right] == o) right--;
            for (int x = left + 1; x < right; x++) {
                if (icon[y][x] == o) icon[y][x] = liquid;
            }
        }
        return icon;
    }
}
