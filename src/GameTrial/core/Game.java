package GameTrial.core;

import GameTrial.assets.TextureSet;
import GameTrial.entity.Player;
import GameTrial.entity.Sprite;
import GameTrial.entity.Entity;
import java.util.ArrayList;
import java.util.List;
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
    private final GameMap gameMap;
    private final Raycaster raycaster;
    private final Player player;
    private final Renderer renderer;
    private final InputState inputState;
    private final KeyboardInput keyboardInput;
    private final MouseInput mouseInput;
    private volatile boolean hasWon = false;
    private volatile boolean hasLost = false;
    private final List<Sprite> sprites = new ArrayList<>();
    private final Entity entity;
    private final Player tabletCamera;

    private enum View { OFFICE, TABLET }
    private View currentView = View.OFFICE;

    public Game() {
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

            // placeholder animatronic: walks between the 9 camera spots
            List<int[]> patrolTiles = new ArrayList<>();
            for (int[] cameraTile : gameMap.cameraTiles) {
                patrolTiles.add(cameraTile);
            }
            int[] startTile = gameMap.cameraTiles[gameMap.cameraTiles.length - 1];
            entity = new Entity((startTile[0] + 0.5) * GameMap.TILE_SIZE,
                (startTile[1] + 0.5) * GameMap.TILE_SIZE, textureSet, patrolTiles);
            sprites.add(entity);
        } catch (IOException exception) {
            throw new RuntimeException("Could not load a texture: " + exception.getMessage(), exception);
        }
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
        GameLoop gameLoop = new GameLoop(() -> {
            update();
            repaintAction.run();
        });
        gameLoop.start();
    }

    private synchronized void update() {
        if (hasLost || hasWon) {
            return;
        }
        if (!entity.isAttacking()) {
            if (inputState.tabletTogglePressed) {
                inputState.tabletTogglePressed = false;
                currentView = (currentView == View.OFFICE) ? View.TABLET : View.OFFICE;
            }
            getActiveCamera().update(inputState);
        }
        boolean playerIsMoving = false; // guard never moves
        entity.update(gameMap, player, playerIsMoving);
        if (!hasWon && entity.isAttackFinished()) {
            hasLost = true;
        }
    }

    public synchronized void render() {
        renderer.render(getActiveCamera(), sprites);
    }

    // puts a camera in a tile, aims it away from the nearby walls and limits how far it can pan:
    // corner = 90 degrees in total, flat wall = 180 degrees in total
    private void placeCamera(Player camera, int[] tile) {
        int column = tile[0];
        int row = tile[1];
        int lookX = (gameMap.getTile(column - 1, row) == 1 ? 1 : 0) - (gameMap.getTile(column + 1, row) == 1 ? 1 : 0);
        int lookY = (gameMap.getTile(column, row - 1) == 1 ? 1 : 0) - (gameMap.getTile(column, row + 1) == 1 ? 1 : 0);
        if (lookX == 0 && lookY == 0) {
            lookY = -1; // no clear wall nearby: face north
        }
        double lookLength = Math.hypot(lookX, lookY);
        double wallOffsetInTiles = 0.25; // moves the camera toward the wall without touching it
        camera.positionX = (column + 0.5 - lookX / lookLength * wallOffsetInTiles) * GameMap.TILE_SIZE;
        camera.positionY = (row + 0.5 - lookY / lookLength * wallOffsetInTiles) * GameMap.TILE_SIZE;
        camera.faceTowards(camera.positionX + lookX * GameMap.TILE_SIZE, camera.positionY + lookY * GameMap.TILE_SIZE);
        boolean isCorner = lookX != 0 && lookY != 0;
        camera.setMaxYawRadians(Math.toRadians(isCorner ? 45 : 90));
    }

    private Player getActiveCamera() {
        return currentView == View.OFFICE ? player : tabletCamera;
    }

    public boolean hasWon() {
        return hasWon;
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

}