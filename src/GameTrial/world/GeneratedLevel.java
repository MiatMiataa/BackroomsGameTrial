package GameTrial.world;

import java.util.List;

public class GeneratedLevel {
    public final int[][] tileGrid;
    public final int spawnTileColumn;
    public final int spawnTileRow;
    public final List<int[]> exitTiles;
    public final int[][] distanceToExit;
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