package GameTrial.assets;

import java.io.IOException;

public class TextureSet {
    private static final String TEXTURE_FOLDER = "src/textures/";
    private static final String USHER_FOLDER = TEXTURE_FOLDER + "usher_frames/";
    private static final String[] USHER_DIRECTIONS =
        {"front", "front_right", "right", "back_right", "back", "back_left", "left", "front_left"};

    public final Texture wall;
    public final Texture floor;
    public final Texture ceiling;
    public final Texture itemHealth;
    public final Texture exitMarker;
    public final Texture[][] usherPatrol = new Texture[8][4];
    public final Texture[] usherChase = new Texture[4];
    public final Texture[] usherAttack = new Texture[3];
    public final Texture[] usherVanish = new Texture[5];

    public TextureSet() throws IOException {
        wall = new Texture(TEXTURE_FOLDER + "wallpaper.png");
        floor = new Texture(TEXTURE_FOLDER + "carpet.png");
        ceiling = new Texture(TEXTURE_FOLDER + "ceiling.png");
        itemHealth = new Texture(TEXTURE_FOLDER + "item_health.png");
        exitMarker = new Texture(TEXTURE_FOLDER + "exit_marker.png");

        for (int direction = 0; direction < usherPatrol.length; direction++) {
            for (int frame = 0; frame < usherPatrol[direction].length; frame++) {
                usherPatrol[direction][frame] = new Texture(USHER_FOLDER + "patrol_" + USHER_DIRECTIONS[direction] + "_" + (frame + 1) + ".png");
            }
        }

        for (int frame = 0; frame < usherChase.length; frame++) {
            usherChase[frame] = new Texture(USHER_FOLDER + "chase_" + (frame + 1) + ".png");
        }

        for (int frame = 0; frame < usherAttack.length; frame++) {
            usherAttack[frame] = new Texture(USHER_FOLDER + "attack_" + (frame + 1) + ".png");
        }

        for (int frame = 0; frame < usherVanish.length; frame++) {
            usherVanish[frame] = new Texture(USHER_FOLDER + "vanish_" + (frame + 1) + ".png");
        }
    }
}