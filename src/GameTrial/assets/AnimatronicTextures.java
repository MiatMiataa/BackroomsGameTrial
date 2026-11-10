package GameTrial.assets;

import java.io.IOException;

public class AnimatronicTextures {
    private static final String[] DIRECTIONS ={
        "front", "front_left", "left", "back_left", "back", "back_right", "right", "front_right"
    };

    public final Texture[][] walk = new Texture[8][4];
    public final Texture[] sprint;
    public final Texture[] attack;
}
