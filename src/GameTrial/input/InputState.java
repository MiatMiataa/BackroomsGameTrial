package GameTrial.input;

public class InputState {
    public volatile double forward;

    public volatile double turn;

    public volatile double strafe;

    public volatile double mouseTurn;
    
    public volatile boolean tabletTogglePressed;

    public double getTotalTurn() {
        return Math.max(-1.0, Math.min(1.0, turn + mouseTurn));
    }
}