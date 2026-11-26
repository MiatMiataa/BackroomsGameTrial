package fnaf.debug;

import fnaf.game.Game;
import fnaf.entity.AnimatronicManager;
import fnaf.entity.Player;
import fnaf.entity.Sprite;
import fnaf.render.Renderer;
import fnaf.world.GameMap;
import fnaf.world.SlidingBarrier;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public class DebugOverlay {
    private static final int MINIMAP_BOX_WIDTH = 300;
    private static final int MINIMAP_BOX_HEIGHT = 200;
    private static final int MINIMAP_PADDING = 10;

    private static final Color WALL_COLOR = Color.DARK_GRAY;
    private static final Color FLOOR_COLOR = Color.LIGHT_GRAY;
    private static final Color EXIT_COLOR = Color.GREEN;
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 180);
    private static final Color BLOCKED_SEGMENT_COLOR = Color.RED;
    private static final Color CAMERA_COLOR = new Color(220, 220, 220, 150);
    private static final Color ACTIVE_CAMERA_COLOR = Color.YELLOW;

    private int minimapOriginX;
    private int minimapOriginY;

    private final Game game;
    private final Player player;
    private final GameMap gameMap;
    private final AnimatronicManager animatronicManager;

    public DebugOverlay(Game game) {
        this.game = game;
        this.player = game.getPlayer();
        this.gameMap = game.getGameMap();
        this.animatronicManager = game.getAnimatronicManager();
    }

    public void drawMinimap(Graphics2D graphics) {
        int totalColumns = gameMap.getColumnCount();
        int totalRows = gameMap.getRowCount();

        // Calculate how big each tile should be to fit the entire map in the box
        double horizontalScale = (double) MINIMAP_BOX_WIDTH / totalColumns;
        double verticalScale = (double) MINIMAP_BOX_HEIGHT / totalRows;
        double tileScale = Math.min(horizontalScale, verticalScale);

        int mapPixelWidth = (int) (totalColumns * tileScale);
        int mapPixelHeight = (int) (totalRows * tileScale);

        minimapOriginX = Renderer.RENDER_WIDTH - mapPixelWidth - MINIMAP_PADDING;
        minimapOriginY = Renderer.RENDER_HEIGHT - mapPixelHeight - MINIMAP_PADDING;

        // Draw the dark background box
        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(minimapOriginX, minimapOriginY, mapPixelWidth, mapPixelHeight);

        // 1. Draw the whole map grid
        for (int currentColumn = 0; currentColumn < totalColumns; currentColumn++) {
            for (int currentRow = 0; currentRow < totalRows; currentRow++) {
                int tileValue = gameMap.getTile(currentColumn, currentRow);

                if (tileValue == 7) {
                    graphics.setColor(EXIT_COLOR);
                } else if (tileValue == 1) {
                    graphics.setColor(WALL_COLOR);
                } else {
                    graphics.setColor(FLOOR_COLOR);
                }

                int drawPixelX = minimapOriginX + (int) (currentColumn * tileScale);
                int drawPixelY = minimapOriginY + (int) (currentRow * tileScale);

                graphics.fillRect(
                    drawPixelX,
                    drawPixelY,
                    (int) Math.ceil(tileScale),
                    (int) Math.ceil(tileScale)
                );
            }
        }

        // 2. Barriers: solid orange while closed, fading as they open
        for (SlidingBarrier barrier : gameMap.barriers()) {
            int barrierAlpha = (int) (220 * (1.0 - barrier.openAmount));
            if (barrierAlpha <= 0) {
                continue;
            }
            graphics.setColor(new Color(255, 150, 0, barrierAlpha));
            graphics.fillRect(
                minimapOriginX + (int) (barrier.column * tileScale),
                minimapOriginY + (int) (barrier.row * tileScale),
                (int) Math.ceil(tileScale),
                (int) Math.ceil(tileScale)
            );
        }

        // 3. Camera tiles: yellow for the camera on the tablet, grey for the rest
        drawCameraTiles(graphics, tileScale);

        // 4. Paths and animatronic dots are debug information, so they stay hidden
        //    until the player presses M.
        if (game.isShowingMinimapDebug()) {
            drawPaths(graphics, tileScale);
            drawAnimatronics(graphics, tileScale);
        }

        // 5. Draw the Player
        int playerColumn = (int) Math.floor(player.positionX / GameMap.TILE_SIZE);
        int playerRow = (int) Math.floor(player.positionY / GameMap.TILE_SIZE);

        int playerPixelX = minimapOriginX + (int) ((playerColumn + 0.5) * tileScale);
        int playerPixelY = minimapOriginY + (int) ((playerRow + 0.5) * tileScale);

        graphics.setColor(Color.CYAN);
        graphics.fillOval(playerPixelX - 4, playerPixelY - 4, 8, 8);
    }

    private void drawCameraTiles(Graphics2D graphics, double tileScale) {
        for (int cameraIndex = 0; cameraIndex < gameMap.cameraTiles.length; cameraIndex++) {
            int[] cameraTile = gameMap.cameraTiles[cameraIndex];
            boolean isCameraOnScreen = game.isTabletUp() && game.getCameraIndex() == cameraIndex;

            graphics.setColor(isCameraOnScreen ? ACTIVE_CAMERA_COLOR : CAMERA_COLOR);
            graphics.fillRect(
                minimapOriginX + (int) (cameraTile[0] * tileScale),
                minimapOriginY + (int) (cameraTile[1] * tileScale),
                (int) Math.ceil(tileScale),
                (int) Math.ceil(tileScale)
            );
        }
    }

    private void drawPaths(Graphics2D graphics, double tileScale) {
        // Draws the paths with different colors
        graphics.setColor(Color.MAGENTA); // Bonnie
        drawAnimatronicPath(graphics, gameMap.bonniePath, tileScale);
        graphics.setColor(Color.YELLOW); // Chica
        drawAnimatronicPath(graphics, gameMap.chicaPath, tileScale);
        graphics.setColor(new Color(139, 69, 19)); // Freddy (Brown)
        drawAnimatronicPath(graphics, gameMap.freddyPath, tileScale);
        graphics.setColor(Color.RED); // Foxy
        drawAnimatronicPath(graphics, gameMap.foxyPath, tileScale);
    }

    private void drawAnimatronics(Graphics2D graphics, double tileScale) {
        List<Sprite> animatronics = animatronicManager.sprites();
        for (Sprite animatronic : animatronics) {
            int animatronicColumn = (int) Math.floor(animatronic.positionX / GameMap.TILE_SIZE);
            int animatronicRow = (int) Math.floor(animatronic.positionY / GameMap.TILE_SIZE);

            int animatronicPixelX = minimapOriginX + (int) ((animatronicColumn + 0.5) * tileScale);
            int animatronicPixelY = minimapOriginY + (int) ((animatronicRow + 0.5) * tileScale);

            // Match the class name to give each animatronic a specific color
            String animatronicClassName = animatronic.getClass().getSimpleName();
            if (animatronicClassName.contains("Bonnie")) {
                graphics.setColor(Color.MAGENTA);
            } else if (animatronicClassName.contains("Chica")) {
                graphics.setColor(Color.YELLOW);
            } else if (animatronicClassName.contains("Freddy")) {
                graphics.setColor(new Color(139, 69, 19)); // Brown
            } else if (animatronicClassName.contains("Foxy")) {
                graphics.setColor(Color.RED);
            } else {
                graphics.setColor(Color.WHITE); // Golden Freddy
            }

            graphics.fillOval(animatronicPixelX - 4, animatronicPixelY - 4, 8, 8);
        }
    }

    private void drawAnimatronicPath(Graphics2D graphics, int[][] pathWaypoints, double tileScale) {
        Color pathColor = graphics.getColor();

        // One line per segment, red when that segment would cross a wall.
        for (int waypointIndex = 0; waypointIndex < pathWaypoints.length - 1; waypointIndex++) {
            boolean segmentIsClear = gameMap.isPathSegmentClear(pathWaypoints[waypointIndex], pathWaypoints[waypointIndex + 1]);
            graphics.setColor(segmentIsClear ? pathColor : BLOCKED_SEGMENT_COLOR);
            graphics.drawLine(
                waypointPixelX(pathWaypoints[waypointIndex], tileScale),
                waypointPixelY(pathWaypoints[waypointIndex], tileScale),
                waypointPixelX(pathWaypoints[waypointIndex + 1], tileScale),
                waypointPixelY(pathWaypoints[waypointIndex + 1], tileScale));
        }

        // A dot on every waypoint, drawn at the centre of its tile.
        graphics.setColor(pathColor);
        for (int waypointIndex = 0; waypointIndex < pathWaypoints.length; waypointIndex++) {
            graphics.fillOval(
                waypointPixelX(pathWaypoints[waypointIndex], tileScale) - 2,
                waypointPixelY(pathWaypoints[waypointIndex], tileScale) - 2,
                4, 4);
        }
    }

    private int waypointPixelX(int[] waypoint, double tileScale) {
        return minimapOriginX + (int) ((waypoint[0] + 0.5) * tileScale);
    }

    private int waypointPixelY(int[] waypoint, double tileScale) {
        return minimapOriginY + (int) ((waypoint[1] + 0.5) * tileScale);
    }
}
