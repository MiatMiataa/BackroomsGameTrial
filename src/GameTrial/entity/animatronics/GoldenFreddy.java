package GameTrial.entity.animatronics;

import GameTrial.assets.AnimatronicTextures;
import GameTrial.entity.Animatronic;
import GameTrial.entity.Player;
import GameTrial.entity.ThreatContext;
import GameTrial.world.GameMap;
import java.util.Random;

public class GoldenFreddy extends Animatronic {
    private static final int SPAWN_CHANCE_PER_TICK = 5000; // 1 in 5000 chance
    private static final int REACTION_WINDOW = 90;         // 1.5 seconds to react

    private final Random random = new Random();
    private int reactionTimer = 0;
    private boolean spawned = false;

    public GoldenFreddy(int[][] path, AnimatronicTextures textures) {
        super(path, 0, 0, 0, textures, 0.95);
    }

    @Override
    protected void updatePatrol(GameMap map, Player player, ThreatContext threatContext) {
        if (!spawned && random.nextInt(SPAWN_CHANCE_PER_TICK) == 0) {
            spawned = true;
            positionX = player.positionX + player.directionX * 96;
            positionY = player.positionY + player.directionY * 96;
            reactionTimer = 0;
            state = State.APPROACH;
            animationTicks = 0;
        }
    }

    @Override
    protected void updateApproach(Player player, ThreatContext threatContext) {
        double toX = positionX - player.positionX;
        double toY = positionY - player.positionY;
        double length = Math.hypot(toX, toY);
        double facingHim = length < 0.001 ? 1 : (player.directionX * toX + player.directionY * toY) / length;
        boolean lookingAtHim = facingHim > 0.6;

        // If the player looks away or brings up the tablet, he vanishes
        if (!lookingAtHim || threatContext.tabletUp) {
            state = State.RETREAT;
            animationTicks = 0;
            return;
        }
        
        if (++reactionTimer > REACTION_WINDOW) {
            state = State.ATTACK;
            animationTicks = 0;
        }
    }

    @Override
    protected void updateRetreat(GameMap map, Player player, ThreatContext threatContext) {
        spawned = false;
        state = State.PATROL;
        animationTicks = 0;
    }
}