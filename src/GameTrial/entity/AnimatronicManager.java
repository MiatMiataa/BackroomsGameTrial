package GameTrial.entity;

import GameTrial.assets.TextureSet;
import GameTrial.entity.animatronics.BonnieAndChica;
import GameTrial.entity.animatronics.Foxy;
import GameTrial.entity.animatronics.Freddy;
import GameTrial.entity.animatronics.GoldenFreddy;
import GameTrial.world.GameMap;
import java.util.ArrayList;
import java.util.List;

public class AnimatronicManager {
    private final List<Animatronic> animatronics = new ArrayList<>();
    private final List<Sprite> cachedSprites = new ArrayList<>();
    private final BonnieAndChica bonnie;
    private final BonnieAndChica chica;
    private final Freddy freddy;
    private final Foxy foxy;
    private Animatronic attacker;
    private boolean hasLost;

    public AnimatronicManager(GameMap gameMap, TextureSet textures) {
        bonnie = new BonnieAndChica(gameMap.bonniePath, BonnieAndChica.Side.LEFT, textures.of(AnimatronicType.BONNIE));
        chica = new BonnieAndChica(gameMap.chicaPath, BonnieAndChica.Side.RIGHT, textures.of(AnimatronicType.CHICA));
        freddy = new Freddy(gameMap.freddyPath, textures.of(AnimatronicType.FREDDY));
        foxy = new Foxy(gameMap.foxyPath, textures.of(AnimatronicType.FOXY));
        
        animatronics.add(bonnie);
        animatronics.add(chica);
        animatronics.add(freddy);
        animatronics.add(foxy);
        animatronics.add(new GoldenFreddy(new int[][]{{8, 22}}, textures.of(AnimatronicType.GOLDEN_FREDDY)));
    }

    public void setAILevels(int freddyLevel, int bonnieLevel, int chicaLevel, int foxyLevel) {
        freddy.setAILevel(freddyLevel);
        bonnie.setAILevel(bonnieLevel);
        chica.setAILevel(chicaLevel);
        foxy.setAILevel(foxyLevel);
    }

    public void update(GameMap gameMap, Player player, ThreatContext threatContext) {
        if (attacker != null) {
            attacker.tick(gameMap, player, threatContext);
            if (attacker.isAttackFinished()) hasLost = true;
            return;
        }
        for (Animatronic animatronic : animatronics) {
            animatronic.tick(gameMap, player, threatContext);
            if (animatronic.isAttacking()) {
                attacker = animatronic;
                break;
            }
        }
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

    public boolean hasLost() { return hasLost; }
    public boolean isAnyAttacking() { return attacker != null; }
}