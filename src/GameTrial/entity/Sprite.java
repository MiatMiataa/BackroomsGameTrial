package GameTrial.entity;

import GameTrial.assets.Texture;

public class Sprite {
    public double positionX;
    public double positionY;

    public final Texture texture;
    public final double heightScale;
    
    public double distanceSquaredToPlayer;

    public Sprite(double positionX, double positionY, Texture texture, double heightScale) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.texture = texture;
        this.heightScale = heightScale;
    }
}
