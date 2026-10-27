package GameTrial.core;

public class GameLoop implements Runnable {
    private static final int MAX_CATCH_UP_TICKS = 5;

    private final int FPS = 60;

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
        double nanosecondsPerFrame = 1000000000.0 / FPS;
        double frameTimeAccumulator = 0;
        long lastFrameTimestamp = System.nanoTime();
        long currentFrameTimestamp;

        while (isRunning) {
            currentFrameTimestamp = System.nanoTime();
            frameTimeAccumulator += (currentFrameTimestamp - lastFrameTimestamp) / nanosecondsPerFrame;
            lastFrameTimestamp = currentFrameTimestamp;

            if (frameTimeAccumulator > MAX_CATCH_UP_TICKS) frameTimeAccumulator = MAX_CATCH_UP_TICKS;

            if (frameTimeAccumulator >= 1) {
                tickAction.run();
                frameTimeAccumulator--;
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