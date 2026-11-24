package GameTrial.render;

import GameTrial.assets.Texture;
import GameTrial.entity.Player;
import GameTrial.world.GameMap;
import GameTrial.world.SlidingBarrier;

public class WallRenderer {
    private final ScreenBuffer screenBuffer;
    private final Raycaster raycaster;
    private final Texture wallTexture;
    private final Texture officeDoorTexture;
    private final Texture pirateCoveTexture;
    public final double[] columnWallDistances;

    public WallRenderer(ScreenBuffer screenBuffer, Raycaster raycaster, Texture wallTexture,
                        Texture officeDoorTexture, Texture pirateCoveTexture) {
        this.screenBuffer = screenBuffer;
        this.raycaster = raycaster;
        this.wallTexture = wallTexture;
        this.officeDoorTexture = officeDoorTexture;
        this.pirateCoveTexture = pirateCoveTexture;
        this.columnWallDistances = new double[screenBuffer.width];
    }

    public void render(Player player) {
        int renderWidth = screenBuffer.width;

        for (int screenColumn = 0; screenColumn < renderWidth; screenColumn++) {
            double cameraX = 2 * screenColumn / (double) renderWidth - 1;
            double rayDirectionX = player.directionX + player.cameraPlaneX * cameraX;
            double rayDirectionY = player.directionY + player.cameraPlaneY * cameraX;

            RayHit rayHitResult = raycaster.castRay(player.positionX, player.positionY, rayDirectionX, rayDirectionY);

            // A barrier counts as a wall here, so an animatronic standing in the
            // doorway stays hidden until the door is fully open.
            columnWallDistances[screenColumn] = rayHitResult.hasHitWall 
                ? rayHitResult.perpendicularWallDistance 
                : Double.MAX_VALUE;

            if (!rayHitResult.hasHitWall) {
                continue;
            }

            // What the doorway lets through is drawn first, the door or curtain on top.
            if (rayHitResult.hitBarrier != null && rayHitResult.wallBehindBarrier != null
                && rayHitResult.wallBehindBarrier.hasHitWall) {
                drawColumn(screenColumn, rayHitResult.wallBehindBarrier, wallTexture,
                    rayDirectionX, rayDirectionY);
            }

            if (rayHitResult.hitBarrier == null) {
                drawColumn(screenColumn, rayHitResult, wallTexture, rayDirectionX, rayDirectionY);
            } else {
                drawBarrierColumn(screenColumn, rayHitResult, rayDirectionX, rayDirectionY);
            }
        }
    }

    private void drawColumn(int screenColumn, RayHit rayHitResult, Texture texture,
                            double rayDirectionX, double rayDirectionY) {

        int renderWidth = screenBuffer.width;
        int renderHeight = Renderer.RENDER_HEIGHT;
        int[] screenPixels = screenBuffer.pixels;

        int wallStripeHeight = (int) ((renderHeight * GameMap.TILE_SIZE) / rayHitResult.perpendicularWallDistance);
        if (wallStripeHeight <= 0) {
            return;
        }
        int fogAmount = Fog.getFogAmount(rayHitResult.perpendicularWallDistance);

        int stripeTopY = -wallStripeHeight / 2 + renderHeight / 2;
        if (stripeTopY < 0) {
            stripeTopY = 0;
        }

        int stripeBottomY = wallStripeHeight / 2 + renderHeight / 2;
        if (stripeBottomY >= renderHeight) {
            stripeBottomY = renderHeight - 1;
        }

        boolean isShadedSide = (rayHitResult.wallOrientation == 1);

        int textureColumnX = ((int) (rayHitResult.wallHitFraction * texture.width)) & texture.widthMask;
        if (rayHitResult.wallOrientation == 0 && rayDirectionX > 0) {
            textureColumnX = texture.width - textureColumnX - 1; 
        }
        if (rayHitResult.wallOrientation == 1 && rayDirectionY < 0) { 
            textureColumnX = texture.width - textureColumnX - 1;
        }

        double textureStepPerPixel = (double) texture.height / wallStripeHeight;
        double textureRowPosition = (stripeTopY - renderHeight / 2 + wallStripeHeight / 2) * textureStepPerPixel;

        for (int screenRow = stripeTopY; screenRow < stripeBottomY; screenRow++) {
            int textureRowY = (int) textureRowPosition;
            if (textureRowY >= texture.height) {
                textureRowY = texture.height - 1;
            }
            textureRowPosition += textureStepPerPixel;

            int wallPixel = isShadedSide
                ? texture.getDarkPixel(textureColumnX, textureRowY)
                : texture.getPixel(textureColumnX, textureRowY);
                
            screenPixels[screenRow * renderWidth + screenColumn] = Fog.blend(wallPixel, fogAmount);
        }
    }

