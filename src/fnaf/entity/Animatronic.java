package fnaf.entity;

import fnaf.assets.AnimatronicTextures;
import fnaf.world.GameMap;
import fnaf.assets.Texture;

public abstract class Animatronic extends Sprite {
    public enum State { PATROL, APPROACH, ATTACK, RETREAT }

    protected static final int ATTACK_DURATION_TICKS = 60;
    protected static final double ARRIVAL_RADIUS = 4.0;

    // How many ticks an animatronic may stand still on one waypoint before it gives up on it.
    protected static final int MAX_BLOCKED_TICKS = 60;

    protected final java.util.Random random = new java.util.Random();

    protected final AnimatronicTextures textures;
    protected final int[][] path;
    protected final int stepTicks;
    protected final double walkSpeed;
    protected final double sprintSpeed;

    protected State state = State.PATROL;
    protected int pathIndex = 0;

    // Waypoint a closed door sends the animatronic back to. FNAF 1 never sends
    // anybody all the way home, so this is not always 0.
    protected int retreatStopIndex = 0;
    protected int stepTimer = 0;
    protected int animationTicks = 0;
    protected boolean attackFinished = false;
    protected boolean sprinting = false;
    protected boolean isMoving = false;
    protected int aiLevel;
    protected int movementOpportunityIntervalInTicks;
    protected int ticksSinceLastMovementOpportunity = 0;

    // Ticks spent failing to make progress toward the current waypoint.
    protected int blockedTicks = 0;

    protected double facingX = 0, facingY = 1;

    protected Animatronic(int[][] path, double walkSpeed, double sprintSpeed,
                          int stepTicks, AnimatronicTextures textures, double heightScale) {
        super((path[0][0] + 0.5) * GameMap.TILE_SIZE,
              (path[0][1] + 0.5) * GameMap.TILE_SIZE,
              textures.framesByOrientation[0], heightScale);
        this.path = path;
        this.walkSpeed = walkSpeed;
        this.sprintSpeed = sprintSpeed;
        this.stepTicks = stepTicks;
        this.textures = textures;
    }

    public final void tick(GameMap map, Player player, AnimatronicContext animatronicContext) {
        animationTicks++;
        isMoving = false;
        switch (state) {
            case PATROL:
                updatePatrol(map, player, animatronicContext);
                break;
            case APPROACH:
                updateApproach(player, animatronicContext);
                break;
            case ATTACK:
                updateAttack(player);
                break;
            case RETREAT:
                updateRetreat(map, player, animatronicContext);
                break;
        }
        texture = pickTexture(player);
    }

    protected abstract void updatePatrol(GameMap map, Player player, AnimatronicContext animatronicContext);
    protected abstract void updateApproach(Player player, AnimatronicContext animatronicContext);
    protected abstract void updateRetreat(GameMap map, Player player, AnimatronicContext animatronicContext);

    protected void updateAttack(Player player) {
        faceToward(player.positionX, player.positionY);
        if (animationTicks >= ATTACK_DURATION_TICKS) {
            attackFinished = true;
        }
    }

    // pathing stuff

    protected double currentSpeed() {
        return sprinting && sprintSpeed > 0 ? sprintSpeed : walkSpeed;
    }

    protected void moveAlongPath(GameMap map) {
        if (pathIndex >= path.length) {
            return;
        }
        int[] waypoint = path[pathIndex];
        double targetX = (waypoint[0] + 0.5) * GameMap.TILE_SIZE;
        double targetY = (waypoint[1] + 0.5) * GameMap.TILE_SIZE;

        if (Math.hypot(targetX - positionX, targetY - positionY) < 0.001) {
            blockedTicks = 0;
            return;
        }

        double step = currentSpeed();
        if (step <= 0) {
            return;
        }

        // One axis at a time, longest axis first. A wall in front of one axis must
        // not cancel the whole step, otherwise the animatronic can freeze forever,
        // and never moving on both axes in the same tick keeps it from cutting a
        // corner diagonally when it is still a few pixels off the last waypoint.
        boolean movedOnXAxis = false;
        boolean movedOnYAxis = false;
        if (Math.abs(targetX - positionX) >= Math.abs(targetY - positionY)) {
            movedOnXAxis = stepAlongAxis(map, targetX - positionX, 0, step);
            if (!movedOnXAxis) {
                movedOnYAxis = stepAlongAxis(map, 0, targetY - positionY, step);
            }
        } else {
            movedOnYAxis = stepAlongAxis(map, 0, targetY - positionY, step);
            if (!movedOnYAxis) {
                movedOnXAxis = stepAlongAxis(map, targetX - positionX, 0, step);
            }
        }
        isMoving = movedOnXAxis || movedOnYAxis;

        if (isMoving) {
            blockedTicks = 0;
            faceToward(targetX, targetY);
            return;
        }

        blockedTicks++;
        if (blockedTicks >= MAX_BLOCKED_TICKS) {
            skipBlockedWaypoint(map);
        }
    }

    private boolean stepAlongAxis(GameMap map, double deltaX, double deltaY, double step) {
        double distance = Math.abs(deltaX) + Math.abs(deltaY);
        if (distance < 0.001) {
            return false;
        }
        double movement = Math.min(step, distance);
        double nextX = positionX + Math.signum(deltaX) * movement;
        double nextY = positionY + Math.signum(deltaY) * movement;
        if (map.isWall(nextX, nextY)) {
            return false;
        }
        positionX = nextX;
        positionY = nextY;
        return true;
    }

