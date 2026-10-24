package com.shyNkindguy.bingo.icon;

import com.shyNkindguy.bingo.ui.BingoPalette;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MinecraftFont;

public class IconRenderer {
    // Margen libre (en píxeles) entre el ícono y el borde de la casilla.
    private static final int CELL_MARGIN = 2;

    // Dónde van los píxeles: el lienzo del mapa en el juego, una lista en los tests.
    @FunctionalInterface
    public interface PixelSink {
        void setPixel(int x, int y, byte color);
    }

    public static void drawInCell(MapCanvas canvas, int cellX, int cellY,
                                  int cellSize, IconDefinition icon, boolean completed) {
        drawInCell(canvas::setPixel, cellX, cellY, cellSize, icon, completed);
    }

    // Ícono escalado al tamaño entero más grande que entra en la casilla, y centrado.
    public static void drawInCell(PixelSink sink, int cellX, int cellY,
                                  int cellSize, IconDefinition icon, boolean completed) {
        int scale = fitScale(cellSize - 2 * CELL_MARGIN, icon.getWidth(), icon.getHeight());

        // Centrar
        int offsetX = (cellSize - icon.getWidth() * scale) / 2;
        int offsetY = (cellSize - icon.getHeight() * scale) / 2;

        drawScaled(sink, cellX + offsetX, cellY + offsetY, icon, scale, completed);
    }

    // Mayor escala entera con la que el ícono entra en `space` píxeles (nunca menos de 1).
    public static int fitScale(int space, int iconWidth, int iconHeight) {
        return Math.max(1, Math.min(space / iconWidth, space / iconHeight));
    }

    // Escalado nearest-neighbor: cada píxel del ícono se pinta como un cuadrado de scale×scale.
    public static void drawScaled(PixelSink sink, int x, int y,
                                  IconDefinition icon, int scale, boolean completed) {
        for (int iy = 0; iy < icon.getHeight(); iy++) {
            for (int ix = 0; ix < icon.getWidth(); ix++) {
                byte color = icon.getPixel(ix, iy);
                if (color == BingoPalette.TRANSPARENT) continue;

                byte painted = completed ? tintCompleted(color) : color;
                for (int sy = 0; sy < scale; sy++) {
                    for (int sx = 0; sx < scale; sx++) {
                        sink.setPixel(x + ix * scale + sx, y + iy * scale + sy, painted);
                    }
                }
            }
        }
    }

    // Aplicar tinte verde a iconos completados
    private static byte tintCompleted(byte color) {
        // Opcional: oscurecer o verdecer colores
        return color;
    }

    // Dibujar contador de cantidad (ej. "6x")
    public static void drawQuantity(MapCanvas canvas, int x, int y, int quantity) {
        String text = quantity + "x";
        canvas.drawText(x, y, MinecraftFont.Font, text);
    }

    // Dibujar icono compuesto (OR condition: 2 materiales): cada uno centrado en su mitad de la casilla
    public static void drawComposite(MapCanvas canvas, int cellX, int cellY,
                                     int cellSize, IconDefinition icon1, IconDefinition icon2) {
        int halfSize = cellSize / 2;
        int scale = fitScale(halfSize - CELL_MARGIN,
                Math.max(icon1.getWidth(), icon2.getWidth()),
                Math.max(icon1.getHeight(), icon2.getHeight()));

        drawScaled(canvas::setPixel,
                cellX + (halfSize - icon1.getWidth() * scale) / 2,
                cellY + (cellSize - icon1.getHeight() * scale) / 2, icon1, scale, false);
        drawScaled(canvas::setPixel,
                cellX + halfSize + (halfSize - icon2.getWidth() * scale) / 2,
                cellY + (cellSize - icon2.getHeight() * scale) / 2, icon2, scale, false);
    }
}
