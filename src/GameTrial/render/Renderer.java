package GameTrial.render;

import GameTrial.entity.Sprite;
import java.util.List;
import GameTrial.assets.TextureSet;
import GameTrial.entity.Player;
import java.util.Arrays;
import java.awt.image.BufferedImage;

public class Renderer {
    public static final int RENDER_WIDTH = 960;
    public static final int RENDER_HEIGHT = 540;
    public static final int VIEW_HEIGHT = 440;
    private static final int HUD_COLOR = 0xFF101010;
    
    private final ScreenBuffer screenBuffer = new ScreenBuffer(RENDER_WIDTH, RENDER_HEIGHT);
    private final FloorCeilingRenderer floorCeilingRenderer;
    private final WallRenderer wallRenderer;
    private final SpriteRenderer spriteRenderer;

    public Renderer(Raycaster raycaster, TextureSet textureSet) {
        new FloorCeilingRenderer(screenBuffer, textureSet.floor, textureSet.ceiling);
        wallRenderer = new WallRenderer(screenBuffer, raycaster, textureSet.wall);
        spriteRenderer = new SpriteRenderer(screenBuffer, wallRenderer.columnWallDistances);
    }

    public void render(Player player, List<Sprite> sprites) {
        floorCeilingRenderer.render(player);
        wallRenderer.render(player);
        spriteRenderer.render(player, sprites);
        Arrays.fill(screenBuffer.pixels, RENDER_WIDTH * VIEW_HEIGHT, screenBuffer.pixels.length, HUD_COLOR); 
    }

    public BufferedImage getImage() {
        return screenBuffer.getImage();
    }
}