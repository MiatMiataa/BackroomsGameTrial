package GameTrial.render;

import GameTrial.world.SlidingBarrier;

public class RayHit {
    public int hitTileColumn;
    public int hitTileRow;
    public int wallOrientation;
    public double perpendicularWallDistance;
    public double wallHitFraction;
    public boolean hasHitWall;

    // Set when the hit is an office door or the Pirate Cove curtain instead of a wall.
    public SlidingBarrier hitBarrier;

    // The wall behind a barrier that is still partly closed. The ray keeps travelling
    // past the barrier, so the renderer can show the hallway through the open part.
    public RayHit wallBehindBarrier;

    public RayHit(int hitTileColumn, int hitTileRow, int wallOrientation, double perpendicularWallDistance,
        double wallHitFraction, boolean hasHitWall) {
            
        this.hitTileColumn = hitTileColumn;
        this.hitTileRow = hitTileRow;
        this.wallOrientation = wallOrientation;
        this.perpendicularWallDistance = perpendicularWallDistance;
        this.wallHitFraction = wallHitFraction;
        this.hasHitWall = hasHitWall;
    }
}
