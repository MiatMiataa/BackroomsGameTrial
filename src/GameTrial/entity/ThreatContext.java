package GameTrial.entity;

public class ThreatContext {
    public boolean tabletUp;
    public boolean leftDoorClosed;
    public boolean rightDoorClosed;
    public AnimatronicType viewingCamera;

    // Tile the camera currently on screen is pointed at, or null while the tablet is down.
    public int[] viewingCameraTile;

    // False while Bonnie or Chica is still standing on the Show Stage.
    public boolean showStageIsClear;

    public long tick;
}
