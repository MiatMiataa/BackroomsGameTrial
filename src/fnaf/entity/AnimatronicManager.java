package fnaf.entity;

import fnaf.assets.TextureSet;
import fnaf.audio.AudioPlayer;
import fnaf.entity.animatronics.BonnieAndChicaAI;
import fnaf.entity.animatronics.Foxy;
import fnaf.entity.animatronics.Freddy;
import fnaf.entity.animatronics.GoldenFreddy;
import fnaf.world.GameMap;
import java.util.ArrayList;
import java.util.List;

public class AnimatronicManager {
    private final List<Animatronic> animatronics = new ArrayList<>();
    private final List<Sprite> cachedSprites = new ArrayList<>();
    private final BonnieAndChicaAI bonnie;
    private final BonnieAndChicaAI chica;
    private final Freddy freddy;
    private final Foxy foxy;
    private final GoldenFreddy goldenFreddy;
    private Animatronic attacker;
    private AnimatronicType attackerType;
    private boolean hasLost;

    public AnimatronicManager(GameMap gameMap, TextureSet textures, AudioPlayer audio) {
        bonnie = new BonnieAndChicaAI(gameMap.bonniePath, BonnieAndChicaAI.Side.LEFT, textures.getAnimatronicTextures(AnimatronicType.BONNIE));
        chica = new BonnieAndChicaAI(gameMap.chicaPath, BonnieAndChicaAI.Side.RIGHT, textures.getAnimatronicTextures(AnimatronicType.CHICA));
        freddy = new Freddy(gameMap.freddyPath, textures.getAnimatronicTextures(AnimatronicType.FREDDY));
        foxy = new Foxy(gameMap.foxyPath, textures.getAnimatronicTextures(AnimatronicType.FOXY));
        
        animatronics.add(bonnie);
        animatronics.add(chica);
        animatronics.add(freddy);
        animatronics.add(foxy);
        // He never walks, so his only waypoint is the CAM 2B poster he spawns at.
        goldenFreddy = new GoldenFreddy(new int[][]{{9, 17}}, textures.getAnimatronicTextures(AnimatronicType.GOLDEN_FREDDY), audio);
        animatronics.add(goldenFreddy);
    }

    public void setAILevels(int freddyLevel, int bonnieLevel, int chicaLevel, int foxyLevel) {
        freddy.setAILevel(freddyLevel);
        bonnie.setAILevel(bonnieLevel);
        chica.setAILevel(chicaLevel);
        foxy.setAILevel(foxyLevel);
    }

    public void update(GameMap gameMap, Player player, AnimatronicContext animatronicContext) {
        animatronicContext.showStageIsClear = !isOnShowStage(bonnie) && !isOnShowStage(chica);
        if (attacker != null) {
            attacker.tick(gameMap, player, animatronicContext);
            if (attacker.isAttackFinished()) hasLost = true;
            return;
        }
        for (Animatronic animatronic : animatronics) {
            animatronic.tick(gameMap, player, animatronicContext);
            if (animatronic.isAttacking()) {
                attacker = animatronic;
                attackerType = typeOf(animatronic);
                break;
            }
        }
    }

    /** Which animatronic is killing the guard, so the jumpscare can match it. */
    public AnimatronicType getAttackerType() {
        return attackerType;
    }

    private AnimatronicType typeOf(Animatronic animatronic) {
        if (animatronic == bonnie) {
            return AnimatronicType.BONNIE;
        }
        if (animatronic == chica) {
            return AnimatronicType.CHICA;
        }
        if (animatronic == freddy) {
            return AnimatronicType.FREDDY;
        }
        if (animatronic == foxy) {
            return AnimatronicType.FOXY;
        }
        if (animatronic == goldenFreddy) {
            return AnimatronicType.GOLDEN_FREDDY;
        }
        return null;
    }

    public List<Sprite> sprites() {
        cachedSprites.clear();
        for (Animatronic animatronic : animatronics) {
            if (animatronic.isVisible()) {
                cachedSprites.add(animatronic);
            }
        }
        return cachedSprites;
    }

    // The Show Stage is the top of the map: rows 1 and 2 are stage and stage front.
    private boolean isOnShowStage(Animatronic animatronic) {
        return Animatronic.toTile(animatronic.positionY) <= 2;
    }

    public boolean hasLost() { return hasLost; }
    public boolean isAnyAttacking() { return attacker != null; }
}
