package GameTrial.core;

import GameTrial.assets.TextureSet;
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
import GameTrial.core.NightConfig;
import GameTrial.entity.Animatronic;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Game {
    private final GameMap gameMap;
    private final Raycaster raycaster;
    private final Player player;
    private final Renderer renderer;
    private final InputState inputState;
    private final KeyboardInput keyboardInput;
    private final MouseInput mouseInput;
    private final AnimatronicManager animatronics;
    private final ThreatContext threatContext = new ThreatContext();
    private final Player tabletCamera;

    private int currentCameraIndex = 0;
    private volatile boolean hasWon = false;
    private volatile boolean hasLost = false;

    private enum View { OFFICE, TABLET }
    private View currentView = View.OFFICE;

    private static final int TICKS_PER_HOUR  = 5340;   // about 89 s at 60 FPS
    private static final int TICKS_PER_NIGHT = TICKS_PER_HOUR * 6;   // 32,040

    private final int currentNightNumber;
    private int freddyAILevel;
    private int bonnieAILevel;
    private int chicaAILevel;
    private int foxyAILevel;
    private int lastHourIndex = 0;

    public Game() { this(1); }

    public Game(int startNightNumber) {
        this.currentNightNumber = Math.max(1, Math.min(5, startNightNumber));
        freddyAILevel = NightConfig.freddyAILevel(currentNightNumber);
        bonnieAILevel = NightConfig.bonnieAILevel(currentNightNumber);
        chicaAILevel = NightConfig.chicaAILevel(currentNightNumber);
        foxyAILevel = NightConfig.foxyAILevel(currentNightNumber);
        goldenFreddyAILevel = NightConfig.goldenFreddyAILevel(currentNightNumber);

        gameMap = new GameMap();
        raycaster = new Raycaster(gameMap);

        player = new Player();
        player.positionX = (gameMap.guardColumn + 0.5) * GameMap.TILE_SIZE;
        player.positionY = (gameMap.guardRow + 0.5) * GameMap.TILE_SIZE;
        player.faceTowards(player.positionX, player.positionY - GameMap.TILE_SIZE);

        tabletCamera = new Player();
        placeCamera(tabletCamera, gameMap.cameraTiles[0]);

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
    }

    public boolean hasWon() {
        return hasWon;
    }
    public boolean hasLost() {
        return hasLost;
    }

    public void attachInput(Component screenComponent) {
        screenComponent.addKeyListener(keyboardInput);
        screenComponent.addFocusListener(keyboardInput);
        screenComponent.addMouseListener(mouseInput);
        screenComponent.addMouseMotionListener(mouseInput);
        screenComponent.addFocusListener(mouseInput);
    }

    public void start(Runnable repaintAction) {
        GameLoop gameLoop = new GameLoop(() -> { update(); repaintAction.run(); });
        gameLoop.start();
    }

    private void applyHourlyAIGains(int hourIndex) {
        boolean gainsHappened = false;
        if (hourIndex == 2) {                 // 2 AM
            bonnieAILevel++;
            gainsHappened = true;
        } else if (hourIndex == 3 || hourIndex == 4) {   // 3 AM and 4 AM
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
        int hourIndex = (int) (threatContext.tick / TICKS_PER_HOUR);
        return hourIndex == 0 ? 12 : hourIndex;   // 12, 1, 2, 3, 4, 5, 6
    }

    private synchronized void update() {
        if (hasLost || hasWon) return;

        if (threatContext.tick >= TICKS_PER_NIGHT) {
            hasWon = true;
            return;
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
                currentCameraIndex = Math.min(inputState.requestedCamera,
                    gameMap.cameraTiles.length - 1);
                inputState.requestedCamera = -1;
                placeCamera(tabletCamera, gameMap.cameraTiles[currentCameraIndex]);
            }
            getActiveCamera().update(inputState);
        }

        threatContext.tabletUp = currentView == View.TABLET;
        threatContext.viewingCamera = threatContext.tabletUp
            ? cameraAnimatronic(currentCameraIndex)
            : null;
        threatContext.leftDoorClosed = inputState.leftDoorClosed;
        threatContext.rightDoorClosed = inputState.rightDoorClosed;
        threatContext.tick++;

        animatronics.update(gameMap, player, threatContext);
        if (animatronics.hasLost()) {
            hasLost = true;
        }
    }

    public synchronized void render() {
        renderer.render(getActiveCamera(),
        animatronics.sprites());
    }

    private AnimatronicType cameraAnimatronic(int cameraIndex) {
        int[] tile = gameMap.cameraTiles[cameraIndex];
        int[] cove = gameMap.foxyPath[0];
        if (tile[0] == cove[0] && tile[1] == cove[1]) {
            return AnimatronicType.FOXY;
        }
        return null;
    }

    private void placeCamera(Player camera, int[] tile) {
        int column = tile[0];
        int row = tile[1];
        int lookX = (gameMap.getTile(column - 1, row) == 1 ? 1 : 0) - (gameMap.getTile(column + 1, row) == 1 ? 1 : 0);
        int lookY = (gameMap.getTile(column, row - 1) == 1 ? 1 : 0) - (gameMap.getTile(column, row + 1) == 1 ? 1 : 0);
        if (lookX == 0 && lookY == 0) {
            lookY = -1;
        }
        double lookLength = Math.hypot(lookX, lookY);
        double wallOffsetInTiles = 0.25;
        camera.positionX = (column + 0.5 - lookX / lookLength * wallOffsetInTiles) * GameMap.TILE_SIZE;
        camera.positionY = (row + 0.5 - lookY / lookLength * wallOffsetInTiles) * GameMap.TILE_SIZE;
        camera.faceTowards(camera.positionX + lookX * GameMap.TILE_SIZE,
                           camera.positionY + lookY * GameMap.TILE_SIZE);
        camera.setMaxYawRadians(Math.toRadians((lookX != 0 && lookY != 0) ? 45 : 90));
    }

    private Player getActiveCamera() {
        return currentView == View.OFFICE ? player : tabletCamera;
    }

        public boolean isTablet() {
        return currentView == View.TABLET;
    }

    public int getCurrentCameraIndex() {
        return currentCameraIndex;
    }

    public boolean isLeftDoorClosed() {
        return inputState.leftDoorClosed;
    }

    public boolean isRightDoorClosed() {
        return inputState.rightDoorClosed;
    }

    public BufferedImage getRenderedImage() {
        return renderer.getImage();
    }
    public Player getPlayer() {
        return player;
    }
    public GameMap getGameMap() {
        return gameMap;
    }
    public AnimatronicManager getAnimatronicManager() {
        return animatronics;
    }
}