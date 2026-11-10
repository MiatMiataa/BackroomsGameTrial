package GameTrial.input;

public class InputState {
    public volatile double turn;
    public volatile double mouseTurn;
    public volatile boolean tabletTogglePressed;
    public volatile boolean leftDoorTogglePressed;
    public volatile boolean rightDoorTogglePressed;
    public volatile boolean flashlightOn;
    public volatile int requestedCamera = -1;

    public double getTotalTurn() {
        return Math.max(-1.0, Math.min(1.0, turn + mouseTurn));
    }
}