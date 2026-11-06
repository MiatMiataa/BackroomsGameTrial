package GameTrial.entity;

import GameTrial.assets.TextureSet;
import GameTrial.world.GameMap;
import java.util.Arrays;
import java.util.List;

// The Usher: walks a loop through the patrol tiles, is nearly blind (notices only movement up close),
// chases when it notices you, and attacks when it touches you.
public class Entity extends Sprite {
    private enum State { PATROL, CHASE, ATTACK, VANSIH }

    private static final double PATROL_SPEED = 1.2;
    private static final double CHASE_SPEED = 3.35;       // the player walks at 3.35
    private static final double CATCH_DISTANCE = 36;
    private static final double SIGHT_DISTANCE = 4.5 * GameMap.TILE_SIZE;
    private static final int GIVE_UP_STEPS = 12;         // path steps from the player before it gives up chasing

    private static final int TICKS_PER_WALK_FRAME = 8;
    private static final int TICKS_PER_ATTACK_FRAME = 10;
    private static final int TICKS_PER_VANISH_FRAME = 6;

    private static final int[] COLUMN_STEPS = {1, -1, 0, 0};
    private static final int[] ROW_STEPS = {0, 0, 1, -1};

    private final TextureSet textures;
    private final List<int[]> patrolTiles;   // {column, row} pairs, visited in order, then repeated
    private int patrolIndex = 0;

    private State state = State.PATROL;
    private int animationTicks = 0;
    private boolean attackFinished = false;

    private double facingX = 0;
    private double facingY = 1;
    private double targetX;
    private double targetY;

    // Steps to the current goal tile for every tile, [row][column]. -1 = wall or cannot reach.
    private int[][] distanceToGoal;
    private int goalColumn = -1;
    private int goalRow = -1;

    public Entity(double positionX, double positionY, TextureSet textures, List<int[]> patrolTiles) {
        super(positionX, positionY, textures.usherPatrol[0][0], 0.85);
        this.textures = textures;
        this.patrolTiles = patrolTiles;
        targetX = positionX;
        targetY = positionY;
    }

    public boolean isAttacking() {
        return state == State.ATTACK;
    }

    public boolean isAttackFinished() {
        return attackFinished;
    }

    public void update(GameMap gameMap, Player player, boolean playerIsMoving) {
        animationTicks++;
        double distanceToPlayer = Math.hypot(player.positionX - positionX, player.positionY - positionY);

        if (state == State.ATTACK) {
            faceToward(player.positionX, player.positionY);
            if (animationTicks >= textures.usherAttack.length * TICKS_PER_ATTACK_FRAME) attackFinished = true;
            updateTexture(player);
            return;
        }

        if (distanceToPlayer < CATCH_DISTANCE) {
            state = State.ATTACK;
            animationTicks = 0;
            updateTexture(player);
            return;
        }

        if (state == State.PATROL) {
            if (playerIsMoving && distanceToPlayer <= SIGHT_DISTANCE
                && hasLineOfSight(gameMap, player.positionX, player.positionY)) {
                state = State.CHASE;
            }
        }

        if (state == State.CHASE) {
            setGoal(gameMap, toTile(player.positionX), toTile(player.positionY));
            int stepsAway = distanceToGoal[toTile(positionY)][toTile(positionX)];
            if (stepsAway < 0 || stepsAway > GIVE_UP_STEPS) {
                state = State.PATROL;
            } else {
                walkAlongGrid(stepsAway, CHASE_SPEED, player.positionX, player.positionY);
            }
        }

        if (state == State.VANSIH) {
            if (animationTicks >= textures.usherVanish.length * TICKS_PER_VANISH_FRAME) {
                state = State.PATROL;
                animationTicks = 0;
                patrolIndex = (patrolIndex + 1) % patrolTiles.size();
            }
            updateTexture(player);
            return;
        }

        if (state == State.PATROL) {
            int[] patrolTile = patrolTiles.get(patrolIndex);
            setGoal(gameMap, patrolTile[0], patrolTile[1]);
            double goalCenterX = (patrolTile[0] + 0.5) * GameMap.TILE_SIZE;
            double goalCenterY = (patrolTile[1] + 0.5) * GameMap.TILE_SIZE;
            if (Math.hypot(goalCenterX - positionX, goalCenterY - positionY) < 1) {
                patrolIndex = (patrolIndex + 1) % patrolTiles.size();
            } else {
                int stepsAway = distanceToGoal[toTile(positionY)][toTile(positionX)];
                if (stepsAway >= 0) walkAlongGrid(stepsAway, PATROL_SPEED, goalCenterX, goalCenterY);
            }
        }

        updateTexture(player);
    }

    private void updateTexture(Player player) {
        if (state == State.ATTACK) {
            int frame = Math.min(animationTicks / TICKS_PER_ATTACK_FRAME, textures.usherAttack.length - 1);
            texture = textures.usherAttack[frame];
        } else if (state == State.CHASE) {
            int frameDuration = 3;
            texture = textures.usherChase[(animationTicks / frameDuration) % textures.usherChase.length];
        } else {
            texture = textures.usherPatrol[viewSector(player)][(animationTicks / TICKS_PER_WALK_FRAME) % 4];
        }
        if (state == State.VANSIH) {
            int frame = Math.min(animationTicks / TICKS_PER_VANISH_FRAME, textures.usherVanish.length - 1);
            texture = textures.usherVanish[frame];
        }
    }

