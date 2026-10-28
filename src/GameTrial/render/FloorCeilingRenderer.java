package GameTrial.render;

import GameTrial.assets.Texture;
import GameTrial.entity.Player;
import GameTrial.world.GameMap;

public class FloorCeilingRenderer {
    private static final int HORIZON_ROW_COLOR = 0xFF2A2410;

    private final ScreenBuffer screenBuffer;
    private final Texture floorTexture;
    private final Texture ceilingTexture;

    public FloorCeilingRenderer(ScreenBuffer screenBuffer, Texture floorTexture, Texture ceilingTexture) {
        this.screenBuffer = screenBuffer;
        this.floorTexture = floorTexture;
        this.ceilingTexture = ceilingTexture;
    }

    public void render(Player player) {
        int renderWidth = screenBuffer.width;
        int renderHeight = screenBuffer.height;
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

                tileX += tileStepX;
                tileY += tileStepY;

                screenPixels[floorRowStart + screenColumn] = Fog.blend(floorTexture.getDarkPixel(floorTextureX, floorTextureY), fogAmount);
                screenPixels[ceilingRowStart + screenColumn] = Fog.blend(ceilingTexture.getDarkPixel(ceilingTextureX, ceilingTextureY), fogAmount);
            }
        }
    }
}