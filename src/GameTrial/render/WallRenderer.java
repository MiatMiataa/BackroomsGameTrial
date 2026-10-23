package GameTrial.render;

import GameTrial.assets.Texture;
import GameTrial.entity.Player;
import GameTrial.world.GameMap;

public class WallRenderer {
    private final ScreenBuffer screenBuffer;
    private final Raycaster raycaster;
    private final Texture wallTexture;

    public WallRenderer(ScreenBuffer screenBuffer, Raycaster raycaster, Texture wallTexture) {
        this.screenBuffer = screenBuffer;
        this.raycaster = raycaster;
        this.wallTexture = wallTexture;
    }

    public void render(Player player) {
        int renderWidth = screenBuffer.width;
        int renderHeight = screenBuffer.height;
        int[] screenPixels = screenBuffer.pixels;

        for (int screenColumn = 0; screenColumn < renderWidth; screenColumn++) {
            double cameraX = 2 * screenColumn / (double) renderWidth - 1;
            double rayDirectionX = player.directionX + player.cameraPlaneX * cameraX;
            double rayDirectionY = player.directionY + player.cameraPlaneY * cameraX;

            RayHit rayHitResult = raycaster.castRay(player.positionX, player.positionY, rayDirectionX, rayDirectionY);

            int wallStripeHeight = (int) ((renderHeight * GameMap.TILE_SIZE) / rayHitResult.perpendicularWallDistance);

            int stripeTopY = -wallStripeHeight / 2 + renderHeight / 2;
            if (stripeTopY < 0) stripeTopY = 0;
            int stripeBottomY = wallStripeHeight / 2 + renderHeight / 2;
            if (stripeBottomY >= renderHeight) stripeBottomY = renderHeight - 1;

            boolean isShadedSide = (rayHitResult.wallOrientation == 1);

            int textureColumnX = ((int) (rayHitResult.wallHitFraction * wallTexture.width)) & wallTexture.widthMask;
            if (rayHitResult.wallOrientation == 0 && rayDirectionX > 0) textureColumnX = wallTexture.width - textureColumnX - 1;
            if (rayHitResult.wallOrientation == 1 && rayDirectionY < 0) textureColumnX = wallTexture.width - textureColumnX - 1;

            double textureStepPerPixel = (double) wallTexture.height / wallStripeHeight;
            double textureRowPosition = (stripeTopY - renderHeight / 2 + wallStripeHeight / 2) * textureStepPerPixel;

            for (int screenRow = stripeTopY; screenRow < stripeBottomY; screenRow++) {
                int textureRowY = (int) textureRowPosition;
                if (textureRowY >= wallTexture.height) textureRowY = wallTexture.height - 1;
                textureRowPosition += textureStepPerPixel;

                screenPixels[screenRow * renderWidth + screenColumn] = isShadedSide
                        ? wallTexture.getDarkPixel(textureColumnX, textureRowY)
                        : wallTexture.getPixel(textureColumnX, textureRowY);
            }
        }
    }
}