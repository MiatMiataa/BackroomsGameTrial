package GameTrial.render;

import GameTrial.world.GameMap;
import GameTrial.world.SlidingBarrier;

public class Raycaster {
    private final GameMap gameMap;
    private static final int MAX_RAY_STEPS = 48;

    public GameMap getGameMap() {
        return gameMap;
    }

    public Raycaster(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public RayHit castRay(double rayStartX, double rayStartY, double rayDirectionX, double rayDirectionY) {
        int currentTileColumn = (int) Math.floor(rayStartX / GameMap.TILE_SIZE);
        int currentTileRow = (int) Math.floor(rayStartY / GameMap.TILE_SIZE);

        double distanceToCrossOneTileX = (rayDirectionX == 0) ? 1e30 : Math.abs(GameMap.TILE_SIZE / rayDirectionX);
        double distanceToCrossOneTileY = (rayDirectionY == 0) ? 1e30 : Math.abs(GameMap.TILE_SIZE / rayDirectionY);

        int tileStepX, tileStepY;
        double distanceToNextTileEdgeX, distanceToNextTileEdgeY;

        if (rayDirectionX < 0) {
            tileStepX = -1;
            distanceToNextTileEdgeX = (rayStartX - currentTileColumn * GameMap.TILE_SIZE) / Math.abs(rayDirectionX);
        } else {
            tileStepX = 1;
            distanceToNextTileEdgeX = ((currentTileColumn + 1) * GameMap.TILE_SIZE - rayStartX) / Math.abs(rayDirectionX);
        }
        if (rayDirectionY < 0) {
            tileStepY = -1;
            distanceToNextTileEdgeY = (rayStartY - currentTileRow * GameMap.TILE_SIZE) / Math.abs(rayDirectionY);
        } else {
            tileStepY = 1;
            distanceToNextTileEdgeY = ((currentTileRow + 1) * GameMap.TILE_SIZE - rayStartY) / Math.abs(rayDirectionY);
        }

        boolean hasHitWall = false;
        int wallOrientation = 0;
        int stepCount = 0;

        // The nearest barrier this ray crosses, if any. The ray does not stop there:
        // it walks on to the wall behind, so a half-open door still shows the hallway.
        SlidingBarrier nearestBarrier = null;
        int barrierTileColumn = 0;
        int barrierTileRow = 0;
        int barrierOrientation = 0;
        double barrierDistance = 0;
        double barrierHitFraction = 0;

        while (!hasHitWall && stepCount < MAX_RAY_STEPS) {
            stepCount++;
            if (distanceToNextTileEdgeX < distanceToNextTileEdgeY) {
                distanceToNextTileEdgeX += distanceToCrossOneTileX;
                currentTileColumn += tileStepX;
                wallOrientation = 0;
            } else {
                distanceToNextTileEdgeY += distanceToCrossOneTileY;
                currentTileRow += tileStepY;
                wallOrientation = 1;
            }
            if (gameMap.getTile(currentTileColumn, currentTileRow) == 1) {
                hasHitWall = true;
                break;
            }
            if (nearestBarrier == null) {
                SlidingBarrier barrier = gameMap.getBarrierAt(currentTileColumn, currentTileRow);
                if (barrier != null && barrier.isBlockingView()) {
                    nearestBarrier = barrier;
                    barrierTileColumn = currentTileColumn;
                    barrierTileRow = currentTileRow;
                    barrierOrientation = wallOrientation;
                    barrierDistance = distanceAlongRay(wallOrientation,
                        distanceToNextTileEdgeX, distanceToCrossOneTileX,
                        distanceToNextTileEdgeY, distanceToCrossOneTileY);
                    barrierHitFraction = hitFractionAlongWall(barrierOrientation, barrierDistance,
                        rayStartX, rayStartY, rayDirectionX, rayDirectionY);
                }
            }
        }

        double perpendicularWallDistance = distanceAlongRay(wallOrientation,
            distanceToNextTileEdgeX, distanceToCrossOneTileX,
            distanceToNextTileEdgeY, distanceToCrossOneTileY);
        double wallHitFraction = hitFractionAlongWall(wallOrientation, perpendicularWallDistance,
            rayStartX, rayStartY, rayDirectionX, rayDirectionY);

        RayHit wallHit = new RayHit(currentTileColumn, currentTileRow, wallOrientation,
            perpendicularWallDistance, wallHitFraction, hasHitWall);
        if (nearestBarrier == null) {
            return wallHit;
        }

        RayHit barrierHit = new RayHit(barrierTileColumn, barrierTileRow, barrierOrientation,
            barrierDistance, barrierHitFraction, true);
        barrierHit.hitBarrier = nearestBarrier;
        barrierHit.wallBehindBarrier = wallHit;
        return barrierHit;
    }

    private static double distanceAlongRay(int wallOrientation,
        double distanceToNextTileEdgeX, double distanceToCrossOneTileX,
        double distanceToNextTileEdgeY, double distanceToCrossOneTileY) {

        return (wallOrientation == 0)
            ? (distanceToNextTileEdgeX - distanceToCrossOneTileX)
            : (distanceToNextTileEdgeY - distanceToCrossOneTileY);
    }

    private static double hitFractionAlongWall(int wallOrientation, double distanceAlongTheRay,
        double rayStartX, double rayStartY, double rayDirectionX, double rayDirectionY) {

        double wallHitPixelX = rayStartX + rayDirectionX * distanceAlongTheRay;
        double wallHitPixelY = rayStartY + rayDirectionY * distanceAlongTheRay;
        double wallHitFraction = (wallOrientation == 0) ? wallHitPixelY : wallHitPixelX;

        wallHitFraction = wallHitFraction % GameMap.TILE_SIZE;
        if (wallHitFraction < 0) {
            wallHitFraction += GameMap.TILE_SIZE;
        }
        return wallHitFraction / GameMap.TILE_SIZE;
    }
}
