package com.shyNkindguy.bingo.game;

import org.bukkit.map.MapPalette;

    public class IconPresets {

        public static final byte[][] UNKNOWN = {
                {0,0,0,0,0,0,0,0},
                {0,12,12,0,0,12,12,0},
                {0,12,12,0,0,12,12,0},
                {0,0,0,0,0,0,0,0},
                {0,12,12,0,0,12,12,0},
                {0,12,12,0,0,12,12,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
        };

        public static final byte[][] LOG = fill(MapPalette.BROWN);

        public static final byte[][] INGOT = fill(MapPalette.LIGHT_GRAY);

        public static final byte[][] BLOCK = fill(MapPalette.GRAY_2);

        public static final byte[][] FOOD = fill(MapPalette.RED);

        public static final byte[][] BUCKET = {
                {0,0,15,15,15,15,0,0},
                {0,15,15,15,15,15,15,0},
                {15,15,0,0,0,0,15,15},
                {15,15,0,0,0,0,15,15},
                {15,15,0,0,0,0,15,15},
                {15,15,15,15,15,15,15,15},
                {0,15,15,15,15,15,15,0},
                {0,0,15,15,15,15,0,0},
        };

        public static final byte[][] DIAMOND = {
                {0,0,9,9,9,9,0,0},
                {0,9,12,12,12,12,9,0},
                {9,12,12,12,12,12,12,9},
                {9,12,12,12,12,12,12,9},
                {0,9,12,12,12,12,9,0},
                {0,0,9,9,9,9,0,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
        };

        public static final byte[][] BREAD = {
                {0,0,0,0,0,0,0,0},
                {0,14,14,14,14,14,14,0},
                {14,14,14,14,14,14,14,14},
                {14,14,14,14,14,14,14,14},
                {14,14,14,14,14,14,14,14},
                {0,14,14,14,14,14,14,0},
                {0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0},
        };

        private static byte[][] fill(byte color) {
            byte[][] icon = new byte[8][8];
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    icon[y][x] = color;
                }
            }
            return icon;
        }
    }
    