package GameTrial.world;

public class GameMap {
    public static final int TILE_SIZE = 64;  //each tile is 64px

    // 1 = wall, 0 = floor
    private final int[][] tileGrid = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},  //0
        {1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1},  //1
        {1,1,1,1,1,1,1,1,2,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1},  //2
        {1,1,1,1,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1},  //3
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,1,1,1,1,1},  //4
        {1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1},  //5
        {1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,1},  //6
        {1,2,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,1,1},  //7
        {1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,1,1},  //8
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1},  //9
        {1,0,0,3,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,1},  //10
        {1,1,1,1,1,1,1,1,0,0,1,1,1,0,0,1,1,0,1,1,1,1,1,1,1,1,1,1},  //11
        {1,1,1,1,1,1,1,1,0,0,1,1,1,0,0,1,0,0,0,0,0,0,0,1,1,1,1,1},  //12
        {1,1,1,1,0,0,0,1,0,0,1,1,1,0,0,1,0,0,0,0,0,0,0,1,1,1,1,1},  //13
        {1,1,1,1,0,0,0,0,0,0,1,1,1,0,0,1,0,0,0,0,0,0,0,1,1,1,1,1},  //14
        {1,1,1,1,0,0,2,1,0,0,1,0,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1},  //15
        {1,1,1,1,0,0,0,1,0,2,0,0,0,2,0,1,1,1,1,1,1,1,1,1,1,1,1,1},  //16
        {1,1,1,1,1,1,1,1,0,2,1,7,1,2,0,1,1,1,1,1,1,1,1,1,1,1,1,1},  //17
        {1,1,1,1,1,1,1,1,0,0,1,1,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1},  //18
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}   //19
    };

    // office, guard stands here (column, row)
    public final int guardColumn = 11;
    public final int guardRow = 17;

    // left and right office doors (column, row).
    public final int[][] doorTiles = { 
        {10, 16}, //this ones left door
        {12, 16} //this ones right door
    };

    public final int[][] cameraTiles = {
            {8,2}, {7,3}, {3,10}, {9,16}, {9,17}, {6,15}, {13,16}, {13,17}, {1,7}, {6,10}, {21,4}
        //   1A     1B      1C      2A      2B      3       4A        4B      5       6       7
    };
    
    public final int[][] PirateCoveCurtain = {
        {3, 9}
    };

    public final int[] freddySpawn = {11, 1};
    public final int[] bonnieSpawn = {10, 1};
    public final int[] chicaSpawn = {12, 1};
    public final int[] foxySpawn = {14, 9};

    // Each path is {column, row} waypoints. First waypoint = spawn.
    // Last waypoint = the tile where the animatronic transitions to APPROACH.

    // Bonnie: 1A -> 1B -> 5 -> 2A -> 2B -> Left Door
    public final int[][] bonniePath = {
        {8, 2},   // 1A - Show Stage
        {8, 3},   // move down
        {7, 3},   // 1B - Dining Area
        {7, 4},   // move down
        {4, 4},   // move left
        {4, 5},   // move down
        {1, 5},   // move left
        {1, 7},   // 5 - Backstage
        {1, 9},   // move down
        {3, 9},   // move right
        {3, 10},  // 1C - Pirate Cove
        {4, 10},  // move right
        {4, 14},  // move down
        {9, 14},  // move right into West Hall
        {9, 16},  // 2A - West Hall
        {9, 17},  // 2B - West Hall Corner
        {10, 16}  // Left Door
    };

    // Chica: 1A -> 1B -> 7 -> 4A -> 4B -> Right Door
    public final int[][] chicaPath = {
        {8, 2},   // 1A - Show Stage
        {8, 3},   // move down
        {7, 3},   // 1B - Dining Area
        {15, 3},  // move right
        {20, 3},  // move right
        {21, 4},  // 7 - Restrooms
        {21, 5},  // move down
        {22, 5},  // move right
        {22, 8},  // move down
        {21, 8},  // move left
        {21, 9},  // move down
        {20, 9},  // move left
        {20, 12}, // move down
        {19, 12}, // move left
        {16, 12}, // move left
        {16, 14}, // move down
        {13, 14}, // move left to East Hall
        {13, 16}, // 4A - East Hall
        {13, 17}, // 4B - East Hall Corner
        {12, 16}  // Right Door
    };

    // Freddy: 1A -> 1B -> 7 -> 6 -> 4A -> 4B -> Right Door
    public final int[][] freddyPath = {
        {8, 2},   // 1A - Show Stage
        {8, 3},   // move down
        {7, 3},   // 1B - Dining Area
        {15, 3},  // move right
        {20, 3},  // move right
        {21, 4},  // 7 - Restrooms
        {21, 5},  // move down
        {22, 5},  // move right
        {22, 8},  // move down
        {21, 8},  // move left
        {21, 9},  // move down
        {20, 9},  // move left
        {20, 12}, // move down
        {19, 12}, // move left
        {16, 12}, // move left
        {16, 10}, // move up
        {14, 10}, // move left
        {14, 8},  // move up
        {9, 8},   // move left
        {9, 9},   // move down
        {6, 9},   // move left
        {6, 10},  // 6 - Kitchen
        {5, 10},  // move left
        {5, 14},  // move down
        {13, 14}, // move right to East Hall
        {13, 16}, // 4A - East Hall
        {13, 17}, // 4B - East Hall Corner
        {12, 16}  // Right Door
    };

    // Foxy: 1C -> 2A -> 2B -> Left Door
    public final int[][] foxyPath = {
        {3, 10},  // 1C - Pirate Cove
        {4, 10},  // move right
        {4, 14},  // move down
        {9, 14},  // move right into West Hall
        {9, 16},  // 2A - West Hall
        {9, 17},  // 2B - West Hall Corner
        {10, 16}  // Left Door
    };

    /*

    public final int[][] goldenfreddy = {
        {16, 11} 
    };
    wait for now because golden freddy is not implemented yet, but this is where he would spawn and attack from
    esmeringhoy

    */

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