package com.shyNkindguy.bingo.ui;

import com.shyNkindguy.bingo.core.card.BingoCard;
import com.shyNkindguy.bingo.core.card.BingoCell;
import com.shyNkindguy.bingo.core.progress.TaskProgress;
import com.shyNkindguy.bingo.core.task.Task;
import com.shyNkindguy.bingo.game.BingoGame;
import com.shyNkindguy.bingo.icon.IconDefinition;
import com.shyNkindguy.bingo.icon.IconRegistry;
import com.shyNkindguy.bingo.icon.IconRenderer;
import com.shyNkindguy.bingo.icon.TaskIcons;
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
    private static final int MAP_SIZE = 128;
    private static final int GRID_TOP = 15;   // debajo del título "BINGO"
    private static final int MAX_CELL = 36;

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
        int size = card.getSize();
        // casillas cuadradas lo más grandes posible (22 px en 5×5), grilla centrada a lo ancho
        int cellSize = Math.min(MAX_CELL, (MAP_SIZE - GRID_TOP - 3) / size);
        int startX = (MAP_SIZE - size * cellSize) / 2;
        int startY = GRID_TOP;

        for (int r= 0; r < size; r++){
            for (int c=0; c < size; c++){
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

                Task task = cell.getTask();
                Material material = TaskIcons.material(task);
                Material second = TaskIcons.secondMaterial(task);
                IconDefinition badge = TaskIcons.badge(task);
                if (material != null && second != null) {
                    IconRenderer.drawComposite(canvas, x, y, cellSize, iconOf(material), iconOf(second));
                } else if (material != null) {
                    IconRenderer.drawInCell(canvas, x, y, cellSize, iconOf(material), cell.isCompleted());
                } else if (badge != null) {
                    // sin ícono propio: la marca del tipo en grande
                    IconRenderer.drawInCell(canvas, x, y, cellSize, badge, cell.isCompleted());
                    badge = null;
                }
                if (badge != null && !cell.isCompleted()) {
                    IconRenderer.drawScaled(canvas::setPixel, x + 2, y + 2, badge, 1, false);
                }
                if (TaskIcons.hasConditions(task) && !cell.isCompleted()) {
                    // punto morado abajo a la izquierda: solo cuenta en cierto lugar, hora o sin morir
                    for (int dx = 0; dx < 3; dx++) {
                        for (int dy = 0; dy < 3; dy++) {
                            canvas.setPixel(x + 2 + dx, y + cellSize - 7 + dy, BingoPalette.CONDITION_MARK);
                        }
                    }
                }

                TaskProgress progress = cell.getProgress();
                if (!cell.isCompleted() && progress.target() > 1) {
                    drawAmount(canvas, x, y, cellSize, progress);
                }

                if (cell.isCompleted()) {
                    for (int i = 0; i < 6; i++) {
                        canvas.setPixel(x + i, y + i, MapPalette.DARK_GREEN);
                        canvas.setPixel(x + (cellSize - 3 - i), y + i, MapPalette.DARK_GREEN);
                    }
                }

            }
        }
    }

    private static IconDefinition iconOf(Material material) {
        IconDefinition icon = IconRegistry.get(material);
        return icon != null ? icon : IconRegistry.getFallback(material);
    }

    // Tareas de varias unidades: la cantidad pedida arriba a la derecha y una barra de avance abajo.
    private void drawAmount(MapCanvas canvas, int x, int y, int cellSize, TaskProgress progress) {
        String amount = String.valueOf(progress.target());
        int textX = x + cellSize - 2 - MinecraftFont.Font.getWidth(amount);
        canvas.drawText(textX, y + 2, MinecraftFont.Font, amount);

        int barX = x + 2;
        int barY = y + cellSize - 3;
        int barWidth = cellSize - 4;
        int filled = progress.scaledTo(barWidth);
        for (int i = 0; i < barWidth; i++) {
            canvas.setPixel(barX + i, barY, i < filled ? MapPalette.DARK_GREEN : MapPalette.GRAY_1);
        }
    }

}
