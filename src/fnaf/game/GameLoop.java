package fnaf.game;

public class GameLoop implements Runnable {
    private static final int MAX_CATCH_UP_TICKS = 5;

    private static final int TICKS_PER_SECOND = 60;

    private final Runnable tickAction;
    private Thread gameThread;
    private volatile boolean isRunning = false;

    public GameLoop(Runnable tickAction) {
        this.tickAction = tickAction;
    }

    public synchronized void start() {
        if (isRunning) return;
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        double nanosecondsPerTick = 1000000000.0 / TICKS_PER_SECOND;
        double tickTimeAccumulator = 0;
        long lastTickTimestamp = System.nanoTime();
        long currentTickTimestamp;

        while (isRunning) {
            currentTickTimestamp = System.nanoTime();
            tickTimeAccumulator += (currentTickTimestamp - lastTickTimestamp) / nanosecondsPerTick;
            lastTickTimestamp = currentTickTimestamp;

            if (tickTimeAccumulator > MAX_CATCH_UP_TICKS) {
                tickTimeAccumulator = MAX_CATCH_UP_TICKS;
            }
            
            if (tickTimeAccumulator >= 1) {
                tickAction.run();
                tickTimeAccumulator--;
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
