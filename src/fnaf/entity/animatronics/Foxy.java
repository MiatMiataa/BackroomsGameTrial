package fnaf.entity.animatronics;

import fnaf.assets.AnimatronicTextures;
import fnaf.entity.Animatronic;
import fnaf.entity.Player;
import fnaf.entity.AnimatronicContext;
import fnaf.world.GameMap;

public class Foxy extends Animatronic {
    private static final int APPROACH_TIMEOUT = 30;

    // Any camera at all keeps Foxy in his cove, then he stays stalled for a
    // random moment after the tablet comes down, as in FNAF 1.
    private static final int MINIMUM_CAMERA_STALL_TICKS = 50;     // 0.83 s
    private static final int MAXIMUM_CAMERA_STALL_TICKS = 1000;   // 16.67 s
    private static final int SPRINT_STAGE = 4;

    private int stage = 0;
    private int approachTimer = 0;
    private int cameraStallTicksLeft = 0;
    private boolean tabletWasUp = false;
    private boolean retreatPeek = false;

    public Foxy(int[][] path, AnimatronicTextures textures) {
        super(path, 1.2, 3.35, 0, textures, 0.9);
        setMovementOpportunityIntervalInTicks(301);   // about 5.01 s
    }

    @Override
    protected void updatePatrol(GameMap map, Player player, AnimatronicContext animatronicContext) {
        if (!sprinting || pathIndex == 0) {
            map.pirateCoveCurtain.targetOpenAmount = 0.0;
        } else if (map.pirateCoveCurtain.targetOpenAmount < 0.5) {
            map.pirateCoveCurtain.targetOpenAmount = 0.5;
        } else {
            map.pirateCoveCurtain.targetOpenAmount = 1.0;
        }
        
        if (!sprinting) {
            if (animatronicContext.tabletUp) {
                if (!tabletWasUp) {
                    tabletWasUp = true;
                    cameraStallTicksLeft = MINIMUM_CAMERA_STALL_TICKS
                        + random.nextInt(MAXIMUM_CAMERA_STALL_TICKS - MINIMUM_CAMERA_STALL_TICKS + 1);
                }
                postponeMovementOpportunity();
                return;
            }
            tabletWasUp = false;
            if (cameraStallTicksLeft > 0) {
                cameraStallTicksLeft--;
                postponeMovementOpportunity();
                return;
            }
            if (!shouldTakeMovementOpportunity()) return;
            stage++;
            if (stage >= SPRINT_STAGE) {
                sprinting = true;
                pathIndex = 0;
            }
            return;
        }
        if (!arrivedAtWaypoint()) { moveAlongPath(map); return; }
        if (atLastWaypoint()) {
            state = State.APPROACH;
            approachTimer = 0;
            animationTicks = 0;
            return;
        }
        pathIndex++;
        moveAlongPath(map);
    }

    @Override
    protected void updateApproach(Player player, AnimatronicContext animatronicContext) {
        if (animatronicContext.leftDoorClosed) {
            state = State.RETREAT;
            sprinting = false;   // he runs out, but walks home
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
        if (!arrivedAtWaypoint()) {
            moveAlongPath(map);
            return;
        }

        if (pathIndex == retreatStopIndex) {
            retreatPeek = random.nextBoolean();

            stage = retreatPeek ? 1 : 0;

            map.pirateCoveCurtain.targetOpenAmount = retreatPeek ? 0.5 : 0.0;

            sprinting = false;
            cameraStallTicksLeft = 0;
            tabletWasUp = false;
            state = State.PATROL;
            animationTicks = 0;
            return;
        }
        pathIndex--;
        moveAlongPath(map);
    }
}
