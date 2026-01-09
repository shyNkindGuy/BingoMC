package com.shyNkindguy.bingo.ui;

import com.shyNkindguy.bingo.game.BingoCard;
import com.shyNkindguy.bingo.game.BingoCell;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.game.MapIconRegistry;
import org.bukkit.entity.Player;
import org.bukkit.map.*;
import org.bukkit.map.MapView;

public class BingoMapRenderer extends MapRenderer {
    private final BingoGame game;
    private boolean rendered = false;

    public BingoMapRenderer(BingoGame game){
        super(false);
        this.game = game;
    }
    public void invalidate() {
        this.rendered = false;
    }
    private void drawIcon(MapCanvas canvas, int x, int y, byte[][] icon) {
        for (int i = 0; i < icon.length; i++) {
            for (int j = 0; j < icon[i].length; j++) {
                byte color = icon[i][j];
                if (color != 0) {
                    canvas.setPixel(x + j, y + i, color);
                }
            }
        }
    }
    private void drawIconScaled(MapCanvas canvas, int x, int y, byte[][] icon, int scale) {
        for (int iy = 0; iy < icon.length; iy++) {
            for (int ix = 0; ix < icon[iy].length; ix++) {
                byte color = icon[iy][ix];
                if (color == 0) continue;

                for (int sx = 0; sx < scale; sx++) {
                    for (int sy = 0; sy < scale; sy++) {
                        canvas.setPixel(
                                x + ix * scale + sx,
                                y + iy * scale + sy,
                                color
                        );
                    }
                }
            }
        }
    }
    @Override

    public void render(MapView view, MapCanvas canvas, Player player){
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                canvas.setPixel(x, y, MapPalette.PALE_BLUE);
            }
        }
        BingoCard card = game.getCard(player);
        if (card == null) return;

        canvas.drawText(6,6, MinecraftFont.Font, "BINGO HUMILDE");

        BingoCell[][] grid = card.getGrid();
        int cellSize = 22;
        int startX = 4;
        int startY = 15;

        for (int r= 0; r < 5; r++){
            for (int c=0; c < 5; c++){
                int x = startX + c * cellSize;
                int y = startY + r * cellSize;
                BingoCell cell = grid[r][c];

                byte bg = cell.isCompleted()
                        ? MapPalette.LIGHT_GREEN
                        : MapPalette.LIGHT_GRAY;
                byte border = MapPalette.GRAY_1;

                for (int dx = 1; dx < cellSize - 1; dx++) {
                    for (int dy = 1; dy < cellSize - 1; dy++) {
                        canvas.setPixel(x + dx, y + dy, bg);
                    }
                }
                for (int i = 0; i < cellSize; i++) {
                    canvas.setPixel(x + i, y, border);
                    canvas.setPixel(x + i, y + cellSize - 1, border);
                    canvas.setPixel(x, y + i, border);
                    canvas.setPixel(x + cellSize - 1, y + i, border);
                }

                byte[][] icon = MapIconRegistry.get(cell.getObjective().getMaterial());
                if (icon == null) {
                    icon = MapIconRegistry.fallback(cell.getObjective().getMaterial());
                }

                int scale = 3;
                int iconSize = icon.length * scale;
                int offset = (cellSize - iconSize) / 2;

                drawIconScaled(canvas, x + offset, y + offset, icon, scale);
//                String name = cell.getObjective().getMaterial().name();
//                String label = name.length() >= 2 ? name.substring(0,2): name;
//                canvas.drawText(x+4, y+6, MinecraftFont.Font, label);

                if (cell.isCompleted()) {
                    for (int i = 0; i < 6; i++) {
                        canvas.setPixel(x + i, y + i, MapPalette.DARK_GREEN);
                        canvas.setPixel(x + (cellSize - 3 - i), y + i, MapPalette.DARK_GREEN);
                    }
                }

            }
        }
    }

}
