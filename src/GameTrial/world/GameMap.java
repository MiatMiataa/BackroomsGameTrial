package GameTrial.world;

public class GameMap {
    public static final int TILE_SIZE = 64;

    // 1 = wall, 0 = floor
    private final int[][] tileGrid = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},  //1
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},//2
        {1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1},//3
        {1,1,1,1,1,1,1,1,2,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1},//4
        {1,1,1,1,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1},//
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,1},//
        {1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0},//
        {1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1},//
        {1,2,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1},//
        {1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1},//
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0},//
        {1,0,0,1,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1},//
        {1,1,1,1,1,1,1,0,0,1,1,1,1,1,0,0,1,1,0,1,1,1,1,1},//
        {1,1,1,1,1,1,1,0,0,1,1,1,1,1,0,0,1,0,0,0,0,0,2,1},//
        {1,1,1,0,0,0,1,0,0,1,1,1,1,1,0,0,1,0,0,0,0,0,0,1},//
        {1,1,1,0,0,0,0,0,0,1,1,1,1,1,0,0,1,0,0,0,0,0,0,1},//
        {1,1,1,0,0,2,1,0,0,1,0,0,0,1,0,0,1,1,1,1,1,1,1,1},//
        {1,1,1,0,0,0,1,0,2,0,0,0,0,0,2,0,1,1,1,1,1,1,1,1},//
        {1,1,1,1,1,1,1,0,2,1,0,7,0,1,2,0,1,1,1,1,1,1,1,1},//
        {1,1,1,1,1,1,1,0,0,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1},//
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1} //
    };

    // office, guard stands here (column, row)
    public final int guardColumn = 11;
    public final int guardRow = 24;

    // left and right office doors (column, row). open for now.
    public final int[][] doorTiles = { 
        {7, 23}, //this ones left door
        {11, 23} //this ones right door
    };

    // (column, row): Party3, Party1, Party4, Party2, CentralHall, MainHall, Dining, Backstage, Kitchen
    public final int[][] cameraTiles = {
        {5, 25},
        {13, 15},
        {15, 9},
        {15, 14},
        {9, 9},
        {1, 6},
        {43, 6},
        {33, 1},
        {31, 21}
    };

    public final int[] freddySpawn = {21, 21};
    public final int[] bonnieSpawn = {43, 6};
    public final int[] chicaSpawn = {1, 6};
    public final int[] foxySpawn = {14, 15};

    // Each path is {column, row} waypoints. First waypoint = spawn.
    // Last waypoint = the tile where the animatronic transitions to APPROACH.

        public final int[][] freddyPath = {
        {14, 15},   // spawn (Party 1)
        {10, 15},   // move left to corridor
        {10, 22},   // move down corridor
        {9,  22}    // front of guard (defend with flashlight)
    };

    public final int[][] bonniePath = {
        {43, 6},    // Dining
        {9,  6},    // move left along top hallway
        {9,  22},   // move down central corridor
        {7,  23}    // left door
    };

    public final int[][] chicaPath = {
        {15, 10},   // Party 4 (spawn)
        {10, 10},   // move left to corridor
        {10, 22},   // move down central corridor
        {11, 23}    // right door
    };

    public final int[][] foxyPath = {
        {1,  6},    // Pirate Cove
        {9,  6},    // move right along top hallway
        {9,  22},   // move down central corridor
        {7,  23}    // left door
    };

    /*

    public final int[][] goldenfreddy = {
        {8, 22} 
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