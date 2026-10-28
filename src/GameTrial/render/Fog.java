package GameTrial.render;

import GameTrial.world.GameMap;

public class Fog {
    private static final double FOG_START_TILES = 2.0;
    private static final double FOG_END_TILES = 24.0;

    private static final int FOG_RED = 42;
    private static final int FOG_GREEN = 36;
    private static final int FOG_BLUE = 16;

    private static final int FULL_FOG = 256;

    public static int getFogAmount(double distanceInPixels) {
        double distanceInTiles = distanceInPixels / GameMap.TILE_SIZE;
        if (distanceInTiles <= FOG_START_TILES) return 0;
        if (distanceInTiles >= FOG_END_TILES) return FULL_FOG;
        return (int) (FULL_FOG * (distanceInTiles - FOG_START_TILES) / (FOG_END_TILES - FOG_START_TILES));
    }

    public static int blend(int pixel, int fogAmount) {
        if (fogAmount == 0) return pixel;
        int keepAmount = FULL_FOG - fogAmount;
        int red = (((pixel >> 16) & 0xFF) * keepAmount + FOG_RED * fogAmount) >> 8;
        int green = (((pixel >> 8) & 0xFF) * keepAmount + FOG_GREEN * fogAmount) >> 8;
        int blue = ((pixel & 0xFF) * keepAmount + FOG_BLUE * fogAmount) >> 8;
        return (0xFF << 24) | (red << 16) | (green << 8) | blue;
    }
}