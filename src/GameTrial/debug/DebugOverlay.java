package GameTrial.debug;

import GameTrial.render.Renderer;
import GameTrial.entity.Player;
import GameTrial.entity.Sprite;
import GameTrial.entity.AnimatronicManager;
import GameTrial.world.GameMap;
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

    private int minimapOriginX;
    private int minimapOriginY;

    private final Player player;
    private final GameMap gameMap;
    private final AnimatronicManager animatronicManager;

    public DebugOverlay(Player player, GameMap gameMap, AnimatronicManager animatronicManager) {
        this.player = player;
        this.gameMap = gameMap;
        this.animatronicManager = animatronicManager;
    }

    public void drawMinimapOverlay(Graphics2D graphicsContext) {
        int totalColumns = gameMap.getColumnCount();
        int totalRows = gameMap.getRowsCount();

        // Calculate how big each tile should be to fit the entire map in the box
        double horizontalScale = (double) MINIMAP_BOX_WIDTH / totalColumns;
        double verticalScale = (double) MINIMAP_BOX_HEIGHT / totalRows;
        double smallestScale = Math.min(horizontalScale, verticalScale);

        int mapPixelWidth = (int) (totalColumns * smallestScale);
        int mapPixelHeight = (int) (totalRows * smallestScale);

        minimapOriginX = Renderer.RENDER_WIDTH - mapPixelWidth - MINIMAP_PADDING;
        minimapOriginY = Renderer.RENDER_HEIGHT - mapPixelHeight - MINIMAP_PADDING;

        // Draw the dark background box
        graphicsContext.setColor(BACKGROUND_COLOR);
        graphicsContext.fillRect(minimapOriginX, minimapOriginY, mapPixelWidth, mapPixelHeight);

        // 1. Draw the whole map grid
        for (int currentColumn = 0; currentColumn < totalColumns; currentColumn++) {
            for (int currentRow = 0; currentRow < totalRows; currentRow++) {
                int tileValue = gameMap.getTile(currentColumn, currentRow);

                if (tileValue == 7) {
                    graphicsContext.setColor(EXIT_COLOR);
                } else if (tileValue == 1) {
                    graphicsContext.setColor(WALL_COLOR);
                } else {
                    graphicsContext.setColor(FLOOR_COLOR);
                }

                int drawPixelX = minimapOriginX + (int) (currentColumn * smallestScale);
                int drawPixelY = minimapOriginY + (int) (currentRow * smallestScale);

                graphicsContext.fillRect(
                    drawPixelX,
                    drawPixelY,
                    (int) Math.ceil(smallestScale),
                    (int) Math.ceil(smallestScale)
                );
            }
        }

         // 2. Draws the paths with different colors
        graphicsContext.setColor(Color.MAGENTA); // Bonnie
        drawAnimatronicPath(graphicsContext, gameMap.bonniePath, smallestScale);
        graphicsContext.setColor(Color.YELLOW); // Chica
        drawAnimatronicPath(graphicsContext, gameMap.chicaPath, smallestScale);
        graphicsContext.setColor(new Color(139, 69, 19)); // Freddy (Brown)
        drawAnimatronicPath(graphicsContext, gameMap.freddyPath, smallestScale);
        graphicsContext.setColor(Color.RED); // Foxy
        drawAnimatronicPath(graphicsContext, gameMap.foxyPath, smallestScale);

        // 3. Draw the Animatronics
        List<Sprite> animatronicSprites = animatronicManager.sprites();
        for (Sprite animatronicSprite : animatronicSprites) {
            int animatronicColumn = (int) Math.floor(animatronicSprite.positionX / GameMap.TILE_SIZE);
            int animatronicRow = (int) Math.floor(animatronicSprite.positionY / GameMap.TILE_SIZE);

            int animatronicPixelX = minimapOriginX + (int) ((animatronicColumn + 0.5) * smallestScale);
            int animatronicPixelY = minimapOriginY + (int) ((animatronicRow + 0.5) * smallestScale);

            // Match the class name to give each animatronic a specific color
            String animatronicClassName = animatronicSprite.getClass().getSimpleName();
            if (animatronicClassName.contains("Bonnie")) {
                graphicsContext.setColor(Color.MAGENTA);
            } else if (animatronicClassName.contains("Chica")) {
                graphicsContext.setColor(Color.YELLOW);
            } else if (animatronicClassName.contains("Freddy")) {
                graphicsContext.setColor(new Color(139, 69, 19)); // Brown
            } else if (animatronicClassName.contains("Foxy")) {
                graphicsContext.setColor(Color.RED);
            } else {
                graphicsContext.setColor(Color.WHITE); // Golden Freddy
            }

            graphicsContext.fillOval(animatronicPixelX - 4, animatronicPixelY - 4, 8, 8);
        }

        // 4. Draw the Player
        int playerColumn = (int) Math.floor(player.positionX / GameMap.TILE_SIZE);
        int playerRow = (int) Math.floor(player.positionY / GameMap.TILE_SIZE);

        int playerPixelX = minimapOriginX + (int) ((playerColumn + 0.5) * smallestScale);
        int playerPixelY = minimapOriginY + (int) ((playerRow + 0.5) * smallestScale);

        graphicsContext.setColor(Color.CYAN);
        graphicsContext.fillOval(playerPixelX - 4, playerPixelY - 4, 8, 8);
    }

    private void drawAnimatronicPath(Graphics2D graphicsContext, int[][] pathWaypoints, double tileScale) {
        Color pathColor = graphicsContext.getColor();

        // One line per segment, red when that segment would cross a wall.
        for (int pathIndex = 0; pathIndex < pathWaypoints.length - 1; pathIndex++) {
            boolean segmentIsClear = gameMap.isPathSegmentClear(pathWaypoints[pathIndex], pathWaypoints[pathIndex + 1]);
            graphicsContext.setColor(segmentIsClear ? pathColor : BLOCKED_SEGMENT_COLOR);
            graphicsContext.drawLine(
                waypointPixelX(pathWaypoints[pathIndex], tileScale),
                waypointPixelY(pathWaypoints[pathIndex], tileScale),
                waypointPixelX(pathWaypoints[pathIndex + 1], tileScale),
                waypointPixelY(pathWaypoints[pathIndex + 1], tileScale));
        }

        // A dot on every waypoint, drawn at the centre of its tile.
        graphicsContext.setColor(pathColor);
        for (int pathIndex = 0; pathIndex < pathWaypoints.length; pathIndex++) {
            graphicsContext.fillOval(
                waypointPixelX(pathWaypoints[pathIndex], tileScale) - 2,
                waypointPixelY(pathWaypoints[pathIndex], tileScale) - 2,
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
