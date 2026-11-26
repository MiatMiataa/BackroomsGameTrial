package fnaf.render;

import fnaf.entity.Sprite;
import java.util.List;
import fnaf.assets.TextureSet;
import fnaf.entity.Player;
import java.awt.image.BufferedImage;

public class Renderer {
    public static final int RENDER_WIDTH = 480;
    public static final int RENDER_HEIGHT = 270;
    
    private final ScreenBuffer screenBuffer = new ScreenBuffer(RENDER_WIDTH, RENDER_HEIGHT);
    private final FloorAndCeilingRenderer floorAndCeilingRenderer;
    private final WallRenderer wallRenderer;
    private final SpriteRenderer spriteRenderer;

    public Renderer(Raycaster raycaster, TextureSet textureSet) {
        floorAndCeilingRenderer = new FloorAndCeilingRenderer(screenBuffer, textureSet.floor, textureSet.ceiling);
        wallRenderer = new WallRenderer(screenBuffer, raycaster, textureSet.wall,
            textureSet.officeDoor, textureSet.pirateCove);
        spriteRenderer = new SpriteRenderer(screenBuffer, wallRenderer.columnWallDistances);
    }

    public void render(Player player, List<Sprite> sprites) {
        floorAndCeilingRenderer.render(player);
        wallRenderer.render(player);
        spriteRenderer.render(player, sprites);
    }

    public BufferedImage getImage() {
        return screenBuffer.getImage();
    }
}
