package fnaf.input;

public class PlayerInput {
    public volatile double turn;
    public volatile double mouseTurn;
    public volatile boolean tabletTogglePressed;
    public volatile boolean leftDoorClosed;
    public volatile boolean rightDoorClosed;
    public volatile int selectedCamera = -1;

    // Set by M, read and cleared by the game loop.
    public volatile boolean toggleMinimapDebug = false;

    public double getTotalTurn() {
        return Math.max(-1.0, Math.min(1.0, turn + mouseTurn));
    }
}
