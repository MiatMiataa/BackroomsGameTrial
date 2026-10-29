package GameTrial.core;

import GameTrial.assets.TextureSet;
import GameTrial.entity.Player;
import GameTrial.input.InputState;
import GameTrial.input.KeyboardInput;
import GameTrial.input.MouseInput;
import GameTrial.render.Raycaster;
import GameTrial.render.Renderer;
import GameTrial.world.GameMap;
import GameTrial.world.GeneratedLevel;
import GameTrial.world.MapGenerator;
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

    public Game() {
        GeneratedLevel generatedLevel = MapGenerator.generate(MapGenerator.DEFAULT_SEED);
        int[] exitTile = generatedLevel.exitTiles.get(0);

       gameMap = new GameMap(generatedLevel.tileGrid, generatedLevel.distanceToExit,
            generatedLevel.spawnTileColumn, generatedLevel.spawnTileRow,
            exitTile[0], exitTile[1]);

        raycaster = new Raycaster(gameMap);

        player = new Player();
        player.positionX = (generatedLevel.spawnTileColumn + 0.5) * GameMap.TILE_SIZE;
        player.positionY = (generatedLevel.spawnTileRow + 0.5) * GameMap.TILE_SIZE;
        player.faceTowards((exitTile[0] + 0.5) * GameMap.TILE_SIZE,
            (exitTile[1] + 0.5) * GameMap.TILE_SIZE);
        
        inputState = new InputState();

        keyboardInput = new KeyboardInput(inputState);

        mouseInput = new MouseInput(inputState);
        
        try {
            renderer = new Renderer(raycaster, new TextureSet());
        } catch (IOException exception) {
            throw new RuntimeException("Could not load a texture: " + exception.getMessage(), exception);
        }
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
        player.update(gameMap, inputState);
            if (!hasWon) {
                int playerTileColumn = (int) Math.floor(player.positionX / GameMap.TILE_SIZE);
                int playerTileRow = (int) Math.floor(player.positionY / GameMap.TILE_SIZE);
            if (playerTileColumn == gameMap.exitTileColumn && playerTileRow == gameMap.exitTileRow) {
                hasWon = true;
            }
        }
    }

    public synchronized void render() {
        renderer.render(player);
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