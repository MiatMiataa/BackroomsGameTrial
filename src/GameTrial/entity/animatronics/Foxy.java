package GameTrial.entity.animatronics;

import GameTrial.assets.AnimatronicTextures;
import GameTrial.entity.Animatronic;
import GameTrial.entity.AnimatronicType;
import GameTrial.entity.Player;
import GameTrial.entity.ThreatContext;
import GameTrial.world.GameMap;

public class Foxy extends Animatronic {
    private static final int APPROACH_TIMEOUT = 30;

    private int stage = 0;
    private int approachTimer = 0;

    public Foxy(int[][] path, AnimatronicTextures textures) {
        super(path, 1.2, 3.35, 0, textures, 0.9);
        setMovementOpportunityIntervalInTicks(301);   // about 5.01 s
    }

    @Override
    protected void updatePatrol(GameMap map, Player player, ThreatContext threatContext) {
        if (!sprinting) {
            if (threatContext.viewingCamera == AnimatronicType.FOXY) {
                stage = 0;
                ticksSinceLastMovementOpportunity = 0;
                return;
            }
            if (shouldTakeMovementOpportunity()) {
                stage++;
                if (stage >= 4) {
                    sprinting = true;
                    pathIndex = 0;
                    positionX = (path[0][0] + 0.5) * GameMap.TILE_SIZE;
                    positionY = (path[0][1] + 0.5) * GameMap.TILE_SIZE;
                }
            }
            return;
        }
        if (!arrivedAtWaypoint()) { moveAlongPath(map); return; }
        if (atLastWaypoint()) {
            state = State.APPROACH;
            sprinting = false;
            approachTimer = 0;
            animationTicks = 0;
            return;
        }
        pathIndex++;
        moveAlongPath(map);
    }

    @Override
    protected void updateApproach(Player player, ThreatContext threatContext) {
        if (threatContext.leftDoorClosed) {
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
        positionX = (path[0][0] + 0.5) * GameMap.TILE_SIZE;
        positionY = (path[0][1] + 0.5) * GameMap.TILE_SIZE;
        pathIndex = 0;
        stepTimer = 0;
        stage = 0;
        sprinting = false;
        state = State.PATROL;
        animationTicks = 0;
    }
}