    private int viewSector(Player player) {
        double toPlayerX = player.positionX - positionX;
        double toPlayerY = player.positionY - positionY;
        double length = Math.hypot(toPlayerX, toPlayerY);
        if (length < 0.001) return 0;
        toPlayerX /= length;
        toPlayerY /= length;

        double handedness = Math.signum(player.directionX * player.cameraPlaneY - player.directionY * player.cameraPlaneX);
        double rightX = handedness * toPlayerY; 
        double rightY = handedness * -toPlayerX;

        double towardViewer = facingX * toPlayerX + facingY * toPlayerY;
        double towardRight = facingX * rightX + facingY * rightY;
        int sector = (int) Math.round(Math.atan2(towardRight, towardViewer) / (Math.PI / 4));
        return ((sector % 8) + 8) % 8;
    }

    private boolean hasLineOfSight(GameMap gameMap, double targetPixelX, double targetPixelY) {
        double deltaX = targetPixelX - positionX;
        double deltaY = targetPixelY - positionY;
        int samples = (int) (Math.hypot(deltaX, deltaY) / 8) + 1;
        for (int sample = 1; sample < samples; sample++) {
            if (gameMap.isWall(positionX + deltaX * sample / samples, positionY + deltaY * sample / samples)) return false;
        }
        return true;
    }

    private void walkAlongGrid(int stepsAway, double speed, double finalX, double finalY) {
        if (stepsAway == 0) {
            targetX = positionX;
            targetY = positionY;
            moveToward(finalX, finalY, speed);
            return;
        }
        if (Math.hypot(targetX - positionX, targetY - positionY) < 0.001) chooseNextTarget(stepsAway);
        moveToward(targetX, targetY, speed);
    }

    private void chooseNextTarget(int currentSteps) {
        int column = toTile(positionX);
        int row = toTile(positionY);
        int bestSteps = currentSteps;
        for (int direction = 0; direction < 4; direction++) {
            int nextColumn = column + COLUMN_STEPS[direction];
            int nextRow = row + ROW_STEPS[direction];
            int nextSteps = distanceToGoal[nextRow][nextColumn];
            if (nextSteps >= 0 && nextSteps < bestSteps) {
                bestSteps = nextSteps;
                targetX = (nextColumn + 0.5) * GameMap.TILE_SIZE;
                targetY = (nextRow + 0.5) * GameMap.TILE_SIZE;
            }
        }
    }

    private void moveToward(double goalX, double goalY, double speed) {
        double deltaX = goalX - positionX;
        double deltaY = goalY - positionY;
        double distance = Math.hypot(deltaX, deltaY);
        if (distance < 0.001) return;
        facingX = deltaX / distance;
        facingY = deltaY / distance;
        if (distance <= speed) {
            positionX = goalX;
            positionY = goalY;
        } else {
            positionX += facingX * speed;
            positionY += facingY * speed;
        }
    }

    private void faceToward(double pixelX, double pixelY) {
        double deltaX = pixelX - positionX;
        double deltaY = pixelY - positionY;
        double distance = Math.hypot(deltaX, deltaY);
        if (distance < 0.001) return;
        facingX = deltaX / distance;
        facingY = deltaY / distance;
    }

    private void setGoal(GameMap gameMap, int column, int row) {
        if (distanceToGoal != null && column == goalColumn && row == goalRow) return;
        goalColumn = column;
        goalRow = row;

        int rowCount = gameMap.getRowsCount();
        int columnCount = gameMap.getColumnCount();
        if (distanceToGoal == null) distanceToGoal = new int[rowCount][columnCount];
        for (int[] rowDistances : distanceToGoal) Arrays.fill(rowDistances, -1);

        int[] queue = new int[rowCount * columnCount];
        int head = 0;
        int tail = 0;
        distanceToGoal[row][column] = 0;
        queue[tail++] = row * columnCount + column;

        while (head < tail) {
            int index = queue[head++];
            int currentColumn = index % columnCount;
            int currentRow = index / columnCount;
            for (int direction = 0; direction < 4; direction++) {
                int nextColumn = currentColumn + COLUMN_STEPS[direction];
                int nextRow = currentRow + ROW_STEPS[direction];
                if (gameMap.getTile(nextColumn, nextRow) == 1) continue;
                if (distanceToGoal[nextRow][nextColumn] != -1) continue;
                distanceToGoal[nextRow][nextColumn] = distanceToGoal[currentRow][currentColumn] + 1;
                queue[tail++] = nextRow * columnCount + nextColumn;
            }
        }
    }

    private static int toTile(double pixelPosition) {
        return (int) Math.floor(pixelPosition / GameMap.TILE_SIZE);
    }
}