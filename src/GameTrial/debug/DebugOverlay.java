package GameTrial.debug;

import GameTrial.entity.Player;
import GameTrial.render.RayHit;
import GameTrial.render.Raycaster;
import GameTrial.world.GameMap;
import java.awt.Color;
import java.awt.Graphics2D;

public class DebugOverlay {
    private Player player;
    private GameMap gameMap;
    private Raycaster raycaster;

    public DebugOverlay(Player player, GameMap gameMap, Raycaster raycaster) {
        this.player = player;
        this.gameMap = gameMap;
        this.raycaster = raycaster;
    }
    
    private void drawMinimap(Graphics2D graphics) {
        int rowCount = gameMap.getRowsCount();
        int columnCount = gameMap.getColumnCount();
        int tileSize = GameMap.TILE_SIZE;

        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                if (gameMap.getTile(column, row) == 1) {
                    graphics.setColor(Color.darkGray);
                    graphics.fillRect(column * tileSize, row * tileSize, tileSize, tileSize);
                    graphics.setColor(Color.black);
                    graphics.drawRect(column * tileSize, row * tileSize, tileSize, tileSize);
                } else {
                    graphics.setColor(Color.lightGray);
                    graphics.fillRect(column * tileSize, row * tileSize, tileSize, tileSize);
                }
            }
        }

        graphics.setColor(Color.RED);
        graphics.fillOval((int) (player.positionX - player.collisionRadius), (int) (player.positionY - player.collisionRadius),
                player.collisionRadius * 2, player.collisionRadius * 2);

        graphics.setColor(Color.YELLOW);
        int facingLineLength = 30;
        graphics.drawLine((int) player.positionX, (int) player.positionY,
                (int) (player.positionX + player.directionX * facingLineLength),
                (int) (player.positionY + player.directionY * facingLineLength));

        drawDebugRay(graphics);
    }

    public void drawMinimapOverlay(Graphics2D graphics) {
        int minimapBoxSize = 150;
        int minimapPadding = 10;
        int rowCount = gameMap.getRowsCount();
        int columnCount = gameMap.getColumnCount();
        double minimapScale = (double) minimapBoxSize / (Math.max(rowCount, columnCount) * GameMap.TILE_SIZE);

        graphics.setColor(new Color(0, 0, 0, 180));
        graphics.fillRect(minimapPadding, minimapPadding, minimapBoxSize, minimapBoxSize);

        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) {
                int tilePixelX = minimapPadding + (int) (column * GameMap.TILE_SIZE * minimapScale);
                int tilePixelY = minimapPadding + (int) (row * GameMap.TILE_SIZE * minimapScale);
                int tilePixelSize = (int) (GameMap.TILE_SIZE * minimapScale) + 1;
                graphics.setColor(gameMap.getTile(column, row) == 1 ? Color.DARK_GRAY : Color.LIGHT_GRAY);
                graphics.fillRect(tilePixelX, tilePixelY, tilePixelSize, tilePixelSize);
            }
        }

        int playerMinimapX = minimapPadding + (int) (player.positionX * minimapScale);
        int playerMinimapY = minimapPadding + (int) (player.positionY * minimapScale);
        graphics.setColor(Color.RED);
        graphics.fillOval(playerMinimapX - 3, playerMinimapY - 3, 6, 6);

        graphics.setColor(Color.YELLOW);
        int directionLineLength = 15;
        graphics.drawLine(playerMinimapX, playerMinimapY,
                playerMinimapX + (int) (player.directionX * directionLineLength),
                playerMinimapY + (int) (player.directionY * directionLineLength));
    }

    private void drawDebugRay(Graphics2D graphics) {
        RayHit rayHitResult = raycaster.castRay(player.positionX, player.positionY, player.directionX, player.directionY);

        double wallHitPixelX = player.positionX + player.directionX * rayHitResult.perpendicularWallDistance;
        double wallHitPixelY = player.positionY + player.directionY * rayHitResult.perpendicularWallDistance;

        graphics.setColor(Color.GREEN);
        graphics.drawLine((int) player.positionX, (int) player.positionY, (int) wallHitPixelX, (int) wallHitPixelY);

        graphics.setColor(Color.CYAN);
        graphics.drawRect(rayHitResult.hitTileColumn * GameMap.TILE_SIZE, rayHitResult.hitTileRow * GameMap.TILE_SIZE,
                GameMap.TILE_SIZE, GameMap.TILE_SIZE);
    }
}
