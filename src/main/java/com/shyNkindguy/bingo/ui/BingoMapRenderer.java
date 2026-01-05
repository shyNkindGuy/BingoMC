package com.shyNkindguy.bingo.ui;

import com.shyNkindguy.bingo.game.BingoCard;
import com.shyNkindguy.bingo.game.BingoCell;
import com.shyNkindguy.bingo.game.BingoGame;
import org.bukkit.entity.Player;
import org.bukkit.map.*;
import org.bukkit.map.MapView;

public class BingoMapRenderer extends MapRenderer {
    private final BingoGame game;

    public BingoMapRenderer(BingoGame game){
        super(false);
        this.game = game;
    }
    @Override
    public void render(MapView view, MapCanvas canvas, Player player){
        BingoCard card = game.getCard(player);
        if(card == null) return;
        canvas.drawText(5,5, MinecraftFont.Font, "BINGO");
        BingoCell[][] grid = card.getGrid();
        int cellSize = 22;
        int startX = 4;
        int startY = 15;

        for (int r= 0; r < 5; r++){
            for (int c=0; c < 5; c++){
                int x = startX + c * cellSize;
                int y = startY + r * cellSize;
                BingoCell cell = grid[r][c];

                byte color = cell.isCompleted()
                        ? MapPalette.LIGHT_GREEN
                        : MapPalette.GRAY_2;
                for (int dx = 0; dx < cellSize - 2; dx++){
                    for (int dy = 0; dy < cellSize - 2; dy++){
                        canvas.setPixel(x + dx, y + dy, color);
                    }
                }
                String name = cell.getObjective().getMaterial().name();
                String label = name.length() >= 2 ? name.substring(0,2): name;
                canvas.drawText(x+4, y+6, MinecraftFont.Font, label);

                if (cell.isCompleted()) {
                    for (int i = 0; i < 6; i++) {
                        canvas.setPixel(x + i, y + i, MapPalette.DARK_GREEN);
                        canvas.setPixel(x + 5 - i, y + i, MapPalette.DARK_GREEN);
                    }
                }

            }
        }
    }
}
