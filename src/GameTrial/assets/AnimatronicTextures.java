package GameTrial.assets;

import java.io.File;
import java.io.IOException;

public class AnimatronicTextures {
    private static final int ORIENTATION_COUNT = 8;

    public final Texture[] byOrientation = new Texture[ORIENTATION_COUNT];

    public AnimatronicTextures(String folder, String name) throws IOException {
        Texture errorTexture = new Texture("src/textures/error.png");
        boolean zeroBasedNumbering = new File(folder + name + "_0.png").exists();

        for (int orientation = 0; orientation < ORIENTATION_COUNT; orientation++) {
            int fileIndex = zeroBasedNumbering ? orientation : orientation + 1;
            File frameFile = new File(folder + name + "_" + fileIndex + ".png");
            if (frameFile.exists()) {
                byOrientation[orientation] = new Texture(frameFile.getPath());
            } else if (byOrientation[0] != null) {
                // A character with a single pose (Golden Freddy never moves) faces
                // the same way from every direction instead of showing an error.
                byOrientation[orientation] = byOrientation[0];
            } else {
                byOrientation[orientation] = errorTexture;
            }
        }
    }
}
