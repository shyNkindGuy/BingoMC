package com.shyNkindguy.bingo.ui;

import com.shyNkindguy.bingo.game.BingoCard;
import com.shyNkindguy.bingo.game.BingoCell;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.icon.IconDefinition;
import com.shyNkindguy.bingo.icon.IconRegistry;
import com.shyNkindguy.bingo.icon.IconRenderer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.map.*;
import org.bukkit.map.MapView;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// Contextual: cada jugador tiene su propio lienzo y ve SU cartilla. Solo se redibuja cuando la cartilla
// cambia (invalidate); el resto de los ticks el lienzo conserva lo último que se pintó.
public class BingoMapRenderer extends MapRenderer {
    private final BingoGame game;
    private final Set<UUID> upToDate = new HashSet<>();

    public BingoMapRenderer(BingoGame game){
        super(true);
        this.game = game;
    }
    public void invalidate() {
        upToDate.clear();
    }
    @Override

    public void render(MapView view, MapCanvas canvas, Player player){
        if (!upToDate.add(player.getUniqueId())) return;

        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                canvas.setPixel(x, y, MapPalette.PALE_BLUE);
            }
        }
        BingoCard card = game.getCard(player);
        if (card == null) return;

        canvas.drawText(6,6, MinecraftFont.Font, "BINGO");

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

                Material material = cell.getObjective().getMaterial();
                IconDefinition icon = IconRegistry.get(material);
                if (icon == null) {
                    icon = IconRegistry.getFallback(material);
                }
                IconRenderer.drawInCell(canvas, x, y, cellSize, icon, cell.isCompleted());

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
