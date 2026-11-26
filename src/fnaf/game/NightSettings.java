package fnaf.game;

public final class NightSettings {
    private NightSettings() {}

    // Index = night number (1-5). Columns = Freddy, Bonnie, Chica, Foxy AI level at 12 AM.
    private static final int[][] STARTING_AI_LEVEL = {
        {0, 0, 0, 0},   // index 0 unused, keeps the array 1-based
        {0, 0, 0, 0},   // night 1
        {0, 3, 1, 1},   // night 2
        {1, 0, 5, 2},   // night 3
        {2, 2, 4, 6},   // night 4
        {3, 5, 7, 5}    // night 5
    };

    public static int nightCount() { return STARTING_AI_LEVEL.length - 1; }

    public static int freddyAILevel(int night) { return STARTING_AI_LEVEL[night][0]; }
    public static int bonnieAILevel(int night) { return STARTING_AI_LEVEL[night][1]; }
    public static int chicaAILevel(int night)  { return STARTING_AI_LEVEL[night][2]; }
    public static int foxyAILevel(int night)   { return STARTING_AI_LEVEL[night][3]; }
}
