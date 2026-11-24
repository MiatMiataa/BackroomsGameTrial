package GameTrial.entity.animatronics;

import GameTrial.assets.AnimatronicTextures;
import GameTrial.entity.Animatronic;
import GameTrial.entity.Player;
import GameTrial.entity.ThreatContext;
import GameTrial.world.GameMap;

public class Freddy extends Animatronic {
    private static final int APPROACH_TIMEOUT = 180;

    private static final int EAST_HALL_CORNER_WAYPOINTS = 4;

    private int approachTimer;

    public Freddy(int[][] path, AnimatronicTextures textures) {
        super(path, 1.0, 0, 0, textures, 0.95);
        setMovementOpportunityIntervalInTicks(181);   // about 3.02 s
    }

    @Override
    protected void updatePatrol(GameMap map, Player player, ThreatContext threatContext) {
        // Walking is never interrupted: only the decision to set off again is.
        if (!arrivedAtWaypoint()) { moveAlongPath(map); return; }
        if (atLastWaypoint()) {
            state = State.APPROACH;
            approachTimer = 0;
            animationTicks = 0;
            return;
        }
        if (pathIndex == 0 && !threatContext.showStageIsClear) {
            postponeMovementOpportunity();
            return;
        }
        if (camerasStallHisMovement(threatContext)) {
            postponeMovementOpportunity();
            return;
        }
        if (!shouldTakeMovementOpportunity()) return;
        pathIndex++;
        moveAlongPath(map);
    }

    /**
     * Before the East Hall Corner any camera makes Freddy fail every movement
     * opportunity. Once he is there only his own camera does, which is why
     * watching 4B is what keeps him out of the office.
     */
    private boolean camerasStallHisMovement(ThreatContext threatContext) {
        if (!threatContext.tabletUp) {
            return false;
        }
        return isInEastHallCorner() ? isOnWatchedCamera(threatContext) : true;
    }

    private boolean isInEastHallCorner() {
        return pathIndex >= path.length - EAST_HALL_CORNER_WAYPOINTS;
    }

    @Override
    protected void updateApproach(Player player, ThreatContext threatContext) {
        // FNAF 1: a closed right door stops him at the door, it never sends him
        // back down the hall. He waits there until the door opens again.
        if (threatContext.rightDoorClosed) {
            approachTimer = 0;
            return;
        }
        if (++approachTimer > APPROACH_TIMEOUT) {
            state = State.ATTACK;
            animationTicks = 0;
        }
    }

    @Override
    protected void updateRetreat(GameMap map, Player player, ThreatContext threatContext) {
        state = State.PATROL;
        animationTicks = 0;
    }
}
