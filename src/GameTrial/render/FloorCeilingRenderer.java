package GameTrial.render;

import GameTrial.assets.Texture;
import GameTrial.entity.Player;
import GameTrial.world.GameMap;

public class FloorCeilingRenderer {
    private static final int HORIZON_ROW_COLOR = 0xFF2A2410;

    private final ScreenBuffer screenBuffer;
    private final Texture floorTexture;
    private final Texture ceilingTexture;
    private final GameMap gameMap;
    private final Texture arrowTexture;

    public FloorCeilingRenderer(ScreenBuffer screenBuffer, Texture floorTexture, Texture ceilingTexture, 
                                GameMap gameMap, Texture arrowTexture) {
        this.screenBuffer = screenBuffer;
        this.floorTexture = floorTexture;
        this.ceilingTexture = ceilingTexture;
        this.gameMap = gameMap;
        this.arrowTexture = arrowTexture;   
    }

    public void render(Player player) {
        int renderWidth = screenBuffer.width;
        int renderHeight = Renderer.VIEW_HEIGHT;
        int[] screenPixels = screenBuffer.pixels;

        int horizonRow = renderHeight / 2;

        double cameraHeight = 0.5 * renderHeight;

        double playerTileX = player.positionX / GameMap.TILE_SIZE;
        double playerTileY = player.positionY / GameMap.TILE_SIZE;

        double leftmostRayDirectionX = player.directionX - player.cameraPlaneX;
        double leftmostRayDirectionY = player.directionY - player.cameraPlaneY;
        double rightmostRayDirectionX = player.directionX + player.cameraPlaneX;
        double rightmostRayDirectionY = player.directionY + player.cameraPlaneY;

        for (int screenColumn = 0; screenColumn < renderWidth; screenColumn++) {
            screenPixels[(horizonRow - 1) * renderWidth + screenColumn] = HORIZON_ROW_COLOR;
            screenPixels[horizonRow * renderWidth + screenColumn] = HORIZON_ROW_COLOR;
        }

        for (int distanceFromHorizon = 1; distanceFromHorizon < horizonRow; distanceFromHorizon++) {
            int floorRowStart = (horizonRow + distanceFromHorizon) * renderWidth;
            int ceilingRowStart = (horizonRow - 1 - distanceFromHorizon) * renderWidth;

            double rowDistanceInTiles = cameraHeight / distanceFromHorizon;
            int fogAmount = Fog.getFogAmount(rowDistanceInTiles * GameMap.TILE_SIZE);

            double tileStepX = rowDistanceInTiles * (rightmostRayDirectionX - leftmostRayDirectionX) / renderWidth;
            double tileStepY = rowDistanceInTiles * (rightmostRayDirectionY - leftmostRayDirectionY) / renderWidth;

            double tileX = playerTileX + rowDistanceInTiles * leftmostRayDirectionX;
            double tileY = playerTileY + rowDistanceInTiles * leftmostRayDirectionY;

            for (int screenColumn = 0; screenColumn < renderWidth; screenColumn++) {
                int floorTextureX = ((int) Math.floor(tileX * floorTexture.width)) & floorTexture.widthMask;
                int floorTextureY = ((int) Math.floor(tileY * floorTexture.height)) & floorTexture.heightMask;
                int ceilingTextureX = ((int) Math.floor(tileX * ceilingTexture.width)) & ceilingTexture.widthMask;
                int ceilingTextureY = ((int) Math.floor(tileY * ceilingTexture.height)) & ceilingTexture.heightMask;
                int cellColumn = (int) Math.floor(tileX);
                int cellRow = (int) Math.floor(tileY);
                int arrowDirection = gameMap.getArrowDirection(cellColumn, cellRow);
                int arrowColor = arrowDirection < 0 
                    ? 0 
                    : sampleArrow(tileX - cellColumn, tileY - cellRow, arrowDirection);
                
                tileX += tileStepX;
                tileY += tileStepY;

                int floorColor = arrowColor != 0 ? arrowColor : floorTexture.getPixel(floorTextureX, floorTextureY);
                screenPixels[floorRowStart + screenColumn] = Fog.blend(floorColor, fogAmount);

                screenPixels[ceilingRowStart + screenColumn] = Fog.blend(ceilingTexture.getPixel(ceilingTextureX, 
                    ceilingTextureY), fogAmount);
            }
        }
    }
    
    private int sampleArrow(double fractionX, double fractionY, int direction) {
    int size = arrowTexture.width;
    int arrowX = (int) (fractionX * size);
    int arrowY = (int) (fractionY * size);
    int textureX;
    int textureY;
    switch (direction) {
        case 0: textureX = arrowY; textureY = size - 1 - arrowX; break;
        case 1: textureX = size - 1 - arrowY; textureY = arrowX; break;
        case 2: textureX = size - 1 - arrowX; textureY = size - 1 - arrowY; break;
        default: textureX = arrowX; textureY = arrowY; break;
    }
    if (arrowTexture.isTransparent(textureX, textureY)) {
        return 0;
    }
    return arrowTexture.getPixel(textureX, textureY);
}
}