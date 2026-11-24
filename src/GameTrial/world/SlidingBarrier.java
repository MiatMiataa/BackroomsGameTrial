package GameTrial.world;

/**
 * A wall that can open and close: the two office doors and the Pirate Cove curtain.
 *
 * The barrier is not part of the tile grid. Entity movement keeps using the static
 * grid, so the animatronic routes are never blocked by a door; only the raycasting
 * sees the barrier, which is what makes a closed door hide the hallway.
 */
public class SlidingBarrier {
    public enum BarrierType { OFFICE_DOOR, PIRATE_COVE_CURTAIN }

    // 60 ticks per second: a door drops in about 0.2 s, the curtain slides in about 1.7 s.
    private static final double OFFICE_DOOR_OPEN_SPEED_PER_TICK = 1.0 / 12.0;
    private static final double PIRATE_COVE_CURTAIN_OPEN_SPEED_PER_TICK = 1.0 / 100.0;

    public final int column;
    public final int row;
    public final BarrierType type;

    // 0.0 = fully closed, 1.0 = fully open.
    public double openAmount;
    public double targetOpenAmount;

    private final double openSpeedPerTick;

    public SlidingBarrier(int column, int row, BarrierType type, double startingOpenAmount) {
        this.column = column;
        this.row = row;
        this.type = type;
        this.openSpeedPerTick = (type == BarrierType.OFFICE_DOOR)
            ? OFFICE_DOOR_OPEN_SPEED_PER_TICK
            : PIRATE_COVE_CURTAIN_OPEN_SPEED_PER_TICK;
        openAmount = Math.max(0.0, Math.min(1.0, startingOpenAmount));
        targetOpenAmount = openAmount;
    }

    public boolean isAt(int otherColumn, int otherRow) {
        return column == otherColumn && row == otherRow;
    }

    /** True while any part of the barrier still fills the doorway. */
    public boolean isBlockingView() {
        return openAmount < 1.0;
    }

    public void update() {
        if (openAmount < targetOpenAmount) {
            openAmount = Math.min(targetOpenAmount, openAmount + openSpeedPerTick);
        } else if (openAmount > targetOpenAmount) {
            openAmount = Math.max(targetOpenAmount, openAmount - openSpeedPerTick);
        }
    }
}
