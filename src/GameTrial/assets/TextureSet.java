package GameTrial.assets;

import GameTrial.entity.AnimatronicType;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class TextureSet {
    private static final String TEXTURE_FOLDER = "src/textures/";
    private static final String ANIMATRONIC_FOLDER = TEXTURE_FOLDER;
    
    public final Texture wall;
    public final Texture floor;
    public final Texture ceiling;
    public final Texture officeDoor;
    public final Texture pirateCove;

    private final Map<AnimatronicType, AnimatronicTextures> animatronicTextures = new EnumMap<>(AnimatronicType.class);

    public TextureSet() throws IOException {
        wall = loadOrFail(TEXTURE_FOLDER + "wall.png");
        floor = loadOrFail(TEXTURE_FOLDER + "floor.png");
        ceiling = loadOrFail(TEXTURE_FOLDER + "ceiling.png");
        officeDoor = loadOrFail(TEXTURE_FOLDER + "office_door.png");
        pirateCove = loadOrFail(TEXTURE_FOLDER + "pirate_cove.png");

        for (AnimatronicType type : AnimatronicType.values()) {
            animatronicTextures.put(type, new AnimatronicTextures(ANIMATRONIC_FOLDER + type.folder() + "/", type.folder()));
        }
    }

    private Texture loadOrFail(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            return new Texture(TEXTURE_FOLDER + "error.png");
        }
        return new Texture(path);
    }

    public AnimatronicTextures of(AnimatronicType type) {
        return animatronicTextures.get(type);
    }
}
