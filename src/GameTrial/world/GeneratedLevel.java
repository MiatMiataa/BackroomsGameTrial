package GameTrial.world;

import java.util.List;

public class GeneratedLevel {
    public final int[][] tileGrid;
    public final int spawnTileColumn;
    public final int spawnTileRow;
    // Each entry is {column, row}. One exit for now, a list so more can be added later.
    public final List<int[]> exitTiles;
    // Steps to the nearest exit for every tile. -1 = wall or cannot reach the exit.
    public final int[][] distanceToExit;
    // Each entry is {column, row}. Open tiles for items and entities, away from spawn and exit.
    public final List<int[]> safeSpotTiles;
    public final long usedSeed;

    public GeneratedLevel(int[][] tileGrid, int spawnTileColumn, int spawnTileRow, List<int[]> exitTiles,
                          int[][] distanceToExit, List<int[]> safeSpotTiles, long usedSeed) {
        this.tileGrid = tileGrid;
        this.spawnTileColumn = spawnTileColumn;
        this.spawnTileRow = spawnTileRow;
        this.exitTiles = exitTiles;
        this.distanceToExit = distanceToExit;
        this.safeSpotTiles = safeSpotTiles;
        this.usedSeed = usedSeed;
    }
}