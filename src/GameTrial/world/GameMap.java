package GameTrial.world;

import java.util.Arrays;
import java.util.List;

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
        {1,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1},  //9
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

    // Doors and curtain. They are not tiles: entity movement keeps using tileGrid,
    // so the animatronic routes never get blocked by a closed door.
    public final SlidingBarrier leftDoorBarrier;
    public final SlidingBarrier rightDoorBarrier;
    public final SlidingBarrier pirateCoveCurtain;

    public final int[] freddySpawn = {11, 1};
    public final int[] bonnieSpawn = {10, 1};
    public final int[] chicaSpawn = {12, 1};
    public final int[] foxySpawn = {2, 9};

    // Each path is {column, row} waypoints. First waypoint = spawn.
    // Last waypoint = the tile where the animatronic transitions to APPROACH.
    //
    // Rules every path must follow (checked by validatePaths() at startup):
    //   - every waypoint is on a non-wall tile
    //   - every segment runs along a single axis (no diagonals, they cut wall corners)
    //   - every tile a segment crosses is a non-wall tile

    // Bonnie: 1A (Show Stage) -> 1B (Dining) -> 5 (Backstage) -> back -> 1C (Pirate Cove)
    //         -> West Hall -> 2A -> 2B -> Left Door
    public final int[][] bonniePath = {
        {10, 1},  // spawn - Show Stage
        {10, 2},  // move down
        {8, 2},   // 1A - Show Stage
        {8, 3},   // move down
        {7, 3},   // 1B - Dining Area
        {4, 3},   // move left
        {4, 4},   // move down
        {2, 4},   // move left
        {2, 5},   // move down
        {1, 5},   // move left
        {1, 7},   // 5 - Backstage
        {1, 5},   // walk the route back out
        {2, 5},
        {2, 4},
        {4, 4},
        {4, 5},   // move down
        {8, 5},   // move right
        {8, 10},  // move down
        {3, 10},  // 1C - Pirate Cove
        {8, 10},  // walk back out
        {8, 14},  // move down (West Hall corridor)
        {9, 14},  // move right
        {9, 16},  // 2A - West Hall
        {9, 17},  // 2B - West Hall Corner
        {9, 16},  // step back into the hall
        {10, 16}  // Left Door
    };

    // Chica: 1A (Show Stage) -> 1B (Dining) -> 7 (Restrooms) -> 4A -> 4B -> Right Door
    public final int[][] chicaPath = {
        {12, 1},  // spawn - Show Stage
        {12, 2},  // move down
        {8, 2},   // 1A - Show Stage
        {8, 3},   // move down
        {7, 3},   // 1B - Dining Area
        {15, 3},  // move right
        {19, 3},  // move right
        {19, 4},  // move down
        {20, 4},  // move right
        {21, 4},  // 7 - Restrooms
        {22, 4},  // move right
        {22, 8},  // move down
        {21, 8},  // move left
        {21, 9},  // move down
        {22, 9},  // move right
        {22, 4},  // walk the route back out
        {20, 4},
        {19, 4},
        {19, 3},
        {13, 3},  // move left into the East Hall corridor
        {13, 14}, // move down
        {13, 16}, // 4A - East Hall
        {13, 17}, // 4B - East Hall Corner
        {13, 16}, // step back into the hall
        {12, 16}  // Right Door
    };

    // Freddy: 1A -> 1B -> 7 (Restrooms) -> 6 (Kitchen) -> 4A -> 4B -> Right Door
    public final int[][] freddyPath = {
        {11, 1},  // spawn - Show Stage
        {11, 2},  // move down
        {8, 2},   // 1A - Show Stage
        {8, 3},   // move down
        {7, 3},   // 1B - Dining Area
        {15, 3},  // move right
        {19, 3},  // move right
        {19, 4},  // move down
        {20, 4},  // move right
        {21, 4},  // 7 - Restrooms
        {22, 4},  // move right
        {22, 8},  // move down
        {21, 8},  // move left
        {21, 9},  // move down
        {22, 9},  // move right
        {22, 4},  // walk the route back out
        {20, 4},
        {19, 4},
        {19, 3},
        {13, 3},  // move left
        {13, 10}, // move down
        {6, 10},  // 6 - Kitchen
        {13, 10}, // walk back out
        {13, 14}, // move down (East Hall corridor)
        {13, 16}, // 4A - East Hall
        {13, 17}, // 4B - East Hall Corner
        {13, 16}, // step back into the hall
        {12, 16}  // Right Door
    };

    // Foxy: 1C -> West Hall -> 2A -> 2B -> Left Door
    // Foxy's first waypoint is Pirate Cove, and Game.cameraAnimatronic() relies on that.
    public final int[][] foxyPath = {
        {2, 9},  // 1C - Pirate Cove
        {3, 9},  // move right
        {8, 9},  // move right
        {8, 14},  // move down
        {9, 14},  // move right into West Hall
        {9, 16},  // 2A - West Hall
        {9, 17},  // 2B - West Hall Corner
        {9, 16},  // step back into the hall
        {10, 16}  // Left Door
    };

    public GameMap() {
        // Both doors start open, the curtain starts closed.
        leftDoorBarrier = new SlidingBarrier(doorTiles[0][0], doorTiles[0][1],
            SlidingBarrier.BarrierType.OFFICE_DOOR, 1.0);
        rightDoorBarrier = new SlidingBarrier(doorTiles[1][0], doorTiles[1][1],
            SlidingBarrier.BarrierType.OFFICE_DOOR, 1.0);
        pirateCoveCurtain = new SlidingBarrier(PirateCoveCurtain[0][0], PirateCoveCurtain[0][1],
            SlidingBarrier.BarrierType.PIRATE_COVE_CURTAIN, 0.0);
        validatePaths();
    }

    public SlidingBarrier getBarrierAt(int column, int row) {
        if (leftDoorBarrier.isAt(column, row)) {
            return leftDoorBarrier;
        }
        if (rightDoorBarrier.isAt(column, row)) {
            return rightDoorBarrier;
        }
        if (pirateCoveCurtain.isAt(column, row)) {
            return pirateCoveCurtain;
        }
        return null;
    }

    public List<SlidingBarrier> barriers() {
        return Arrays.asList(leftDoorBarrier, rightDoorBarrier, pirateCoveCurtain);
    }

    public void updateBarriers() {
        leftDoorBarrier.update();
        rightDoorBarrier.update();
        pirateCoveCurtain.update();
    }

    /**
     * True when a straight waypoint-to-waypoint move stays on open tiles.
     * Segments have to run along one axis; a diagonal segment cuts wall corners
     * even when both of its end tiles happen to be open.
     */
    public boolean isPathSegmentClear(int[] startWaypoint, int[] endWaypoint) {
        if (startWaypoint == null || endWaypoint == null) {
            return false;
        }
        int startColumn = startWaypoint[0];
        int startRow = startWaypoint[1];
        int endColumn = endWaypoint[0];
        int endRow = endWaypoint[1];

        if (getTile(startColumn, startRow) == 1 || getTile(endColumn, endRow) == 1) {
            return false;
        }
        int columnStep = Integer.compare(endColumn, startColumn);
        int rowStep = Integer.compare(endRow, startRow);
        if (columnStep != 0 && rowStep != 0) {
            return false;  // diagonal
        }

        int column = startColumn;
        int row = startRow;
        while (column != endColumn || row != endRow) {
            column += columnStep;
            row += rowStep;
            if (getTile(column, row) == 1) {
                return false;
            }
        }
        return true;
    }

    private void validatePaths() {
        validatePath("bonniePath", bonniePath);
        validatePath("chicaPath", chicaPath);
        validatePath("freddyPath", freddyPath);
        validatePath("foxyPath", foxyPath);
        System.out.println("[GameMap] Path validation passed: every waypoint and segment is on open tiles.");
    }

    private void validatePath(String pathName, int[][] path) {
        for (int waypointIndex = 0; waypointIndex < path.length; waypointIndex++) {
            int column = path[waypointIndex][0];
            int row = path[waypointIndex][1];
            if (column < 0 || column >= getColumnCount() || row < 0 || row >= getRowsCount()) {
                throw new IllegalStateException(pathName + " waypoint " + waypointIndex + " "
                    + describeWaypoint(path[waypointIndex]) + " is outside the map");
            }
            if (getTile(column, row) == 1) {
                throw new IllegalStateException(pathName + " waypoint " + waypointIndex + " "
                    + describeWaypoint(path[waypointIndex]) + " is inside a wall tile");
            }
        }

        for (int segmentIndex = 0; segmentIndex < path.length - 1; segmentIndex++) {
            int[] start = path[segmentIndex];
            int[] end = path[segmentIndex + 1];
            if (start[0] != end[0] && start[1] != end[1]) {
                throw new IllegalStateException(pathName + " segment " + segmentIndex + " "
                    + describeWaypoint(start) + "->" + describeWaypoint(end)
                    + " is diagonal; every segment must run along one axis");
            }
            if (!isPathSegmentClear(start, end)) {
                throw new IllegalStateException(pathName + " segment " + segmentIndex + " "
                    + describeWaypoint(start) + "->" + describeWaypoint(end)
                    + " hits wall tile " + describeFirstBlockedTile(start, end));
            }
        }
    }

    private String describeFirstBlockedTile(int[] start, int[] end) {
        int columnStep = Integer.compare(end[0], start[0]);
        int rowStep = Integer.compare(end[1], start[1]);
        int column = start[0];
        int row = start[1];
        while (column != end[0] || row != end[1]) {
            column += columnStep;
            row += rowStep;
            if (getTile(column, row) == 1) {
                return describeTile(column, row);
            }
        }
        return describeTile(end[0], end[1]);
    }

    private String describeWaypoint(int[] waypoint) {
        return "{" + waypoint[0] + "," + waypoint[1] + "}";
    }

    private String describeTile(int column, int row) {
        return "(" + column + "," + row + ")";
    }

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
