package GameTrial.entity;

import GameTrial.assets.AnimatronicTextures;
import GameTrial.world.GameMap;
import GameTrial.assets.Texture;

public abstract class Animatronic extends Sprite {
    public enum State { PATROL, APPROACH, ATTACK, RETREAT }

    protected static final int TICKS_PER_WALK_FRAME   = 8;
    protected static final int TICKS_PER_SPRINT_FRAME = 3;
    protected static final int TICKS_PER_IDLE_FRAME = 15;
    protected static final int TICKS_PER_ATTACK_FRAME = 4;
    protected static final double ARRIVAL_RADIUS = 4.0;

    protected final AnimatronicTextures textures;
    protected final int[][] path;
    protected final int stepTicks;
    protected final double walkSpeed;
    protected final double sprintSpeed;

    protected State state = State.PATROL;
    protected int pathIndex = 0;
    protected int stepTimer = 0;
    protected int animationTicks = 0;
    protected boolean attackFinished = false;
    protected boolean sprinting = false;
    protected boolean isMoving = false;

    protected double facingX = 0, facingY = 1;

    protected Animatronic(int[][] path, double walkSpeed, double sprintSpeed,
                          int stepTicks, AnimatronicTextures textures, double heightScale) {
        super((path[0][0] + 0.5) * GameMap.TILE_SIZE,
              (path[0][1] + 0.5) * GameMap.TILE_SIZE,
              textures.walk[0][0], heightScale);
        this.path = path;
        this.walkSpeed = walkSpeed;
        this.sprintSpeed = sprintSpeed;
        this.stepTicks = stepTicks;
        this.textures = textures;
    }

    public final void tick(GameMap map, Player player, ThreatContext threatContext) {
        animationTicks++;
        isMoving = false;
        switch (state) {
            case PATROL:
                updatePatrol(map, player, threatContext);
                break;
            case APPROACH:
                updateApproach(player, threatContext);
                break;
            case ATTACK:
                updateAttack(player);
                break;
            case RETREAT:
                updateRetreat(map, player, threatContext);
                break;
        }
        texture = pickTexture(player);
    }

    protected abstract void updatePatrol(GameMap map, Player player, ThreatContext threatContext);
    protected abstract void updateApproach(Player player, ThreatContext threatContext);
    protected abstract void updateRetreat(GameMap map, Player player, ThreatContext threatContext);

    protected void updateAttack(Player player) {
        faceToward(player.positionX, player.positionY);
        if (animationTicks >= textures.attack.length * TICKS_PER_ATTACK_FRAME) {
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

        // wallclip guard

        double dx = targetX - positionX;
        double dy = targetY - positionY;
        double length = Math.hypot(dx, dy);
        if (length < 0.001) {
            return;
        }
        double step = currentSpeed();
        double nextX = positionX + dx / length * step;
        double nextY = positionY + dy / length * step;
        if (map.isWall(nextX, nextY)) {
            return;
        }

        moveToward(targetX, targetY, step);
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
        if (state == State.ATTACK) {
            int frame = Math.min(animationTicks / TICKS_PER_ATTACK_FRAME,
                                textures.attack.length - 1);
            return textures.attack[frame];
        }
        if (sprinting && textures.sprint != null) {
            return textures.sprint[(animationTicks / TICKS_PER_SPRINT_FRAME)
                                    % textures.sprint.length];
        }
        if (isMoving) {
            Texture[] walkFrames = textures.walk[viewSector(player)];
            return walkFrames[(animationTicks / TICKS_PER_WALK_FRAME)
                               % walkFrames.length];
        }
        return textures.idle[(animationTicks / TICKS_PER_IDLE_FRAME)
                              % textures.idle.length];
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
}