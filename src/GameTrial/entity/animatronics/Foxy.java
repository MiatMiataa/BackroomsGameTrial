package GameTrial.entity.animatronics;

import GameTrial.assets.AnimatronicTextures;
import GameTrial.entity.Animatronic;
import GameTrial.entity.AnimatronicType;
import GameTrial.entity.Player;
import GameTrial.entity.ThreatContext;
import GameTrial.world.GameMap;

public class Foxy extends Animatronic {
    private static final int[] STAGE_TICKS     = { 400, 300, 200, 100 };
    private static final int   APPROACH_TIMEOUT = 30;

    private int stage        = 0;
    private int stageTimer   = 0;
    private int approachTimer = 0;

    public Foxy(int[][] path, AnimatronicTextures textures) {
        super(path, 1.2, 3.35, 0, textures, 0.9);
    }

    @Override
    protected void updatePatrol(GameMap map, Player player, ThreatContext threatContext) {
        if (!sprinting) {
            // Stage up only when the player is NOT watching the cove / timer for foxy to come out running
            if (threatContext.viewingCamera == AnimatronicType.FOXY) {
                stage = 0;
                stageTimer = 0;
                return;
            }
            if (++stageTimer < STAGE_TICKS[stage]) {
                return;
            }
            stageTimer = 0;
            stage++;
            if (stage >= STAGE_TICKS.length) {
                sprinting = true;
                pathIndex = 0;
                positionX = (path[0][0] + 0.5) * GameMap.TILE_SIZE;
                positionY = (path[0][1] + 0.5) * GameMap.TILE_SIZE;
            }
            return;
        }

        if (!arrivedAtWaypoint()) {
            moveAlongPath(map); return;
        }
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
        stageTimer = 0;
        sprinting = false;
        state = State.PATROL;
        animationTicks = 0;
    }
}