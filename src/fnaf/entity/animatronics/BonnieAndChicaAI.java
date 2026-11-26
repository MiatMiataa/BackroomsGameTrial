package fnaf.entity.animatronics;

import fnaf.assets.AnimatronicTextures;
import fnaf.entity.Animatronic;
import fnaf.entity.Player;
import fnaf.entity.AnimatronicContext;
import fnaf.world.GameMap;

public class BonnieAndChicaAI extends Animatronic {
    public enum Side { LEFT, RIGHT }

    private static final int APPROACH_TIMEOUT = 180;

    // Bonnie walks back to the Dining Area, Chica only as far as the East Hall,
    // which is why she comes back to the right door faster than Bonnie does.
    private static final int DINING_AREA_WAYPOINT = 4;
    private static final int EAST_HALL_CORNER_WAYPOINTS = 4;

    private final Side side;
    private int approachTimer;

    public BonnieAndChicaAI(int[][] path, Side side, AnimatronicTextures textures) {
        super(path, 1.2, 0, 0, textures, 0.85);
        this.side = side;
        setMovementOpportunityIntervalInTicks(side == Side.LEFT ? 298 : 299);
        retreatStopIndex = side == Side.LEFT ? DINING_AREA_WAYPOINT : path.length - EAST_HALL_CORNER_WAYPOINTS;
    }

    @Override
    protected void updatePatrol(GameMap map, Player player, AnimatronicContext animatronicContext) {
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
    protected void updateApproach(Player player, AnimatronicContext animatronicContext) {
        if (isDefended(animatronicContext)) {
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
    protected void updateRetreat(GameMap map, Player player, AnimatronicContext animatronicContext) {
        if (arrivedAtWaypoint()) {
            if (pathIndex <= retreatStopIndex) {
                state = State.PATROL;
                stepTimer = 0;
                animationTicks = 0;
                return;
            }
            pathIndex--;
        }
        moveAlongPath(map);
    }

    private boolean isDefended(AnimatronicContext animatronicContext) {
        return side == Side.LEFT ? animatronicContext.leftDoorClosed : animatronicContext.rightDoorClosed;
    }
}
