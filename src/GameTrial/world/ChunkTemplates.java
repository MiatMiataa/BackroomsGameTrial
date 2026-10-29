package GameTrial.world;

import java.util.Random;

public class ChunkTemplates {
    // Inside of a chunk: 7 x 7 tiles. 1 = wall, 0 = open.
    // RULE: rows 2 and 3 and columns 2 and 3 (counting from 0) must be all 0.
    // That clear cross joins the four doors, so a template can never trap the player.
    private static final int LANE_START = 2;
    private static final int LANE_END = 3;

    private static class Template {
        final String name;
        final int weight;
        final int[][] tiles;

        Template(String name, int weight, int[][] tiles) {
            this.name = name;
            this.weight = weight;
            this.tiles = tiles;
        }
    }

    // Weight = chance of being picked. 10 is common, 1 is rare. Add your own templates here.
    private static final Template[] TEMPLATES = {
        new Template("empty room", 8, new int[][] {
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0}
        }),
        new Template("four pillars", 10, new int[][] {
            {0,0,0,0,0,0,0},
            {0,1,0,0,0,1,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,1,0,0,0,1,0},
            {0,0,0,0,0,0,0}
        }),
        new Template("L wall", 6, new int[][] {
            {1,1,0,0,0,0,0},
            {1,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,1},
            {0,0,0,0,0,1,1}
        }),
        new Template("corners", 4, new int[][] {
            {1,1,0,0,0,1,1},
            {1,0,0,0,0,0,1},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {1,0,0,0,0,0,1},
            {1,1,0,0,0,1,1}
        }),
        new Template("two blocks", 6, new int[][] {
            {0,0,0,0,0,1,1},
            {0,0,0,0,0,1,1},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0},
            {1,1,0,0,0,0,0},
            {1,1,0,0,0,0,0}
        })
    };

    // Hand-made rooms for the start and the exit. Same rule. Draw your own later.
    public static final int[][] SPAWN_ROOM = {
        {0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0}
    };

    public static final int[][] EXIT_ROOM = {
        {0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0},
        {0,0,0,0,0,0,0}
    };

    public static int[][] pickRandom(Random random) {
        int totalWeight = 0;
        for (Template template : TEMPLATES) totalWeight += template.weight;

        int roll = random.nextInt(totalWeight);
        for (Template template : TEMPLATES) {
            roll -= template.weight;
            if (roll < 0) return template.tiles;
        }
        return TEMPLATES[0].tiles;
    }

    // Stops the game with the template's name if a rule is broken.
    public static void checkAll(int expectedSize) {
        for (Template template : TEMPLATES) checkTemplate(template.name, template.tiles, expectedSize);
        checkTemplate("spawn room", SPAWN_ROOM, expectedSize);
        checkTemplate("exit room", EXIT_ROOM, expectedSize);
    }

    private static void checkTemplate(String name, int[][] tiles, int expectedSize) {
        if (tiles.length != expectedSize) {
            throw new IllegalStateException("Template '" + name + "' must have " + expectedSize + " rows");
        }
        for (int row = 0; row < expectedSize; row++) {
            if (tiles[row].length != expectedSize) {
                throw new IllegalStateException("Template '" + name + "' row " + row + " must have " + expectedSize + " tiles");
            }
            for (int column = 0; column < expectedSize; column++) {
                int tile = tiles[row][column];
                if (tile != 0 && tile != 1) {
                    throw new IllegalStateException("Template '" + name + "' has a tile that is not 0 or 1 at row " + row + ", column " + column);
                }
                boolean isInLane = (row >= LANE_START && row <= LANE_END) || (column >= LANE_START && column <= LANE_END);
                if (isInLane && tile != 0) {
                    throw new IllegalStateException("Template '" + name + "' blocks the clear cross at row " + row + ", column " + column);
                }
            }
        }
    }
}