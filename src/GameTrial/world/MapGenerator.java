package GameTrial.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapGenerator {
    public static final long DEFAULT_SEED = 12345L;

    // Tiles per chunk side. Each chunk owns its top and left wall, so 1 wall + 7 open tiles.
    private static final int CHUNK_SIZE = 8;
    // Chunks per world side. Keep it odd so the exit chunk sits exactly in the middle.
    private static final int WORLD_CHUNKS = 21;
    // Chance that a closed wall between two chunks gets an extra door (makes loops and detours).
    private static final double EXTRA_DOOR_CHANCE = 0.12;
    // 0 = bushy tree with many short dead ends. Closer to 1 = long winding routes with fewer branches.
    private static final double WINDING_CHANCE = 0.6;
    // Tries with the next seed if the exit cannot be reached. Should never be needed.
    private static final int MAX_SEED_ATTEMPTS = 50;

    private static final int WALL_TILE = 1;
    private static final int OPEN_TILE = 0;
    // Door tiles along a chunk wall. Also the clear lane through the chunk, so doors always connect.
    private static final int LANE_START = 3;
    private static final int LANE_END = 4;

    private static final int[][] FOUR_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public static GeneratedLevel generate(long seed) {
        ChunkTemplates.checkAll(CHUNK_SIZE - 1);
        for (int attempt = 0; attempt < MAX_SEED_ATTEMPTS; attempt++) {
            GeneratedLevel level = tryGenerate(seed + attempt);
            if (level.distanceToExit[level.spawnTileRow][level.spawnTileColumn] >= 0) return level;
        }
        throw new IllegalStateException("No reachable map found from seed " + seed);
    }

    private static GeneratedLevel tryGenerate(long seed) {
        Random random = new Random(seed);

        int exitChunkColumn = WORLD_CHUNKS / 2;
        int exitChunkRow = WORLD_CHUNKS / 2;
        // Spawn in a random corner chunk.
        int spawnChunkColumn = random.nextBoolean() ? 0 : WORLD_CHUNKS - 1;
        int spawnChunkRow = random.nextBoolean() ? 0 : WORLD_CHUNKS - 1;

        // doorEast[c][r] = door between chunk (c,r) and (c+1,r). doorSouth[c][r] = door between (c,r) and (c,r+1).
        boolean[][] doorEast = new boolean[WORLD_CHUNKS][WORLD_CHUNKS];
        boolean[][] doorSouth = new boolean[WORLD_CHUNKS][WORLD_CHUNKS];

        connectAllChunks(random, spawnChunkColumn, spawnChunkRow, doorEast, doorSouth);
        addExtraDoors(random, doorEast, doorSouth);

        int tileCount = WORLD_CHUNKS * CHUNK_SIZE + 1;
        int[][] tileGrid = new int[tileCount][tileCount];
        for (int row = 0; row < tileCount; row++) {
            for (int column = 0; column < tileCount; column++) tileGrid[row][column] = WALL_TILE;
        }

        for (int chunkRow = 0; chunkRow < WORLD_CHUNKS; chunkRow++) {
            for (int chunkColumn = 0; chunkColumn < WORLD_CHUNKS; chunkColumn++) {
                boolean hasWestDoor = chunkColumn > 0 && doorEast[chunkColumn - 1][chunkRow];
                boolean hasNorthDoor = chunkRow > 0 && doorSouth[chunkColumn][chunkRow - 1];
                boolean isSpawnChunk = chunkColumn == spawnChunkColumn && chunkRow == spawnChunkRow;
                boolean isExitChunk = chunkColumn == exitChunkColumn && chunkRow == exitChunkRow;
                int[][] template = isSpawnChunk ? ChunkTemplates.SPAWN_ROOM
                        : isExitChunk ? ChunkTemplates.EXIT_ROOM
                        : ChunkTemplates.pickRandom(random);
                buildChunk(tileGrid, chunkColumn, chunkRow, hasWestDoor, hasNorthDoor, template);
            }
        }

        int spawnTileColumn = spawnChunkColumn * CHUNK_SIZE + LANE_END;
        int spawnTileRow = spawnChunkRow * CHUNK_SIZE + LANE_END;
        int exitTileColumn = exitChunkColumn * CHUNK_SIZE + LANE_END;
        int exitTileRow = exitChunkRow * CHUNK_SIZE + LANE_END;

        List<int[]> exitTiles = new ArrayList<>();
        exitTiles.add(new int[]{exitTileColumn, exitTileRow});

        List<int[]> safeSpotTiles = new ArrayList<>();
        for (int chunkRow = 0; chunkRow < WORLD_CHUNKS; chunkRow++) {
            for (int chunkColumn = 0; chunkColumn < WORLD_CHUNKS; chunkColumn++) {
                boolean isSpawnChunk = chunkColumn == spawnChunkColumn && chunkRow == spawnChunkRow;
                boolean isExitChunk = chunkColumn == exitChunkColumn && chunkRow == exitChunkRow;
                if (isSpawnChunk || isExitChunk) continue;
                safeSpotTiles.add(new int[]{chunkColumn * CHUNK_SIZE + LANE_END, chunkRow * CHUNK_SIZE + LANE_END});
            }
        }

        int[][] distanceToExit = measureDistanceToExit(tileGrid, exitTileColumn, exitTileRow);
        return new GeneratedLevel(tileGrid, spawnTileColumn, spawnTileRow, exitTiles, distanceToExit, safeSpotTiles, seed);
    }

    // Grows a tree of doors over all chunks: every chunk gets reachable, with many short dead ends.
    private static void connectAllChunks(Random random, int startChunkColumn, int startChunkRow,
                                         boolean[][] doorEast, boolean[][] doorSouth) {
        boolean[][] isVisited = new boolean[WORLD_CHUNKS][WORLD_CHUNKS];
        List<int[]> activeChunks = new ArrayList<>();
        activeChunks.add(new int[]{startChunkColumn, startChunkRow});
        isVisited[startChunkColumn][startChunkRow] = true;

        while (!activeChunks.isEmpty()) {
            int activeIndex = random.nextDouble() < WINDING_CHANCE
                    ? activeChunks.size() - 1
                    : random.nextInt(activeChunks.size());
            int[] activeChunk = activeChunks.get(activeIndex);

            List<int[]> unvisitedNeighbours = new ArrayList<>();
            for (int[] direction : FOUR_DIRECTIONS) {
                int neighbourColumn = activeChunk[0] + direction[0];
                int neighbourRow = activeChunk[1] + direction[1];
                boolean isInside = neighbourColumn >= 0 && neighbourColumn < WORLD_CHUNKS
                        && neighbourRow >= 0 && neighbourRow < WORLD_CHUNKS;
                if (isInside && !isVisited[neighbourColumn][neighbourRow]) {
                    unvisitedNeighbours.add(new int[]{neighbourColumn, neighbourRow});
                }
            }

            if (unvisitedNeighbours.isEmpty()) {
                activeChunks.remove(activeIndex);
                continue;
            }

            int[] chosenNeighbour = unvisitedNeighbours.get(random.nextInt(unvisitedNeighbours.size()));
            openDoor(activeChunk[0], activeChunk[1], chosenNeighbour[0], chosenNeighbour[1], doorEast, doorSouth);
            isVisited[chosenNeighbour[0]][chosenNeighbour[1]] = true;
            activeChunks.add(chosenNeighbour);
        }
    }

    private static void addExtraDoors(Random random, boolean[][] doorEast, boolean[][] doorSouth) {
        for (int chunkRow = 0; chunkRow < WORLD_CHUNKS; chunkRow++) {
            for (int chunkColumn = 0; chunkColumn < WORLD_CHUNKS; chunkColumn++) {
                if (chunkColumn < WORLD_CHUNKS - 1 && !doorEast[chunkColumn][chunkRow]
                        && random.nextDouble() < EXTRA_DOOR_CHANCE) {
                    doorEast[chunkColumn][chunkRow] = true;
                }
                if (chunkRow < WORLD_CHUNKS - 1 && !doorSouth[chunkColumn][chunkRow]
                        && random.nextDouble() < EXTRA_DOOR_CHANCE) {
                    doorSouth[chunkColumn][chunkRow] = true;
                }
            }
        }
    }

    private static void openDoor(int columnA, int rowA, int columnB, int rowB,
                                 boolean[][] doorEast, boolean[][] doorSouth) {
        if (columnB == columnA + 1) doorEast[columnA][rowA] = true;
        else if (columnB == columnA - 1) doorEast[columnB][rowB] = true;
        else if (rowB == rowA + 1) doorSouth[columnA][rowA] = true;
        else doorSouth[columnB][rowB] = true;
    }

    private static void buildChunk(int[][] tileGrid, int chunkColumn, int chunkRow,
                                   boolean hasWestDoor, boolean hasNorthDoor, int[][] template) {
        int originColumn = chunkColumn * CHUNK_SIZE;
        int originRow = chunkRow * CHUNK_SIZE;

        // The template fills the inside: everything except the top row and left column (the walls).
        for (int row = 0; row < template.length; row++) {
            for (int column = 0; column < template[row].length; column++) {
                tileGrid[originRow + 1 + row][originColumn + 1 + column] = template[row][column];
            }
        }

        // Doors in the walls this chunk owns. The south and east doors are owned by the neighbours.
        for (int lane = LANE_START; lane <= LANE_END; lane++) {
            if (hasWestDoor) tileGrid[originRow + lane][originColumn] = OPEN_TILE;
            if (hasNorthDoor) tileGrid[originRow][originColumn + lane] = OPEN_TILE;
        }
    }

    // Flood fill from the exit. Also proves the exit can be reached.
    private static int[][] measureDistanceToExit(int[][] tileGrid, int exitTileColumn, int exitTileRow) {
        int rowCount = tileGrid.length;
        int columnCount = tileGrid[0].length;
        int[][] distanceToExit = new int[rowCount][columnCount];
        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < columnCount; column++) distanceToExit[row][column] = -1;
        }

        ArrayDeque<int[]> waitingTiles = new ArrayDeque<>();
        distanceToExit[exitTileRow][exitTileColumn] = 0;
        waitingTiles.add(new int[]{exitTileColumn, exitTileRow});

        while (!waitingTiles.isEmpty()) {
            int[] currentTile = waitingTiles.poll();
            for (int[] direction : FOUR_DIRECTIONS) {
                int nextColumn = currentTile[0] + direction[0];
                int nextRow = currentTile[1] + direction[1];
                boolean isInside = nextColumn >= 0 && nextColumn < columnCount && nextRow >= 0 && nextRow < rowCount;
                if (!isInside || tileGrid[nextRow][nextColumn] == WALL_TILE) continue;
                if (distanceToExit[nextRow][nextColumn] != -1) continue;
                distanceToExit[nextRow][nextColumn] = distanceToExit[currentTile[1]][currentTile[0]] + 1;
                waitingTiles.add(new int[]{nextColumn, nextRow});
            }
        }
        return distanceToExit;
    }

    // Run this class to read the map as text. # = wall, . = open, S = spawn, E = exit.
    public static void main(String[] arguments) {
        GeneratedLevel level = generate(DEFAULT_SEED);
        StringBuilder text = new StringBuilder();
        for (int row = 0; row < level.tileGrid.length; row++) {
            for (int column = 0; column < level.tileGrid[0].length; column++) {
                char symbol = level.tileGrid[row][column] == WALL_TILE ? '#' : '.';
                if (column == level.spawnTileColumn && row == level.spawnTileRow) symbol = 'S';
                if (column == level.exitTiles.get(0)[0] && row == level.exitTiles.get(0)[1]) symbol = 'E';
                text.append(symbol);
            }
            text.append('\n');
        }
        System.out.print(text);
        System.out.println("Seed used: " + level.usedSeed);
        System.out.println("Steps from spawn to exit: " + level.distanceToExit[level.spawnTileRow][level.spawnTileColumn]);
    }
}