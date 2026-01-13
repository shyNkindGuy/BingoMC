package com.shyNkindguy.bingo.icon;

import com.shyNkindguy.bingo.ui.BingoPalette;

public class IconDefinition {
    private final String id;
    private final byte[][] pixels; // 16x16 o variable
    private final IconCategory category;
    private final int width, height;

    public IconDefinition(String id, byte[][] pixels, IconCategory category) {
        this.id = id;
        this.pixels = pixels;
        this.category = category;
        this.height = pixels.length;
        this.width = pixels[0].length;
    }

    public byte getPixel(int x, int y) {
        if (x < 0 || y < 0 || y >= height || x >= width)
            return BingoPalette.TRANSPARENT;
        return pixels[y][x];
    }
}
