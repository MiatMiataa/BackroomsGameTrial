package GameTrial.render;

import GameTrial.assets.TextureSet;
import GameTrial.entity.Player;

import java.awt.image.BufferedImage;

public class Renderer {
    public static final int RENDER_WIDTH = 960;
    public static final int RENDER_HEIGHT = 540;

    private final ScreenBuffer screenBuffer = new ScreenBuffer(RENDER_WIDTH, RENDER_HEIGHT);
    private final FloorCeilingRenderer floorCeilingRenderer;
    private final WallRenderer wallRenderer;

    public Renderer(Raycaster raycaster, TextureSet textureSet) {
        floorCeilingRenderer = new FloorCeilingRenderer(screenBuffer, textureSet.floor, textureSet.ceiling);
        wallRenderer = new WallRenderer(screenBuffer, raycaster, textureSet.wall);
    }

    public void render(Player player) {
        floorCeilingRenderer.render(player);
        wallRenderer.render(player);
    }

    public BufferedImage getImage() {
        return screenBuffer.getImage();
    }
}