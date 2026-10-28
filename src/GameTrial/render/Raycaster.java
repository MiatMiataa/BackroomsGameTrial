package GameTrial.render;

import GameTrial.world.GameMap;

public class Raycaster {
    private final GameMap gameMap;
    private static final int MAX_RAY_STEPS = 48;

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

        while (!hasHitWall && stepCount < MAX_RAY_STEPS) {
            if (distanceToNextTileEdgeX < distanceToNextTileEdgeY) {
                distanceToNextTileEdgeX += distanceToCrossOneTileX;
                currentTileColumn += tileStepX;
                wallOrientation = 0;
            } else {
                distanceToNextTileEdgeY += distanceToCrossOneTileY;
                currentTileRow += tileStepY;
                wallOrientation = 1;
            }
            if (gameMap.getTile(currentTileColumn, currentTileRow) == 1) hasHitWall = true;
            stepCount++;
        }

        double perpendicularWallDistance = (wallOrientation == 0)
                ? (distanceToNextTileEdgeX - distanceToCrossOneTileX)
                : (distanceToNextTileEdgeY - distanceToCrossOneTileY);

        double wallHitPixelX = rayStartX + rayDirectionX * perpendicularWallDistance;
        double wallHitPixelY = rayStartY + rayDirectionY * perpendicularWallDistance;
        double wallHitFraction = (wallOrientation == 0) ? wallHitPixelY : wallHitPixelX;
        wallHitFraction = wallHitFraction % GameMap.TILE_SIZE;
        if (wallHitFraction < 0) wallHitFraction += GameMap.TILE_SIZE;
        wallHitFraction /= GameMap.TILE_SIZE;

        return new RayHit(currentTileColumn, currentTileRow, wallOrientation, perpendicularWallDistance, wallHitFraction, hasHitWall);
    }
}