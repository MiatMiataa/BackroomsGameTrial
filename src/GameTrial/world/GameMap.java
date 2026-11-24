package GameTrial.world;

public class GameMap {
    public static final int TILE_SIZE = 64;  //each tile is 64px

    // 1 = wall, 0 = floor
    private final int[][] tileGrid = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},  //0
        {1,1,1,1,1,1,1,1,0,0,5,0,6,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1},  //1
        {1,1,1,1,1,1,1,1,2,0,0,4,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1},  //2
        {1,1,1,1,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1},  //3
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,1,1,1,1,1},  //4
        {1,0,5,1,0,0,5,6,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,4,0,0,0,1},  //5
        {1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,6,1,0,0,0,1},  //6
        {1,2,5,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,1,1},  //7
        {1,1,1,1,0,0,5,0,0,6,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,1,1},  //8
        {1,0,8,8,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,6,0,0,0,0,1},  //9
        {1,0,0,3,8,0,2,0,0,4,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,1},  //10
        {1,1,1,1,1,1,1,1,0,0,1,1,1,4,0,1,1,0,1,1,1,1,1,1,1,1,1,1},  //11
        {1,1,1,1,1,1,1,1,0,8,1,1,1,6,0,1,0,0,0,0,0,0,2,1,1,1,1,1},  //12
        {1,1,1,1,0,5,0,1,5,0,1,1,1,0,0,1,0,0,0,0,6,4,0,1,1,1,1,1},  //13
        {1,1,1,1,0,0,0,0,0,0,1,1,1,6,0,1,0,0,0,0,0,0,0,1,1,1,1,1},  //14
        {1,1,1,1,0,0,2,1,0,5,1,0,1,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1},  //15
        {1,1,1,1,0,0,0,1,8,2,0,0,0,2,0,1,1,1,1,1,1,1,1,1,1,1,1,1},  //16
        {1,1,1,1,1,1,1,1,0,2,1,7,1,2,0,1,1,1,1,1,1,1,1,1,1,1,1,1},  //17
        {1,1,1,1,1,1,1,1,0,5,1,1,1,4,6,1,1,1,1,1,1,1,1,1,1,1,1,1},  //18
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
            {8,2}, {7,3}, {6,10}, {9,16}, {9,17}, {6,15}, {13,16}, {13,17}, {1,7}, {22,12}, {21,4}
        //   1A     1B      1C      2A      2B      3       4A        4B      5       6       7
    };
    
    public final int[][] PirateCoveCurtain = {
        {3, 9}
    };

    public final int[] freddySpawn = {11, 2};
    public final int[] bonnieSpawn = {10, 1};
    public final int[] chicaSpawn = {13, 1};
    public final int[] foxySpawn = {2, 9};

    // Each path is {column, row} waypoints. First waypoint = spawn.
    // Last waypoint = the tile where the animatronic transitions to APPROACH.

    public final int[][] bonniePath = {
        {10,1},{10,2},{8,2},{8,3},{7,3},{4,3},{4,4},{2,4},{2,5},{1,5},
             {1,7},                                  // 5 Backstage
             {1,5},{2,5},{2,4},{4,4},{4,5},{8,5},{8,10},
             {3,10},                                 // 1C Pirate Cove
             {8,10},{8,14},{9,14},{9,16},{9,17},{10,16}   // 2A,2B, left door
    };

    public final int[][] chicaPath = {
        {12,1},{12,2},{8,2},{8,3},{7,3},{15,3},{19,3},{19,4},{20,4},
        {21,4},{22,4},{22,8},{21,8},{21,9},     // 7 Restrooms loop
        {22,9},{22,4},{20,4},{19,4},{19,3},{13,3},
        {13,14},{13,16},{13,17},{12,16}};         // 4A,4B, right door

    public final int[][] freddyPath = {
        {11,1},{11,2},{8,2},{8,3},{7,3},{15,3},{19,3},{19,4},{20,4},
        {21,4},{22,4},{22,8},{21,8},{21,9},{22,9},{22,4},{20,4},{19,4},{19,3},
        {13,3},{13,10},{6,10},                  // 6 Kitchen
        {13,10},{13,14},{13,16},{13,17},{12,16}};

    public final int[][] foxyPath = {
        {3,10},{4,10},{8,10},{8,14},{9,14},{9,16},{9,17},{10,16}
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