    /**
     * Draws the part of a door or curtain that is still filling the doorway.
     *
     * An office door slides up, so the gap it leaves is at the bottom and the piece
     * still hanging in the doorway is the lower part of the door image. The curtain
     * slides sideways, so whole columns are either covered by the fabric or past its
     * edge, and the fabric pattern travels with the edge that is opening.
     */
    private void drawBarrierColumn(int screenColumn, RayHit rayHitResult,
                                   double rayDirectionX, double rayDirectionY) {

        SlidingBarrier barrier = rayHitResult.hitBarrier;
        boolean isCurtain = (barrier.type == SlidingBarrier.BarrierType.PIRATE_COVE_CURTAIN);

        if (isCurtain && rayHitResult.wallHitFraction > 1.0 - barrier.openAmount) {
            return;   // this column is already past the edge of the curtain
        }

        Texture texture = isCurtain ? pirateCoveTexture : officeDoorTexture;

        int renderWidth = screenBuffer.width;
        int renderHeight = Renderer.RENDER_HEIGHT;
        int[] screenPixels = screenBuffer.pixels;

        int wallStripeHeight = (int) ((renderHeight * GameMap.TILE_SIZE) / rayHitResult.perpendicularWallDistance);
        if (wallStripeHeight <= 0) {
            return;
        }

        // Kept unclipped: the door's bottom edge is measured from the real top of the
        // stripe, which is often off screen when the door is close.
        int unclippedStripeTopY = -wallStripeHeight / 2 + renderHeight / 2;
        int stripeTopY = Math.max(unclippedStripeTopY, 0);
        int stripeBottomY;
        if (isCurtain) {
            stripeBottomY = unclippedStripeTopY + wallStripeHeight;
        } else {
            stripeBottomY = unclippedStripeTopY + (int) ((1.0 - barrier.openAmount) * wallStripeHeight);
        }
        stripeBottomY = Math.min(stripeBottomY, renderHeight);
        if (stripeTopY >= stripeBottomY) {
            return;
        }

        int fogAmount = Fog.getFogAmount(rayHitResult.perpendicularWallDistance);
        boolean isShadedSide = (rayHitResult.wallOrientation == 1);

        int textureColumnX;
        if (isCurtain) {
            textureColumnX = ((int) (rayHitResult.wallHitFraction * texture.width
                + barrier.openAmount * texture.width)) & texture.widthMask;
        } else {
            textureColumnX = ((int) (rayHitResult.wallHitFraction * texture.width)) & texture.widthMask;
            if (rayHitResult.wallOrientation == 0 && rayDirectionX > 0) {
                textureColumnX = texture.width - textureColumnX - 1;
            }
            if (rayHitResult.wallOrientation == 1 && rayDirectionY < 0) {
                textureColumnX = texture.width - textureColumnX - 1;
            }
        }

        double textureStepPerPixel = (double) texture.height / wallStripeHeight;
        double textureRowPosition = (stripeTopY - unclippedStripeTopY) * textureStepPerPixel
            + (isCurtain ? 0.0 : barrier.openAmount * texture.height);

        for (int screenRow = stripeTopY; screenRow < stripeBottomY; screenRow++) {
            int textureRowY = (int) textureRowPosition;
            if (textureRowY >= texture.height) {
                textureRowY = texture.height - 1;
            }
            if (textureRowY < 0) {
                textureRowY = 0;
            }
            textureRowPosition += textureStepPerPixel;

            int wallPixel = isShadedSide
                ? texture.getDarkPixel(textureColumnX, textureRowY)
                : texture.getPixel(textureColumnX, textureRowY);

            screenPixels[screenRow * renderWidth + screenColumn] = Fog.blend(wallPixel, fogAmount);
        }
    }
}
