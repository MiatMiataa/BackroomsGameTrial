package GameTrial.assets;

import java.io.File;
import java.io.IOException;

public class AnimatronicTextures {
    private static final String[] DIRECTIONS = {
        "front", "front_right", "right", "back_right",
        "back",  "back_left",   "left",  "front_left"
    };

    public final Texture[][] walk = new Texture[8][4];
    public final Texture[] sprint;
    public final Texture[] attack;

    public AnimatronicTextures(String folder) throws IOException {
        for (int direction = 0; direction < 8; direction++) {
            for (int frame = 0; frame < 4; frame++) {
                walk[direction][frame] = new Texture(
                    folder + "walk_" + DIRECTIONS[direction] + "_" + (frame + 1) + ".png");
            }
        }
        sprint = new File(folder + "sprint_1.png").exists()
            ? loadFlat(folder, "sprint_", 4)
            : null;
        attack = loadFlat(folder, "attack_", 3);
    }

    private static Texture[] loadFlat(String folder, String prefix, int count) throws IOException {
        Texture[] frames = new Texture[count];
        for (int i = 0; i < count; i++) {
            frames[i] = new Texture(folder + prefix + (i + 1) + ".png");
        }
        return frames;
    }
}