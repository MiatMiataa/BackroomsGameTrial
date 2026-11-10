package GameTrial.assets;


import GameTrial.entity.AnimatronicType;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;


public class TextureSet {
    private static final String TEXTURE_FOLDER = "src/textures/";
    private static final String ANIMATRONIC_FOLDER = TEXTURE_FOLDER + "animatronics/";

    public final Texture wall;
    public final Texture floor;
    public final Texture ceiling;

    private final Map<AnimatronicType, AnimatronicTextures> animatronicTextures = new EnumMap<>(AnimatronicType.class);

    public TextureSet() throws IOException {
        wall    = new Texture(TEXTURE_FOLDER + "wallpaper.png");
        floor   = new Texture(TEXTURE_FOLDER + "carpet.png");
        ceiling = new Texture(TEXTURE_FOLDER + "ceiling.png");
    }
}