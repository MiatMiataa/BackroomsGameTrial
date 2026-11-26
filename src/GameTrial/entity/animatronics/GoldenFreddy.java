package GameTrial.entity.animatronics;

import GameTrial.assets.AnimatronicTextures;
import GameTrial.audio.AudioPlayer;
import GameTrial.entity.Animatronic;
import GameTrial.entity.Player;
import GameTrial.entity.ThreatContext;
import GameTrial.world.GameMap;

/**
 * Golden Freddy, following the FNAF 1 rules: he never walks and never picks a
 * random moment to kill you. Staring at the "IT'S ME" poster on CAM 2B (West
 * Hall Corner) rolls for him, and he is sitting in the office as soon as the
 * monitor comes down. Raising the monitor again is the only way out; looking
 * away does nothing.
 */
public class GoldenFreddy extends Animatronic {
    private static final int[] POSTER_TILE = {9, 17};   // CAM 2B - West Hall Corner
    private static final int POSTER_ROLL = 5000;        // 1 in 5000 per tick while on the poster

    private static final String OUTSIDE_SOUND = "GoldenFreddyOutside.wav";
    private static final String OFFICE_SOUND = "GoldenFreddyOffice.wav";
    private static final long FALLBACK_OFFICE_SOUND_MILLIS = 15000;
    private static final long EXTRA_REACTION_MILLIS = 1000;
    private static final int TICKS_PER_SECOND = 60;

    private static final double MAXIMUM_APPEARANCE_DISTANCE = 96;
    private static final double MINIMUM_APPEARANCE_DISTANCE = 24;
    private static final double WALL_CLEARANCE = 12;

    private final AudioPlayer audio;
    private final int reactionWindowTicks;

    private boolean appearancePending = false;
    private int reactionTimer = 0;

    public GoldenFreddy(int[][] path, AnimatronicTextures textures, AudioPlayer audio) {
        super(path, 0, 0, 0, textures, 0.95);
        this.audio = audio;

        // He waits out his own arrival sound, plus one extra second to react.
        long officeSoundMillis = audio.getDurationMillis(OFFICE_SOUND);
        if (officeSoundMillis <= 0) {
            officeSoundMillis = FALLBACK_OFFICE_SOUND_MILLIS;
        }
        reactionWindowTicks = (int) ((officeSoundMillis + EXTRA_REACTION_MILLIS)
            * TICKS_PER_SECOND / 1000);
    }

    @Override
    protected void updatePatrol(GameMap map, Player player, ThreatContext threatContext) {
        // The poster has been seen: he is in the office the moment the monitor drops.
        if (appearancePending) {
            if (threatContext.tabletUp) {
                return;
            }
            appearInFrontOfPlayer(map, player);
            return;
        }
        if (threatContext.tabletUp
            && isViewingPoster(threatContext)
            && random.nextInt(POSTER_ROLL) == 0) {
            appearancePending = true;
            audio.playOnce(OUTSIDE_SOUND);
        }
    }

    @Override
    protected void updateApproach(Player player, ThreatContext threatContext) {
        faceToward(player.positionX, player.positionY);
        if (threatContext.tabletUp) {
            audio.stop(OFFICE_SOUND);
            state = State.RETREAT;
            animationTicks = 0;
            return;
        }
        if (++reactionTimer > reactionWindowTicks) {
            state = State.ATTACK;
            animationTicks = 0;
        }
    }

    @Override
    protected void updateRetreat(GameMap map, Player player, ThreatContext threatContext) {
        audio.stop(OFFICE_SOUND);
        appearancePending = false;
        reactionTimer = 0;
        state = State.PATROL;
        animationTicks = 0;
    }

    @Override
    public boolean isVisible() {
        // He has to be seen in the office, otherwise the monitor is a blind guess.
        return state == State.APPROACH || state == State.ATTACK;
    }

    private boolean isViewingPoster(ThreatContext threatContext) {
        int[] watchedTile = threatContext.viewingCameraTile;
        return watchedTile != null
            && watchedTile[0] == POSTER_TILE[0]
            && watchedTile[1] == POSTER_TILE[1];
    }

    /**
     * Puts him straight ahead of the guard, just short of whatever wall the
     * guard is facing, so he is always on screen even when the guard has turned
     * into a corner.
     */
    private void appearInFrontOfPlayer(GameMap map, Player player) {
        double distance = distanceToFirstWallAhead(map, player);
        positionX = player.positionX + player.directionX * distance;
        positionY = player.positionY + player.directionY * distance;
        facingX = -player.directionX;
        facingY = -player.directionY;
        appearancePending = false;
        reactionTimer = 0;
        state = State.APPROACH;
        animationTicks = 0;
        audio.playOnce(OFFICE_SOUND);
    }

    private double distanceToFirstWallAhead(GameMap map, Player player) {
        for (double distance = MINIMUM_APPEARANCE_DISTANCE;
             distance <= MAXIMUM_APPEARANCE_DISTANCE;
             distance += 4) {
            if (map.isWall(player.positionX + player.directionX * distance,
                           player.positionY + player.directionY * distance)) {
                return Math.max(MINIMUM_APPEARANCE_DISTANCE, distance - WALL_CLEARANCE);
            }
        }
        return MAXIMUM_APPEARANCE_DISTANCE;
    }
}
