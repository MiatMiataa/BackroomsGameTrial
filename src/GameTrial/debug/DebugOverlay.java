package GameTrial.debug;

import java.awt.image.BufferedImage;
import GameTrial.entity.Player;
import GameTrial.world.GameMap;
import java.awt.Color;
import java.awt.Graphics2D;

public class DebugOverlay {
    private Player player;
    private GameMap gameMap;
    private BufferedImage minimapImage;

    public DebugOverlay(Player player, GameMap gameMap) {
        this.player = player;
        this.gameMap = gameMap;
    }

    public void drawMinimapOverlay(Graphics2D graphics) {
        int minimapBoxSize = 150;
        int minimapPadding = 10;
        int rowCount = gameMap.getRowsCount();
        int columnCount = gameMap.getColumnCount();
        double minimapScale = (double) minimapBoxSize / (Math.max(rowCount, columnCount) * GameMap.TILE_SIZE);

        graphics.setColor(new Color(0, 0, 0, 180));
        graphics.fillRect(minimapPadding, minimapPadding, minimapBoxSize, minimapBoxSize);

        if (minimapImage == null) {
            minimapImage = buildMinimapImage(minimapBoxSize, minimapScale);
        }
        graphics.drawImage(minimapImage, minimapPadding, minimapPadding, null);

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

    private BufferedImage buildMinimapImage(int minimapBoxSize, double minimapScale) {
        BufferedImage image = new BufferedImage(minimapBoxSize, minimapBoxSize, BufferedImage.TYPE_INT_ARGB);
        int wallColor = Color.DARK_GRAY.getRGB();
        int floorColor = Color.LIGHT_GRAY.getRGB();
        for (int pixelY = 0; pixelY < minimapBoxSize; pixelY++) {
            for (int pixelX = 0; pixelX < minimapBoxSize; pixelX++) {
                int column = (int) (pixelX / minimapScale / GameMap.TILE_SIZE);
                int row = (int) (pixelY / minimapScale / GameMap.TILE_SIZE);
                image.setRGB(pixelX, pixelY, gameMap.getTile(column, row) == 1 ? wallColor : floorColor);
            }
        }
        return image;
    }

}
