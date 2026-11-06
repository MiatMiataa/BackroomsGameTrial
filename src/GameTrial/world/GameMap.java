package GameTrial.world;

public class GameMap {
    public static final int TILE_SIZE = 64;
    
    public final int exitTileColumn;
    public final int exitTileRow;

    private static final boolean ARROWS_ENABLED = true;
    private static final int ARROW_SPACING_TILES = 4;
    
    private final byte[][] arrowDirection;

    private final boolean[][] pathToExit;


    private int[][] tileGrid = {
        {1,0,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,1,1,0,0,0,0,1},
        {1,0,0,1,1,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,1,1,1,1,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1}
    };

    public GameMap() {
        this.exitTileColumn = -1;
        this.exitTileRow = -1;
        pathToExit = null;
        arrowDirection = null;
    }

    public GameMap(int[][] generatedTileGrid) {
        this.tileGrid = generatedTileGrid;
        this.exitTileColumn = -1;
        this.exitTileRow = -1;
        pathToExit = null;
        arrowDirection = null;
    }

    public GameMap(int[][] generatedTileGrid, int exitTileColumn, int exitTileRow) {
        this.tileGrid = generatedTileGrid;
        this.exitTileColumn = exitTileColumn;
        this.exitTileRow = exitTileRow;
        pathToExit = null;
        arrowDirection = null;
    }

    public GameMap(int[][] generatedTileGrid, int[][] distanceToExit,
               int spawnTileColumn, int spawnTileRow,
               int exitTileColumn, int exitTileRow) {
        this.tileGrid = generatedTileGrid;
        this.exitTileColumn = exitTileColumn;
        this.exitTileRow = exitTileRow;
        this.pathToExit = buildPathToExit(distanceToExit, spawnTileColumn, spawnTileRow);
        this.arrowDirection = buildArrowDirections(distanceToExit);
    }
    public boolean isOnPathToExit(int column, int row) {
    if (pathToExit == null) {
        return false;
    }
    if (row < 0 || row >= pathToExit.length) {
        return false;
    }
    if (column < 0 || column >= pathToExit[0].length) {
        return false;
    }
    return pathToExit[row][column];
}

private static boolean[][] buildPathToExit(int[][] distanceToExit, int spawnColumn, int spawnRow) {
    boolean[][] path = new boolean[distanceToExit.length][distanceToExit[0].length];
    int currentColumn = spawnColumn;
    int currentRow = spawnRow;
    int safety = distanceToExit.length * distanceToExit[0].length + 1;

    while (safety-- > 0) {
        path[currentRow][currentColumn] = true;
        int currentDistance = distanceToExit[currentRow][currentColumn];
        if (currentDistance <= 0) {
            break;
        }
        int[] nextStep = findNeighbourOneStepCloser(distanceToExit, currentColumn, currentRow, currentDistance);
        if (nextStep == null) {
            break;
        }
        currentColumn = nextStep[0];
        currentRow = nextStep[1];
    }
    return path;
}

    private static int[] findNeighbourOneStepCloser(int[][] distanceToExit, int column, int row, int currentDistance) {
        int[][] neighbours = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] direction : neighbours) {
                int nextColumn = column + direction[0];
                int nextRow = row + direction[1];
                if (nextRow < 0 || nextRow >= distanceToExit.length) {
                    continue;
                }
                if (nextColumn < 0 || nextColumn >= distanceToExit[0].length) {
                    continue;
                }
                if (distanceToExit[nextRow][nextColumn] == currentDistance - 1) {
                return new int[]{nextColumn, nextRow};
                }
            }
        return null;
    }

public int getArrowDirection(int column, int row) {
    if (arrowDirection == null) {
        return -1;
    }
    if (row < 0 || row >= arrowDirection.length || column < 0 || column >= arrowDirection[0].length) {
        return -1;
    }
    return arrowDirection[row][column];
}

private static byte[][] buildArrowDirections(int[][] distanceToExit) {
    int[] columnSteps = {1, -1, 0, 0};
    int[] rowSteps = {0, 0, 1, -1};
    int rowCount = distanceToExit.length;
    int columnCount = distanceToExit[0].length;
    byte[][] arrows = new byte[rowCount][columnCount];
    for (byte[] arrowRow : arrows) {
        java.util.Arrays.fill(arrowRow, (byte) -1);
    }
    if (!ARROWS_ENABLED) {
        return arrows;
    }

    for (int row = 0; row < rowCount; row += ARROW_SPACING_TILES) {
        for (int column = 0; column < columnCount; column += ARROW_SPACING_TILES) {
            int distance = distanceToExit[row][column];
            if (distance <= 0) {
                continue;
            }
            for (int direction = 0; direction < 4; direction++) {
                int nextColumn = column + columnSteps[direction];
                int nextRow = row + rowSteps[direction];
                if (nextRow < 0 || nextRow >= rowCount || nextColumn < 0 || nextColumn >= columnCount) {
                    continue;
                }
                if (distanceToExit[nextRow][nextColumn] == distance - 1) {
                    arrows[row][column] = (byte) direction;
                    break;
                }
            }
        }
    }
    return arrows;
}

    public int getTile(int column, int row) {
        if (row < 0 || row >= tileGrid.length || column < 0 || column >= tileGrid[0].length) {
            return 1;
        }
        return tileGrid[row][column];
    }

    public boolean isWall(double pixelX, double pixelY) {
        int column = (int) Math.floor(pixelX / TILE_SIZE);
        int row = (int) Math.floor(pixelY / TILE_SIZE);
        return getTile(column, row) == 1;
    }

    public int getRowsCount() {
        return tileGrid.length;
    }

    public int getColumnCount() {
        return tileGrid[0].length;
    }
}
