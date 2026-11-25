package GameTrial.core;

import GameTrial.assets.TextureSet;
import GameTrial.audio.AudioPlayer;
import GameTrial.entity.AnimatronicManager;
import GameTrial.entity.AnimatronicType;
import GameTrial.entity.Player;
import GameTrial.entity.ThreatContext;
import GameTrial.input.InputState;
import GameTrial.input.KeyboardInput;
import GameTrial.input.MouseInput;
import GameTrial.render.Raycaster;
import GameTrial.render.Renderer;
import GameTrial.world.GameMap;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Game {
    // PLAYING is the night itself; the other phases are the win and death screens.
    public enum Phase { PLAYING, WIN_SEQUENCE, JUMPSCARE, STATIC, DEAD }

    // FNAF 1 feeds sway side to side: 25 degrees each way (50 total) over 2 seconds.
    private static final double CAMERA_PAN_RADIANS = Math.toRadians(25);
    private static final int CAMERA_PAN_HALF_TICKS = 60;
    private double cameraBaseDirectionX = 0;
    private double cameraBaseDirectionY = -1;
    private int cameraPanTick = 0;

    // 60 ticks per second.
    private static final int WIN_SEQUENCE_CLOCK_FLIP_TICKS = 120;   // 5 AM held for 2 s
    private static final int WIN_SEQUENCE_TOTAL_TICKS = 300;        // 6 AM held for 3 s
    private static final int JUMPSCARE_TICKS = 120;                 // 2 s
    private static final int STATIC_TICKS = 120;                    // 2 s
    private static final int DEAD_TICKS = 240;                      // 4 s

    private int currentCameraIndex = 0;
    private boolean showMinimapDebug = false;

    private enum View { OFFICE, TABLET }
    private View currentView = View.OFFICE;

    private static final int TICKS_PER_HOUR  = 5340;
    private static final int TICKS_PER_NIGHT = TICKS_PER_HOUR * 6;

    private final int currentNightNumber;
    private int freddyAILevel;
    private int bonnieAILevel;
    private int chicaAILevel;
    private int foxyAILevel;
    private int lastHourIndex = 0;

    private final SaveManager saveManager = new SaveManager();
    private final AudioPlayer audio = new AudioPlayer();
    private final ThreatContext threatContext = new ThreatContext();
    private final Player tabletCamera;

    private GameLoop gameLoop;
    private Runnable exitToMenuAction;
    private Phase phase = Phase.PLAYING;
    private int phaseTick = 0;
    private AnimatronicType jumpscareType = AnimatronicType.FREDDY;
    private boolean nightHasFinished = false;

    private final GameMap gameMap;
    private final Raycaster raycaster;
    private final Player player;
    private final Renderer renderer;
    private final InputState inputState;
    private final KeyboardInput keyboardInput;
    private final MouseInput mouseInput;
    private final AnimatronicManager animatronics;

    public Game() { this(1); }

    public Game(int startNightNumber) {
        this.currentNightNumber = Math.max(1, Math.min(5, startNightNumber));
        freddyAILevel = NightConfig.freddyAILevel(currentNightNumber);
        bonnieAILevel = NightConfig.bonnieAILevel(currentNightNumber);
        chicaAILevel = NightConfig.chicaAILevel(currentNightNumber);
        foxyAILevel = NightConfig.foxyAILevel(currentNightNumber);

        gameMap = new GameMap();
        raycaster = new Raycaster(gameMap);

        player = new Player();
        player.positionX = (gameMap.guardColumn + 0.5) * GameMap.TILE_SIZE;
        player.positionY = (gameMap.guardRow + 0.5) * GameMap.TILE_SIZE;
        player.faceTowards(player.positionX, player.positionY - GameMap.TILE_SIZE);

        tabletCamera = new Player();
        placeCamera(tabletCamera, gameMap.cameraTiles[0], gameMap.cameraLookDirections[0]);

        inputState = new InputState();
        keyboardInput = new KeyboardInput(inputState);
        mouseInput = new MouseInput(inputState);

        try {
            TextureSet textureSet = new TextureSet();
            renderer = new Renderer(raycaster, textureSet);
            animatronics = new AnimatronicManager(gameMap, textureSet);
        } catch (IOException exception) {
            throw new RuntimeException("Could not load a texture: " + exception.getMessage(), exception);
        }

        animatronics.setAILevels(freddyAILevel, bonnieAILevel, chicaAILevel, foxyAILevel);
    }

    public boolean hasWon() { return phase == Phase.WIN_SEQUENCE; }
    public boolean hasLost() { return phase == Phase.JUMPSCARE || phase == Phase.STATIC || phase == Phase.DEAD; }
    public Phase getPhase() { return phase; }
    public AnimatronicType getJumpscareType() { return jumpscareType; }

    /** Called when the night win screen or the death screen has finished. */
    public void setExitToMenuAction(Runnable exitToMenuAction) {
        this.exitToMenuAction = exitToMenuAction;
    }

    public void attachInput(Component screenComponent) {
        screenComponent.addKeyListener(keyboardInput);
        screenComponent.addFocusListener(keyboardInput);
        screenComponent.addMouseListener(mouseInput);
        screenComponent.addMouseMotionListener(mouseInput);
        screenComponent.addFocusListener(mouseInput);
    }

    public void start(Runnable repaintAction) {
        gameLoop = new GameLoop(() -> { update(); repaintAction.run(); });
        gameLoop.start();
    }

    /** Stops the night. Called when the player goes back to the menu. */
    public void stop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        audio.close();
    }

    private void applyHourlyAIGains(int hourIndex) {
        boolean gainsHappened = false;
        if (hourIndex == 2) {
            bonnieAILevel++;
            gainsHappened = true;
        } else if (hourIndex == 3 || hourIndex == 4) {
            bonnieAILevel++;
            chicaAILevel++;
            foxyAILevel++;
            gainsHappened = true;
        }
        if (gainsHappened) {
            animatronics.setAILevels(freddyAILevel, bonnieAILevel, chicaAILevel, foxyAILevel);
        }
    }

    public int getCurrentNightNumber() { return currentNightNumber; }

    public int getDisplayedClockHour() {
        if (phase == Phase.WIN_SEQUENCE) {
            return phaseTick < WIN_SEQUENCE_CLOCK_FLIP_TICKS ? 5 : 6;
        }
        int hourIndex = (int) (threatContext.tick / TICKS_PER_HOUR);
        return hourIndex == 0 ? 12 : Math.min(hourIndex, 6);
    }

    private synchronized void update() {
        switch (phase) {
            case PLAYING:
                updatePlaying();
                break;
            case WIN_SEQUENCE:
                updateWinSequence();
                break;
            case JUMPSCARE:
                updateJumpscare();
                break;
            case STATIC:
                updateStatic();
                break;
            case DEAD:
                updateDead();
                break;
        }
    }

    private void updatePlaying() {
        if (threatContext.tick >= TICKS_PER_NIGHT - 1) {
            beginWinSequence();
            return;
        }

        if (inputState.toggleMinimapDebug) {
            inputState.toggleMinimapDebug = false;
            showMinimapDebug = !showMinimapDebug;
        }

        int currentHourIndex = (int) (threatContext.tick / TICKS_PER_HOUR);
        while (lastHourIndex < currentHourIndex) {
            lastHourIndex++;
            applyHourlyAIGains(lastHourIndex);
        }

        if (!animatronics.isAnyAttacking()) {
            if (inputState.tabletTogglePressed) {
                inputState.tabletTogglePressed = false;
                currentView = (currentView == View.OFFICE) ? View.TABLET : View.OFFICE;
            }
            if (currentView == View.TABLET && inputState.requestedCamera >= 0) {
                currentCameraIndex = Math.min(inputState.requestedCamera, gameMap.cameraTiles.length - 1);
                inputState.requestedCamera = -1;
                placeCamera(tabletCamera, gameMap.cameraTiles[currentCameraIndex],
                    gameMap.cameraLookDirections[currentCameraIndex]);
            }
            if (currentView == View.TABLET) {
                updateCameraPan();
            } else {
                player.update(inputState);
            }
        }

        threatContext.tabletUp = currentView == View.TABLET;
        threatContext.viewingCamera = threatContext.tabletUp ? cameraAnimatronic(currentCameraIndex) : null;
        threatContext.viewingCameraTile = threatContext.tabletUp ? gameMap.cameraTiles[currentCameraIndex] : null;
        threatContext.leftDoorClosed = inputState.leftDoorClosed;
        threatContext.rightDoorClosed = inputState.rightDoorClosed;
        threatContext.tick++;

        // The doors follow the guard's switches; Foxy sets the curtain target himself.
        gameMap.leftDoorBarrier.targetOpenAmount = inputState.leftDoorClosed ? 0.0 : 1.0;
        gameMap.rightDoorBarrier.targetOpenAmount = inputState.rightDoorClosed ? 0.0 : 1.0;

        animatronics.update(gameMap, player, threatContext);
        gameMap.updateBarriers();
        if (animatronics.hasLost()) {
            beginJumpscare();
        }
    }

    private void beginWinSequence() {
        phase = Phase.WIN_SEQUENCE;
        phaseTick = 0;
        audio.stopAll();
    }

    private void updateWinSequence() {
        if (nightHasFinished) {
            return;
        }
        phaseTick++;
        if (phaseTick == WIN_SEQUENCE_CLOCK_FLIP_TICKS) {
            audio.playOnce("bell.wav");
            audio.playOnce("yay.wav");
            saveManager.unlockNight(currentNightNumber + 1);
        }
        if (phaseTick >= WIN_SEQUENCE_TOTAL_TICKS) {
            finishNight();
        }
    }

    private void beginJumpscare() {
        AnimatronicType attackerType = animatronics.getAttackerType();
        jumpscareType = (attackerType != null) ? attackerType : AnimatronicType.FREDDY;
        phase = Phase.JUMPSCARE;
        phaseTick = 0;
        audio.stopAll();
        audio.playOnce(jumpscareType.folder() + "_jumpscare.wav");
    }

    private void updateJumpscare() {
        phaseTick++;
        if (phaseTick >= JUMPSCARE_TICKS) {
            phase = Phase.STATIC;
            phaseTick = 0;
            audio.playOnce("static.wav");
        }
    }

    private void updateStatic() {
        phaseTick++;
        if (phaseTick >= STATIC_TICKS) {
            phase = Phase.DEAD;
            phaseTick = 0;
            audio.stop("static.wav");
        }
    }

    private void updateDead() {
        if (nightHasFinished) {
            return;
        }
        phaseTick++;
        if (phaseTick >= DEAD_TICKS) {
            finishNight();
        }
    }

    private void finishNight() {
        nightHasFinished = true;
        stop();
        if (exitToMenuAction != null) {
            exitToMenuAction.run();
        }
    }

    public synchronized void render() {
        if (phase == Phase.PLAYING || phase == Phase.WIN_SEQUENCE) {
            renderer.render(getActiveCamera(), animatronics.sprites());
        }
    }

    private AnimatronicType cameraAnimatronic(int cameraIndex) {
        int[] cameraTile = gameMap.cameraTiles[cameraIndex];
        int[] curtainTile = gameMap.PirateCoveCurtain[0];
        boolean looksAtPirateCove = cameraTile[0] == curtainTile[0]
            && cameraTile[1] == curtainTile[1] + 1;
        return looksAtPirateCove ? AnimatronicType.FOXY : null;
    }

    private void updateCameraPan() {
        cameraPanTick = (cameraPanTick + 1) % (CAMERA_PAN_HALF_TICKS * 2);
        double halfSweep = (double) cameraPanTick / CAMERA_PAN_HALF_TICKS;
        double sweepProgress = halfSweep <= 1.0 ? halfSweep : 2.0 - halfSweep;
        double panAngle = CAMERA_PAN_RADIANS * (2.0 * sweepProgress - 1.0);

        double panDirectionX = cameraBaseDirectionX * Math.cos(panAngle) - cameraBaseDirectionY * Math.sin(panAngle);
        double panDirectionY = cameraBaseDirectionX * Math.sin(panAngle) + cameraBaseDirectionY * Math.cos(panAngle);
        tabletCamera.faceTowards(
            tabletCamera.positionX + panDirectionX * GameMap.TILE_SIZE,
            tabletCamera.positionY + panDirectionY * GameMap.TILE_SIZE);
    }

    private void placeCamera(Player camera, int[] tile, double[] lookDirection) {
        int column = tile[0];
        int row = tile[1];
        double lookX = lookDirection[0];
        double lookY = lookDirection[1];
        double lookLength = Math.hypot(lookX, lookY);

        // No direction given for this camera: fall back to the walls around its tile.
        if (lookLength < 1e-6) {
            lookX = (gameMap.getTile(column - 1, row) == 1 ? 1 : 0) - (gameMap.getTile(column + 1, row) == 1 ? 1 : 0);
            lookY = (gameMap.getTile(column, row - 1) == 1 ? 1 : 0) - (gameMap.getTile(column, row + 1) == 1 ? 1 : 0);
            if (lookX == 0 && lookY == 0) {
                lookY = -1;
            }
            lookLength = Math.hypot(lookX, lookY);
        }

        double wallOffsetInTiles = 0.25;
        camera.positionX = (column + 0.5 - lookX / lookLength * wallOffsetInTiles) * GameMap.TILE_SIZE;
        camera.positionY = (row + 0.5 - lookY / lookLength * wallOffsetInTiles) * GameMap.TILE_SIZE;
        camera.faceTowards(camera.positionX + lookX * GameMap.TILE_SIZE, camera.positionY + lookY * GameMap.TILE_SIZE);
        camera.setMaxYawRadians(Math.toRadians(90));

        cameraBaseDirectionX = lookX / lookLength;
        cameraBaseDirectionY = lookY / lookLength;
        cameraPanTick = 0;
    }

    private Player getActiveCamera() {
        return currentView == View.OFFICE ? player : tabletCamera;
    }

    public boolean isTablet() { return currentView == View.TABLET; }
    public boolean isShowingMinimapDebug() { return showMinimapDebug; }
    public int getCurrentCameraIndex() { return currentCameraIndex; }
    public boolean isLeftDoorClosed() { return inputState.leftDoorClosed; }
    public boolean isRightDoorClosed() { return inputState.rightDoorClosed; }
    public BufferedImage getRenderedImage() { return renderer.getImage(); }
    public Player getPlayer() { return player; }
    public GameMap getGameMap() { return gameMap; }
    public AnimatronicManager getAnimatronicManager() { return animatronics; }
}
