package GameTrial.assets;

import java.io.IOException;

public class TextureSet {
    private static final String TEXTURE_FOLDER = "src/textures/";

    public final Texture wall;
    public final Texture floor;
    public final Texture ceiling;
    public final Texture itemHealth;

    public TextureSet() throws IOException {
        wall = new Texture(TEXTURE_FOLDER + "wallpaper.png");
        floor = new Texture(TEXTURE_FOLDER + "carpet.png");
        ceiling = new Texture(TEXTURE_FOLDER + "ceiling.png");
        itemHealth = new Texture(TEXTURE_FOLDER + "item_health.png");
    }
}