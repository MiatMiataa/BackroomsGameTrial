package GameTrial.entity;

import GameTrial.input.InputState;

public class Player {
    public double positionX = 64 + 32;
    public double positionY = 64 + 32;

    public double directionX = 1, directionY = 0;

    public double cameraPlaneX = 0, cameraPlaneY = 0.66;

    // guard can look this far left or right of the start direction (90 = both office doors)
    private double maxYawRadians = Math.toRadians(90);

    public void setMaxYawRadians(double newMaxYawRadians) {
        maxYawRadians = newMaxYawRadians;
    }
    private final double rotationSpeed = 0.055;
    private double currentYawRadians = 0;

    public void faceTowards(double targetPixelX, double targetPixelY) {
        double deltaX = targetPixelX - positionX;
        double deltaY = targetPixelY - positionY;
        double distance = Math.hypot(deltaX, deltaY);
        if (distance < 1e-6) {
            return;
        }
        double cameraPlaneLength = Math.hypot(cameraPlaneX, cameraPlaneY);
        directionX = deltaX / distance;
        directionY = deltaY / distance;
        cameraPlaneX = -directionY * cameraPlaneLength;
        cameraPlaneY = directionX * cameraPlaneLength;
        currentYawRadians = 0;
    }

    public void update(InputState inputState) {
        double totalTurn = inputState.getTotalTurn();
        if (totalTurn == 0) {
            return;
        }
        double requestedYaw = currentYawRadians + totalTurn * rotationSpeed;
        double allowedYaw = Math.max(-maxYawRadians, Math.min(maxYawRadians, requestedYaw));
        rotate(allowedYaw - currentYawRadians);
        currentYawRadians = allowedYaw;
    }

    private void rotate(double angleInRadians) {
        double oldDirectionX = directionX;
        directionX = directionX * Math.cos(angleInRadians) - directionY * Math.sin(angleInRadians);
        directionY = oldDirectionX * Math.sin(angleInRadians) + directionY * Math.cos(angleInRadians);

        double oldCameraPlaneX = cameraPlaneX;
        cameraPlaneX = cameraPlaneX * Math.cos(angleInRadians) - cameraPlaneY * Math.sin(angleInRadians);
        cameraPlaneY = oldCameraPlaneX * Math.sin(angleInRadians) + cameraPlaneY * Math.cos(angleInRadians);
    }
}