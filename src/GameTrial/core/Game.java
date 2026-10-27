package GameTrial.core;

import GameTrial.assets.TextureSet;
import GameTrial.entity.Player;
import GameTrial.input.InputState;
import GameTrial.input.KeyboardInput;
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

    public Game() {
        gameMap = new GameMap();
        raycaster = new Raycaster(gameMap);
        player = new Player();
        inputState = new InputState();
        keyboardInput = new KeyboardInput(inputState);

        try {
            renderer = new Renderer(raycaster, new TextureSet());
        } catch (IOException exception) {
            throw new RuntimeException("Could not load a texture: " + exception.getMessage(), exception);
        }
    }

    public void attachInput(Component screenComponent) {
        screenComponent.addKeyListener(keyboardInput);
        screenComponent.addFocusListener(keyboardInput);
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
    }

    public synchronized void render() {
        renderer.render(player);
    }

    public BufferedImage getRenderedImage() {
        return renderer.getImage();
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public Raycaster getRaycaster() {
        return raycaster;
    }

    public Player getPlayer() {
        return player;
    }
}