package com.shyNkindguy.bingo.icon;

import com.shyNkindguy.bingo.ui.BingoPalette;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MinecraftFont;

public class IconRenderer {
    public static void drawInCell(MapCanvas canvas, int cellX, int cellY,
                                  int cellSize, IconDefinition icon, boolean completed) {

        int iconW = icon.getWidth();
        int iconH = icon.getHeight();

        // Centrar
        int offsetX = (cellSize - iconW) / 2;
        int offsetY = (cellSize - iconH) / 2;

        // Dibujar píxeles
        for (int y = 0; y < iconH; y++) {
            for (int x = 0; x < iconW; x++) {
                byte color = icon.getPixel(x, y);
                if (color != BingoPalette.TRANSPARENT) {
                    canvas.setPixel(
                            cellX + offsetX + x,
                            cellY + offsetY + y,
                            completed ? tintCompleted(color) : color
                    );
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

    // Dibujar icono compuesto (OR condition: 2 materiales)
    public static void drawComposite(MapCanvas canvas, int cellX, int cellY,
                                     int cellSize, IconDefinition icon1, IconDefinition icon2) {
        // Dividir celda en mitad izquierda/derecha
        int halfSize = cellSize / 2;

        // Escalar iconos a mitad
        drawScaled(canvas, cellX, cellY, icon1, 0.5);
        drawScaled(canvas, cellX + halfSize, cellY, icon2, 0.5);
    }

    private static void drawScaled(MapCanvas canvas, int x, int y,
                                   IconDefinition icon, double scale) {
        // Implementar escalado bilineal o nearest-neighbor
    }
}
