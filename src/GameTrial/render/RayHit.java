package GameTrial.render;

public class RayHit {
    public int hitTileColumn;
    public int hitTileRow;
    public int wallOrientation;
    public double perpendicularWallDistance;
    public double wallHitFraction;

    public RayHit(int hitTileColumn, int hitTileRow, int wallOrientation, double perpendicularWallDistance, double wallHitFraction) {
        this.hitTileColumn = hitTileColumn;
        this.hitTileRow = hitTileRow;
        this.wallOrientation = wallOrientation;
        this.perpendicularWallDistance = perpendicularWallDistance;
        this.wallHitFraction = wallHitFraction;
    }
}
