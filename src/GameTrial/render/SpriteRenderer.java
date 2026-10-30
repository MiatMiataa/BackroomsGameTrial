package GameTrial.render;

import GameTrial.assets.Texture;
import GameTrial.entity.Player;
import GameTrial.entity.Sprite;
import GameTrial.world.GameMap;
import java.util.Comparator;
import java.util.List;

public class SpriteRenderer {
    private final ScreenBuffer screenBuffer;
    private final double[] columnWallDistances;
    private final Comparator<Sprite> farthestFirst =
        (first, second) -> Double.compare(second.distanceSquaredToPlayer, first.distanceSquaredToPlayer);

    public SpriteRenderer(ScreenBuffer screenBuffer, double[] columnWallDistances) {
        this.screenBuffer = screenBuffer;
        this.columnWallDistances = columnWallDistances;
    }

    public void render(Player player, List<Sprite> sprites) {
        int renderWidth = screenBuffer.width;
        int viewHeight = Renderer.VIEW_HEIGHT;
        int[] screenPixels = screenBuffer.pixels;

        for (Sprite sprite : sprites) {
            double deltaX = sprite.positionX - player.positionX;
            double deltaY = sprite.positionY - player.positionY;
            sprite.distanceSquaredToPlayer = deltaX * deltaX + deltaY * deltaY;
        }
        sprites.sort(farthestFirst);

        double inverseDeterminant = 1.0 / (player.cameraPlaneX * player.directionY - player.directionX * player.cameraPlaneY);

        for (Sprite sprite : sprites) {
            double relativeX = sprite.positionX - player.positionX;
            double relativeY = sprite.positionY - player.positionY;

            double cameraSpaceX = inverseDeterminant * (player.directionY * relativeX - player.directionX * relativeY);
            double depth = inverseDeterminant * (-player.cameraPlaneY * relativeX + player.cameraPlaneX * relativeY);
            if (depth < 1) continue;

            int spriteCenterX = (int) ((renderWidth / 2.0) * (1 + cameraSpaceX / depth));
            int wallHeightAtDepth = (int) ((viewHeight * GameMap.TILE_SIZE) / depth);
            int spriteHeight = (int) (wallHeightAtDepth * sprite.heightScale);
            if (spriteHeight < 1) continue;
            int spriteWidth = spriteHeight;

            int spriteLeftX = spriteCenterX - spriteWidth / 2;
            int spriteTopY = viewHeight / 2 + (wallHeightAtDepth - spriteH eight) / 2 - spriteHeight / 2;

            int firstColumn = Math.max(spriteLeftX, 0);
            int lastColumn = Math.min(spriteLeftX + spriteWidth, renderWidth);
            int firstRow = Math.max(spriteTopY, 0);
            int lastRow = Math.min(spriteTopY + spriteHeight, viewHeight);

            int fogAmount = Fog.getFogAmount(depth);
            Texture texture = sprite.texture;

            for (int screenColumn = firstColumn; screenColumn < lastColumn; screenColumn++) {
                if (depth >= columnWallDistances[screenColumn]) continue;
                int textureX = (screenColumn - spriteLeftX) * texture.width / spriteWidth;

                for (int screenRow = firstRow; screenRow < lastRow; screenRow++) {
                    int textureY = (screenRow - spriteTopY) * texture.height / spriteHeight;
                    if (texture.isTransparent(textureX, textureY)) continue;
                    screenPixels[screenRow * renderWidth + screenColumn] =
                        Fog.blend(texture.getPixel(textureX, textureY), fogAmount);
                }
            }
        }
    }
}