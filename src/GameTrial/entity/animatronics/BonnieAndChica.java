package GameTrial.entity.animatronics;

import GameTrial.assets.AnimatronicTextures;
import GameTrial.entity.Animatronic;
import GameTrial.entity.Player;
import GameTrial.entity.ThreatContext;
import GameTrial.world.GameMap;

public class BonnieAndChica extends Animatronic {
    public enum Side { LEFT, RIGHT }

    private static final int APPROACH_TIMEOUT = 180;

    private final Side side;
    private int approachTimer;

    public BonnieAndChica(int[][] path, Side side, AnimatronicTextures textures) {
        super(path, 1.2, 0, 0, textures, 0.85);
        this.side = side;
        setMovementOpportunityIntervalInTicks(side == Side.LEFT ? 298 : 299);
    }

    @Override
    protected void updatePatrol(GameMap map, Player player, ThreatContext threatContext) {
        if (!arrivedAtWaypoint()) { moveAlongPath(map); return; }
        if (atLastWaypoint()) {
            state = State.APPROACH;
            approachTimer = 0;
            animationTicks = 0;
            return;
        }
        if (!shouldTakeMovementOpportunity()) return;
        pathIndex++;
        moveAlongPath(map);
    }

    @Override
    protected void updateApproach(Player player, ThreatContext threatContext) {
        if (isDefended(threatContext)) {
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

    private boolean isDefended(ThreatContext threatContext) {
        return side == Side.LEFT ? threatContext.leftDoorClosed : threatContext.rightDoorClosed;
    }
}