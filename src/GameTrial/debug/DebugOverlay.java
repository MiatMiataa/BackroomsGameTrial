package GameTrial.debug;

import GameTrial.entity.Player;
import GameTrial.world.GameMap;
import java.awt.Color;
import java.awt.Graphics2D;

public class DebugOverlay {
    private static final boolean SHOW_ROUTE = true;
    private static final int MINIMAP_VISIBLE_TILES = 30;

    private static final int MINIMAP_BOX_SIZE = 150;
    private static final int MINIMAP_PADDING = 10;

    private static final Color PATH_COLOR = new Color(80, 120, 255);

    private static final Color WALL_COLOR = Color.DARK_GRAY;
    private static final Color FLOOR_COLOR = Color.LIGHT_GRAY;
    private static final Color EXIT_COLOR = Color.GREEN;
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 180);

    private final Player player;
    private final GameMap gameMap;

    public DebugOverlay(Player player, GameMap gameMap) {
        this.player = player;
        this.gameMap = gameMap;
    }

    public void drawMinimapOverlay(Graphics2D graphics) {
        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(MINIMAP_PADDING, MINIMAP_PADDING, MINIMAP_BOX_SIZE, MINIMAP_BOX_SIZE);

        drawTiles(graphics);

        int centerX = MINIMAP_PADDING + MINIMAP_BOX_SIZE / 2;
        int centerY = MINIMAP_PADDING + MINIMAP_BOX_SIZE / 2;

        graphics.setColor(Color.RED);
        graphics.fillOval(centerX - 3, centerY - 3, 6, 6);

        graphics.setColor(Color.YELLOW);
        int directionLineLength = 15;
        graphics.drawLine(centerX, centerY,
            centerX + (int) (player.directionX * directionLineLength),
            centerY + (int) (player.directionY * directionLineLength));
    }

    private void drawTiles(Graphics2D graphics) {
        int playerTileColumn = (int) Math.floor(player.positionX / GameMap.TILE_SIZE);
        int playerTileRow = (int) Math.floor(player.positionY / GameMap.TILE_SIZE);
        int halfVisible = MINIMAP_VISIBLE_TILES / 2;
        double pixelsPerTile = (double) MINIMAP_BOX_SIZE / MINIMAP_VISIBLE_TILES;
        int tilePixelSize = (int) Math.ceil(pixelsPerTile);

        for (int offsetRow = 0; offsetRow < MINIMAP_VISIBLE_TILES; offsetRow++) {
            for (int offsetColumn = 0; offsetColumn < MINIMAP_VISIBLE_TILES; offsetColumn++) {
                int column = playerTileColumn + offsetColumn - halfVisible;
                int row = playerTileRow + offsetRow - halfVisible;

                if (column == gameMap.exitTileColumn && row == gameMap.exitTileRow) {
                    graphics.setColor(EXIT_COLOR);
                } else if (SHOW_ROUTE && gameMap.isOnPathToExit(column, row)) {
                    graphics.setColor(PATH_COLOR);
                } else if (gameMap.getTile(column, row) == 1) {
                    graphics.setColor(WALL_COLOR);
                } else {
                    graphics.setColor(FLOOR_COLOR);
                }

                graphics.fillRect(
                    MINIMAP_PADDING + (int) (offsetColumn * pixelsPerTile),
                    MINIMAP_PADDING + (int) (offsetRow * pixelsPerTile),
                    tilePixelSize,
                    tilePixelSize);
            }
        }
    }
}