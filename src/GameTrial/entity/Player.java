package GameTrial.entity;

import GameTrial.input.InputState;
import GameTrial.world.GameMap;

public class Player {
    public double positionX = 64 + 32;
    public double positionY = 64 + 32;

    public double directionX = 1, directionY = 0;

    public double cameraPlaneX = 0, cameraPlaneY = 0.66;

    public final int collisionRadius = 15;
    private final double moveSpeed = 3.0;
    private final double rotationSpeed = 0.06;

    public void update(GameMap gameMap, InputState inputState) {
        double totalTurn = inputState.getTotalTurn();
        if (totalTurn != 0) rotate(totalTurn * rotationSpeed);

        double moveX = directionX * inputState.forward - directionY * inputState.strafe;
        double moveY = directionY * inputState.forward + directionX * inputState.strafe;
        double moveLength = Math.hypot(moveX, moveY);
    
        if (moveLength > 1) {
            moveX /= moveLength;
            moveY /= moveLength;
        }

        moveX *= moveSpeed;
        moveY *= moveSpeed;

        double nextX = positionX + moveX;
        if (!hasCollision(nextX, positionY, gameMap)) positionX = nextX;

        double nextY = positionY + moveY;
        if (!hasCollision(positionX, nextY, gameMap)) positionY = nextY;
    }

    private void rotate(double angleInRadians) {
        double oldDirectionX = directionX;
        directionX = directionX * Math.cos(angleInRadians) - directionY * Math.sin(angleInRadians);
        directionY = oldDirectionX * Math.sin(angleInRadians) + directionY * Math.cos(angleInRadians);

        double oldCameraPlaneX = cameraPlaneX;
        cameraPlaneX = cameraPlaneX * Math.cos(angleInRadians) - cameraPlaneY * Math.sin(angleInRadians);
        cameraPlaneY = oldCameraPlaneX * Math.sin(angleInRadians) + cameraPlaneY * Math.cos(angleInRadians);
    }

    private boolean hasCollision(double targetPositionX, double targetPositionY, GameMap gameMap) {
        return gameMap.isWall(targetPositionX - collisionRadius, targetPositionY - collisionRadius)
            || gameMap.isWall(targetPositionX + collisionRadius, targetPositionY - collisionRadius)
            || gameMap.isWall(targetPositionX - collisionRadius, targetPositionY + collisionRadius)
            || gameMap.isWall(targetPositionX + collisionRadius, targetPositionY + collisionRadius);
    }
}