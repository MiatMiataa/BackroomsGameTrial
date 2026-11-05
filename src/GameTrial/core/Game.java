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
    private volatile boolean hasLost = false;
    private final List<Sprite> sprites = new ArrayList<>();
    private final Entity entity;

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
            TextureSet textureSet = new TextureSet();
            renderer = new Renderer(raycaster, textureSet);

            sprites.add(new Sprite(player.positionX + player.directionX * 120,
                player.positionY + player.directionY * 120,
                textureSet.itemHealth, 0.5));

            sprites.add(new Sprite(player.positionX - player.directionX * 120,
                player.positionY - player.directionY * 120,
                textureSet.itemHealth, 0.5));

            int[] entityTile = findHalfwayTile(generatedLevel);
            List<int[]> patrolTiles = new ArrayList<>(generatedLevel.exitTiles);

            patrolTiles.add(new int[] {generatedLevel.spawnTileColumn, generatedLevel.spawnTileRow});
            entity = new Entity((entityTile[0] + 0.5) * GameMap.TILE_SIZE, (entityTile[1] + 0.5) * GameMap.TILE_SIZE, textureSet, patrolTiles);
            sprites.add(entity);
        } catch (IOException exception) {
            throw new RuntimeException("Could not load a texture: " + exception.getMessage(), exception);
        }
    }

    private int[] findHalfwayTile(GeneratedLevel generatedLevel) {
        int halfwaySteps = generatedLevel.distanceToExit[generatedLevel.spawnTileRow][generatedLevel.spawnTileColumn] / 2;
        for (int row = 0; row < gameMap.getRowsCount(); row++) {
            for (int column = 0; column < gameMap.getColumnCount(); column++) {
                if (gameMap.isOnPathToExit(column, row) && generatedLevel.distanceToExit[row][column] == halfwaySteps) {
                    return new int[] {column, row};
                }
            }
        }
        return new int[] {generatedLevel.spawnTileColumn, generatedLevel.spawnTileRow};
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
        if (hasLost) {
            return;
        }
        if (!entity.isAttacking()) {
            player.update(gameMap, inputState);
        }
        if (!hasWon) {
            int playerTileColumn = (int) Math.floor(player.positionX / GameMap.TILE_SIZE);
            int playerTileRow = (int) Math.floor(player.positionY / GameMap.TILE_SIZE);
            if (playerTileColumn == gameMap.exitTileColumn && playerTileRow == gameMap.exitTileRow) {
                hasWon = true;
            }
        }
        boolean playerIsMoving = inputState.forward != 0 || inputState.strafe != 0;
        entity.update(gameMap, player, playerIsMoving);
        if (!hasWon && entity.isAttackFinished()) {
            hasLost = true;
        }
    }

    public synchronized void render() {
        renderer.render(player, sprites);
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