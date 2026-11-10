package GameTrial.entity.animatronics;

import GameTrial.assets.AnimatronicTextures;
import GameTrial.entity.Animatronic;
import GameTrial.entity.Player;
import GameTrial.entity.ThreatContext;
import GameTrial.world.GameMap;

public class Freddy extends Animatronic {
    private static final int APPROACH_TIMEOUT = 180;
    private static final double FACING_THRESHOLD = 0.6;

    private int approachTimer;

    public Freddy(int[][] path, int stepTicks, AnimatronicTextures textures) {
        super(path, 1.0, 0, stepTicks, textures, 0.95);
    }

    @Override
    protected void updatePatrol(GameMap map, Player player, ThreatContext threatContext) {
        if (isWatched(map, player) || threatContext.tabletUp) {
            return;
        }
        if (!arrivedAtWaypoint()) {
            moveAlongPath(map);
            return;
        }
        if (atLastWaypoint()) {
            state = State.APPROACH;
            approachTimer = 0;
            animationTicks = 0;
            return;
        }
        if (++stepTimer < stepTicks) {
            return;
        }
        stepTimer = 0;
        pathIndex++;
        moveAlongPath(map);
    }

    @Override
    protected void updateApproach(Player player, ThreatContext threatContext) {
        if (threatContext.flashlightOn) {
            state = State.RETREAT;
            animationTicks = 0;
            return;
        }
        if (++approachTimer > APPROACH_TIMEOUT) {
            state = State.ATTACK;
            animationTicks = 0;
        }
    }

    @Override
    protected void updateRetreat(GameMap map, Player player, ThreatContext threatContext) {
        if (arrivedAtWaypoint()) {
            if (pathIndex == 0) {
                state = State.PATROL;
                stepTimer = 0;
                animationTicks = 0;
                return;
            }
            pathIndex--;
        }
        moveAlongPath(map);
    }

    private boolean isWatched(GameMap map, Player player) {
        double toX = player.positionX - positionX;
        double toY = player.positionY - positionY;
        double length = Math.hypot(toX, toY);
        if (length < 0.001) {
            return false;
        }
        double facingPlayer = (player.directionX * toX + player.directionY * toY) / length;
        return facingPlayer > FACING_THRESHOLD && hasLineOfSight(map, player.positionX, player.positionY);
    }
}
