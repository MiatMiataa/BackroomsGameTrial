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
    private Animatronic attacker;
    private boolean hasLost;

    public AnimatronicManager(GameMap gameMap, TextureSet textures) {
        animatronics.add(new BonnieAndChica(
            gameMap.bonniePath, BonnieAndChica.Side.LEFT, 50,
            textures.of(AnimatronicType.BONNIE)));
            
        animatronics.add(new BonnieAndChica(
            gameMap.chicaPath, BonnieAndChica.Side.RIGHT, 65,
            textures.of(AnimatronicType.CHICA)));

        animatronics.add(new Freddy(
            gameMap.freddyPath, 80,
            textures.of(AnimatronicType.FREDDY)));
        
        animatronics.add(new Foxy(
            gameMap.foxyPath,
            textures.of(AnimatronicType.FOXY)));

        animatronics.add(new GoldenFreddy(
            new int[][]{{8, 22}}, // dummy path
            textures.of(AnimatronicType.GOLDEN_FREDDY)));
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
        cachedSprites.addAll(animatronics);
        return cachedSprites;
    }

    public boolean hasLost() {
        return hasLost;
    }

    public boolean isAnyAttacking() {
        return attacker != null;
    }
}