package GameTrial.render;

import GameTrial.assets.TextureSet;
import GameTrial.entity.Player;
import java.util.Arrays;
import java.awt.image.BufferedImage;

public class Renderer {
    public static final int RENDER_WIDTH = 540;
    public static final int VIEW_HEIGHT = 440;
    private static final int HUB_COLOR = 0xFF101010;

    private final ScreenBuffer screenBuffer = new ScreenBuffer(RENDER_WIDTH, VIEW_HEIGHT);
    private final FloorCeilingRenderer floorCeilingRenderer;
    private final WallRenderer wallRenderer;

    public Renderer(Raycaster raycaster, TextureSet textureSet) {
        floorCeilingRenderer = new FloorCeilingRenderer(screenBuffer, textureSet.floor, textureSet.ceiling);
        wallRenderer = new WallRenderer(screenBuffer, raycaster, textureSet.wall);
    }

    public void render(Player player) {
        floorCeilingRenderer.render(player);
        wallRenderer.render(player);
        Arrays.fill(screenBuffer.pixels, RENDER_WIDTH * VIEW_HEIGHT, screenBuffer.pixels.length, HUB_COLOR); 
    }

    public BufferedImage getImage() {
        return screenBuffer.getImage();
    }
}