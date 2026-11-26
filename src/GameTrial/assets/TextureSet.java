package GameTrial.assets;

import GameTrial.entity.AnimatronicType;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class TextureSet {
    private static final String TEXTURE_FOLDER = "src/textures/";
    private static final String ANIMATRONIC_FOLDER = TEXTURE_FOLDER;
    private static final String DESK_FOLDER = TEXTURE_FOLDER + "desk/";
    private static final int DESK_FRAME_COUNT = 4;

    public final Texture wall;
    public final Texture floor;
    public final Texture ceiling;
    public final Texture officeDoor;
    public final Texture pirateCove;
    public final Texture[] deskFrames;

    private final Map<AnimatronicType, AnimatronicTextures> animatronicTextures = new EnumMap<>(AnimatronicType.class);

    public TextureSet() throws IOException {
        wall = loadOrFail(TEXTURE_FOLDER + "wall.png");
        floor = loadOrFail(TEXTURE_FOLDER + "floor.png");
        ceiling = loadOrFail(TEXTURE_FOLDER + "ceiling.png");
        officeDoor = loadOrFail(TEXTURE_FOLDER + "office_door.png");
        pirateCove = loadOrFail(TEXTURE_FOLDER + "pirate_cove.png");
        deskFrames = loadDeskFrames();

        for (AnimatronicType type : AnimatronicType.values()) {
            animatronicTextures.put(type, new AnimatronicTextures(ANIMATRONIC_FOLDER + type.folder() + "/", type.folder()));
        }
    }

    private Texture[] loadDeskFrames() throws IOException {
        Texture[] frames = new Texture[DESK_FRAME_COUNT];
        for (int frameIndex = 0; frameIndex < DESK_FRAME_COUNT; frameIndex++) {
            frames[frameIndex] = loadOrFail(DESK_FOLDER + "Desk_" + (frameIndex + 1) + ".png");
        }
        return frames;
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