    /**
     * Gives up on a waypoint that can never be reached, so a bad path can only
     * cost a few seconds instead of freezing the animatronic for the whole night.
     * When there is no waypoint left to skip to, the animatronic restarts its
     * route from the first waypoint instead of standing still forever.
     */
    protected void skipBlockedWaypoint(GameMap map) {
        int skippedIndex = pathIndex;
        int nextIndex = (state == State.RETREAT) ? pathIndex - 1 : pathIndex + 1;
        if (nextIndex < 0 || nextIndex >= path.length) {
            nextIndex = 0;
        }

        pathIndex = nextIndex;
        double nextCentreX = (path[nextIndex][0] + 0.5) * GameMap.TILE_SIZE;
        double nextCentreY = (path[nextIndex][1] + 0.5) * GameMap.TILE_SIZE;
        if (!map.isWall(nextCentreX, nextCentreY)) {
            positionX = nextCentreX;
            positionY = nextCentreY;
        }
        blockedTicks = 0;

        if (nextIndex != skippedIndex) {
            System.out.println("WARN: " + getClass().getSimpleName() + " stuck on waypoint "
                + skippedIndex + " (" + path[skippedIndex][0] + "," + path[skippedIndex][1]
                + ") - skipping to waypoint " + nextIndex + " ("
                + path[nextIndex][0] + "," + path[nextIndex][1] + ")");
        }
    }

    protected boolean arrivedAtWaypoint() {
        if (pathIndex >= path.length) {
            return true;
        }
        int[] waypoint = path[pathIndex];
        return Math.hypot((waypoint[0] + 0.5) * GameMap.TILE_SIZE - positionX,
                          (waypoint[1] + 0.5) * GameMap.TILE_SIZE - positionY) < ARRIVAL_RADIUS;
    }

    protected boolean atLastWaypoint() {
        return pathIndex == path.length - 1 && arrivedAtWaypoint();
    }

    // animation stuff

    protected Texture pickTexture(Player player) {
        return textures.framesByOrientation[viewSector(player)];
    }


    protected void moveToward(double goalX, double goalY, double speed) {
        double deltaX = goalX - positionX;
        double deltaY = goalY - positionY;
        double distance = Math.hypot(deltaX, deltaY);
        if (distance < 0.001) {
            return;
        }
        facingX = deltaX / distance;
        facingY = deltaY / distance;
        if (distance <= speed) {
            positionX = goalX;
            positionY = goalY;
        } else {
            positionX += facingX * speed;
            positionY += facingY * speed;
        }
        isMoving = true;
    }

    protected void faceToward(double pixelX, double pixelY) {
        double deltaX = pixelX - positionX;
        double deltaY = pixelY - positionY;
        double distance = Math.hypot(deltaX, deltaY);
        if (distance < 0.001) {
            return;
        }
        facingX = deltaX / distance;
        facingY = deltaY / distance;
    }

    protected boolean hasLineOfSight(GameMap map, double targetX, double targetY) {
        double deltaX = targetX - positionX;
        double deltaY = targetY - positionY;
        int samples = (int) (Math.hypot(deltaX, deltaY) / 8) + 1;
        for (int sample = 1; sample < samples; sample++) {
            if (map.isWall(positionX + deltaX * sample / samples,
                           positionY + deltaY * sample / samples)) {
                return false;
            }
        }
        return true;
    }

    protected int viewSector(Player player) {
        double toPlayerX = player.positionX - positionX;
        double toPlayerY = player.positionY - positionY;
        double length = Math.hypot(toPlayerX, toPlayerY);
        if (length < 0.001) {
            return 0;
        }
        toPlayerX /= length;
        toPlayerY /= length;
        double handedness = Math.signum(player.directionX * player.cameraPlaneY
                                      - player.directionY * player.cameraPlaneX);
        double rightX = handedness * toPlayerY;
        double rightY = handedness * -toPlayerX;
        double towardViewer = facingX * toPlayerX + facingY * toPlayerY;
        double towardRight  = facingX * rightX + facingY * rightY;
        int sector = (int) Math.round(Math.atan2(towardRight, towardViewer) / (Math.PI / 4));
        return ((sector % 8) + 8) % 8;
    }

    protected static int toTile(double pixelPosition) {
        return (int) Math.floor(pixelPosition / GameMap.TILE_SIZE);
    }

    public boolean isAttacking() {
         return state == State.ATTACK;
        }

    public boolean isAttackFinished() {
        return attackFinished;
    }

    public boolean isVisible() {
        return true;
    }

    public void setAILevel(int newAILevel) {
        aiLevel = Math.max(0, Math.min(20, newAILevel));
    }

    public void setMovementOpportunityIntervalInTicks(int ticks) {
        movementOpportunityIntervalInTicks = ticks;
    }

    /**
     * One FNAF 1 movement opportunity: a roll between 1 and 20, successful when
     * the roll is at or below the AI level. Failing still costs a full interval.
     */
    protected boolean shouldTakeMovementOpportunity() {
        ticksSinceLastMovementOpportunity++;
        if (ticksSinceLastMovementOpportunity < movementOpportunityIntervalInTicks) {
            return false;
        }
        ticksSinceLastMovementOpportunity = 0;
        int rolledNumber = random.nextInt(20) + 1;
        return rolledNumber <= aiLevel;
    }

    /**
     * Called when something blocks the roll itself, such as the camera being up.
     * The timer restarts, so the block costs one whole movement opportunity.
     */
    protected void postponeMovementOpportunity() {
        ticksSinceLastMovementOpportunity = 0;
    }

    /**
     * True when the camera on screen is pointed at the tile this animatronic is
     * standing on. This is what makes an animatronic show up on a camera at all.
     */
    protected boolean isOnWatchedCamera(AnimatronicContext animatronicContext) {
        if (!animatronicContext.tabletUp || animatronicContext.viewingCameraTile == null) {
            return false;
        }
        return toTile(positionX) == animatronicContext.viewingCameraTile[0]
            && toTile(positionY) == animatronicContext.viewingCameraTile[1];
    }

}
