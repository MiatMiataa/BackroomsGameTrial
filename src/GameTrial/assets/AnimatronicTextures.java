package GameTrial.assets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnimatronicTextures {
    private static final String[] DIRECTIONS = {
        "front", "front_right", "right", "back_right",
        "back",  "back_left",   "left",  "front_left"
    };

    private static final int MAX_FRAMES_PER_ANIMATION = 256;

    public final Texture[][] walk = new Texture[8][];  // 8 directions, variable frames each
    public final Texture[] idle;                       // front facing, one for all view angles
    public final Texture[] sprint;                     // Foxy only animation
    public final Texture[] attack;                     // front facing jumpscare for all animatronics

    public AnimatronicTextures(String folder) throws IOException {
        Texture errorTexture = new Texture("src/textures/error.png");

        for (int direction = 0; direction < 8; direction++) {
            Texture[] loadedWalk = loadNumbered(folder, "walk_" + DIRECTIONS[direction] + "_");
            walk[direction] = (loadedWalk.length > 0) ? loadedWalk : new Texture[] { errorTexture };            
        }

        Texture[] loadedIdle = loadNumbered(folder, "idle_");
        idle = (loadedIdle.length > 0) ? loadedIdle : new Texture[] { errorTexture };

        Texture[] sprintFrames = loadNumbered(folder, "sprint_");
        sprint = (sprintFrames.length > 0) ? sprintFrames : null;

        Texture[] loadedAttack = loadNumbered(folder, "attack_");
        attack = (loadedAttack.length > 0) ? loadedAttack : new Texture[] { errorTexture };
        
    }

    private static Texture[] loadNumbered(String folder, String prefix) throws IOException {
        List<Texture> frames = new ArrayList<>();
        for (int index = 1; index <= MAX_FRAMES_PER_ANIMATION; index++) {
            File frame = new File(folder + prefix + index + ".png");
            if (!frame.exists()) {
                break;
            }
            frames.add(new Texture(frame.getPath()));
        }
        return frames.toArray(new Texture[0]);
    }
}