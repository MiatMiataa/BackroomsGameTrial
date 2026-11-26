package fnaf.game;

import fnaf.assets.JumpscareAnimation;
import fnaf.assets.Texture;
import fnaf.assets.TextureSet;
import fnaf.audio.AudioPlayer;
import fnaf.entity.AnimatronicManager;
import fnaf.entity.AnimatronicType;
import fnaf.entity.Player;
import fnaf.entity.Sprite;
import fnaf.entity.AnimatronicContext;
import fnaf.input.PlayerInput;
import fnaf.input.KeyboardInput;
import fnaf.input.MouseInput;
import fnaf.render.Raycaster;
import fnaf.render.Renderer;
import fnaf.world.GameMap;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    // NIGHT_INTRO is the "NIGHT 1" card; PLAYING is the night itself; the other
    // phases are the win and death screens.
    public enum Phase { NIGHT_INTRO, PLAYING, WIN_SEQUENCE, JUMPSCARE, STATIC, DEAD }

    // FNAF 1 feeds sway side to side: 25 degrees each way (50 total) over 2 seconds.
    private static final double CAMERA_PAN_RADIANS = Math.toRadians(12.5); 
    private static final int CAMERA_PAN_HALF_TICKS = 180;
    private double cameraBaseDirectionX = 0;
    private double cameraBaseDirectionY = -1;
    private int cameraPanTick = 0;

    // 60 ticks per second.
    private static final int NIGHT_INTRO_FADE_TICKS = 100;          // black to the office, 1.7 s
    private static final int NIGHT_INTRO_TOTAL_TICKS = 200;         // fade plus hold, 3.3 s
    private static final int WIN_SEQUENCE_CLOCK_FLIP_TICKS = 120;   // 5 AM held for 2 s
    private static final int WIN_CHEER_DELAY_TICKS = 9 * 60;        // cheer starts 9 s after the chimes
    private static final int WIN_CHEER_TAIL_TICKS = 210;            // the cheer is 3.4 s long
    private static final int WIN_SEQUENCE_TOTAL_TICKS =
        WIN_SEQUENCE_CLOCK_FLIP_TICKS + WIN_CHEER_DELAY_TICKS + WIN_CHEER_TAIL_TICKS;
    private static final int STATIC_TICKS = 120;                    // 2 s
    private static final int FALLBACK_DEAD_TICKS = 240;             // 4 s, only without the robot sound

    // The desk fan two tiles in front of the guard, at column 11 row 15 {11,15}.
    private static final int[] DESK_TILE = {11, 15};
    private static final int DESK_FRAME_TICKS = 10;
    private static final double DESK_HEIGHT_SCALE = 0.9;

    private static final String FAN_SOUND = "11. Office ~ Fan.wav";
    private static final String COMPUTER_ACTIVATION_SOUND = "12. Office ~ Computer Activation.wav";
    private static final String COMPUTER_BLIP_SOUND = "13. Office ~ Computer Blip.wav";
    private static final String CLOCK_CHIMES_SOUND = "18. Office ~ Clock Chimes.wav";
    private static final String CHILDREN_CHEER_SOUND = "26. Children ~ Cheer.wav";
    private static final String DEATH_SCREEN_SOUND = "61. Miscellaneous SFX ~ Robot.wav";
    private static final String JUMPSCARE_SOUND = "AnimatronicScream.wav";
    private static final String STATIC_SOUND = "Static.wav";
    private static final String DOOR_SOUND = "SFXBible_12478.wav";
    private static final String[] MUSIC_TRACKS = {
        "01. Music Track ~ The Sand Temple.wav",
        "02. Music Track ~ In the Depths.wav",
        "03. Music Track ~ Scary Space.wav"
    };

    // The fan and the music are background noise, not the main sound of the night.
    private static final double FAN_VOLUME = 0.35;
    private static final double MUSIC_VOLUME = 0.22;

    private int cameraIndex = 0;
    private boolean minimapDebugVisible = false;

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
    private final AnimatronicContext animatronicContext = new AnimatronicContext();
    private final Player tabletCamera;
    private final Random musicRandom = new Random();
    private String currentMusicTrack = null;

    private GameLoop gameLoop;
    private Runnable exitToMenuAction;
    private Runnable nextNightAction;
    private Phase phase = Phase.NIGHT_INTRO;
    private int phaseTick = 0;
    private AnimatronicType jumpscareType = AnimatronicType.FREDDY;
    private volatile JumpscareAnimation jumpscareAnimation = null;
    private int jumpscareTickLimit = 0;
    private int deathTickLimit = FALLBACK_DEAD_TICKS;
    private boolean nightFinished = false;
    private boolean nightWon = false;
    private boolean lastRightDoorClosed = false;
    private boolean lastLeftDoorClosed = false;

    private final GameMap gameMap;
    private final Raycaster raycaster;
    private final Player player;
    private final Renderer renderer;
    private final PlayerInput playerInput;
    private final KeyboardInput keyboardInput;
    private final MouseInput mouseInput;
    private final AnimatronicManager animatronics;
    private final Texture[] deskFrames;
    private final Sprite deskFan;

    public Game() { this(1); }

    public Game(int startNightNumber) {
        this.currentNightNumber = Math.max(1, Math.min(5, startNightNumber));
        freddyAILevel = NightSettings.freddyAILevel(currentNightNumber);
        bonnieAILevel = NightSettings.bonnieAILevel(currentNightNumber);
        chicaAILevel = NightSettings.chicaAILevel(currentNightNumber);
        foxyAILevel = NightSettings.foxyAILevel(currentNightNumber);

        gameMap = new GameMap();
        raycaster = new Raycaster(gameMap);

        player = new Player();
        player.positionX = (gameMap.guardColumn + 0.5) * GameMap.TILE_SIZE;
        player.positionY = (gameMap.guardRow + 0.5) * GameMap.TILE_SIZE;
        player.faceTowards(player.positionX, player.positionY - GameMap.TILE_SIZE);

        tabletCamera = new Player();
        placeCamera(tabletCamera, gameMap.cameraTiles[0], gameMap.cameraLookDirections[0]);

        playerInput = new PlayerInput();
        keyboardInput = new KeyboardInput(playerInput);
        mouseInput = new MouseInput(playerInput);

        try {
            TextureSet textureSet = new TextureSet();
            renderer = new Renderer(raycaster, textureSet);
            animatronics = new AnimatronicManager(gameMap, textureSet, audio);
            deskFrames = textureSet.deskFrames;
            deskFan = new Sprite((DESK_TILE[0] + 0.5) * GameMap.TILE_SIZE,
                (DESK_TILE[1] + 0.5) * GameMap.TILE_SIZE, deskFrames[0], DESK_HEIGHT_SCALE);
        } catch (IOException exception) {
            throw new RuntimeException("Could not load a texture: " + exception.getMessage(), exception);
        }

        animatronics.setAILevels(freddyAILevel, bonnieAILevel, chicaAILevel, foxyAILevel);
    }

    public boolean hasWon() { return phase == Phase.WIN_SEQUENCE; }
    public boolean hasLost() {
        if (nightWon) {
            return false;
        }
        return phase == Phase.JUMPSCARE || phase == Phase.STATIC || phase == Phase.DEAD;
    }
    public Phase getPhase() { return phase; }
    public AnimatronicType getJumpscareType() { return jumpscareType; }
    public JumpscareAnimation getJumpscareAnimation() { return jumpscareAnimation; }

    /** Called when the death screen has finished. */
    public void setExitToMenuAction(Runnable exitToMenuAction) {
        this.exitToMenuAction = exitToMenuAction;
    }

    /** Called when a won night has finished, so the next night can start. */
    public void setNextNightAction(Runnable nextNightAction) {
        this.nextNightAction = nextNightAction;
    }

    public void attachInput(Component screenComponent) {
        screenComponent.addKeyListener(keyboardInput);
        screenComponent.addFocusListener(keyboardInput);
        screenComponent.addMouseListener(mouseInput);
        screenComponent.addMouseMotionListener(mouseInput);
        screenComponent.addFocusListener(mouseInput);
    }
    
    public void start(Runnable repaintAction) {
        gameLoop = new GameLoop(() -> {
            update();
            repaintAction.run();
        });
        gameLoop.start();
    }

    // Stops the night. Called when the player goes back to the menu.
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
        int hourIndex = (int) (animatronicContext.tick / TICKS_PER_HOUR);
        return hourIndex == 0 ? 12 : Math.min(hourIndex, 6);
    }

    /** 0 is black, 1 is fully lit: the fade of the "NIGHT 1" card. */
    public float getNightIntroBrightness() {
        float fadeProgress = (float) phaseTick / NIGHT_INTRO_FADE_TICKS;
        return Math.max(0.0f, Math.min(1.0f, fadeProgress));
    }

    public synchronized int getPhaseTick() { return phaseTick; }

    private synchronized void update() {
        switch (phase) {
            case NIGHT_INTRO:
                updateNightIntro();
                break;
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

    private void updateNightIntro() {
        phaseTick++;
        if (phaseTick >= NIGHT_INTRO_TOTAL_TICKS) {
            phase = Phase.PLAYING;
            phaseTick = 0;
            lastLeftDoorClosed = playerInput.leftDoorClosed;
            lastRightDoorClosed = playerInput.rightDoorClosed;
            startNightAudio();
        }
    }

    private void startNightAudio() {
        audio.playLooping(FAN_SOUND, FAN_VOLUME);
        playRandomMusicTrack();
    }

    /** Picks a different track each time, so the three tracks keep the night company. */
    private void playRandomMusicTrack() {
        String nextTrack;
        do {
            nextTrack = MUSIC_TRACKS[musicRandom.nextInt(MUSIC_TRACKS.length)];
        } while (MUSIC_TRACKS.length > 1 && nextTrack.equals(currentMusicTrack));
        currentMusicTrack = nextTrack;
        audio.playOnce(nextTrack, MUSIC_VOLUME);
    }

    private void updatePlaying() {
        if (animatronicContext.tick >= TICKS_PER_NIGHT - 1) {
            beginWinSequence();
            return;
        }

        playDoorSoundIfStateChanged();

        if (playerInput.toggleMinimapDebug) {
            playerInput.toggleMinimapDebug = false;
            minimapDebugVisible = !minimapDebugVisible;
        }

        int currentHourIndex = (int) (animatronicContext.tick / TICKS_PER_HOUR);
        while (lastHourIndex < currentHourIndex) {
            lastHourIndex++;
            applyHourlyAIGains(lastHourIndex);
        }

        if (!animatronics.isAnyAttacking()) {
            if (playerInput.tabletTogglePressed) {
                playerInput.tabletTogglePressed = false;
                if (currentView == View.OFFICE) {
                    currentView = View.TABLET;
                    audio.stop(FAN_SOUND);
                    audio.playOnce(COMPUTER_ACTIVATION_SOUND);
                } else {
                    currentView = View.OFFICE;
                    audio.playLooping(FAN_SOUND, FAN_VOLUME);
                }
            }
            if (currentView == View.TABLET && playerInput.selectedCamera >= 0) {
                int selectedCameraIndex = Math.min(playerInput.selectedCamera, gameMap.cameraTiles.length - 1);
                playerInput.selectedCamera = -1;
                if (selectedCameraIndex != cameraIndex) {
                    cameraIndex = selectedCameraIndex;
                    placeCamera(tabletCamera, gameMap.cameraTiles[cameraIndex],
                        gameMap.cameraLookDirections[cameraIndex]);
                    audio.playOnce(COMPUTER_BLIP_SOUND);
                }
            }
            if (currentView == View.TABLET) {
                updateCameraPan();
            } else {
                player.update(playerInput);
            }
        }

        animatronicContext.tabletUp = currentView == View.TABLET;
        animatronicContext.viewingCamera = animatronicContext.tabletUp ? animatronicOnCamera(cameraIndex) : null;
        animatronicContext.viewingCameraTile = animatronicContext.tabletUp ? gameMap.cameraTiles[cameraIndex] : null;
        animatronicContext.leftDoorClosed = playerInput.leftDoorClosed;
        animatronicContext.rightDoorClosed = playerInput.rightDoorClosed;
        animatronicContext.tick++;

        // The desk fan only turns while it is actually running.
        int deskFrameIndex = (int) (animatronicContext.tick / DESK_FRAME_TICKS) % deskFrames.length;
        deskFan.texture = deskFrames[deskFrameIndex];

        // The doors follow the guard's switches; Foxy sets the curtain target himself.
        gameMap.leftDoorBarrier.targetOpenAmount = playerInput.leftDoorClosed ? 0.0 : 1.0;
        gameMap.rightDoorBarrier.targetOpenAmount = playerInput.rightDoorClosed ? 0.0 : 1.0;

        if (!audio.isPlaying(currentMusicTrack)) {
            playRandomMusicTrack();
        }

        animatronics.update(gameMap, player, animatronicContext);
        gameMap.updateBarriers();
        if (animatronics.hasLost()) {
            beginJumpscare();
        }
    }

    private void playDoorSoundIfStateChanged() {
        boolean leftChanged = playerInput.leftDoorClosed != lastLeftDoorClosed;
        boolean rightChanged = playerInput.rightDoorClosed != lastRightDoorClosed;

        if (leftChanged || rightChanged) {
            audio.playOnce(DOOR_SOUND);
        }

        lastLeftDoorClosed = playerInput.leftDoorClosed;
        lastRightDoorClosed = playerInput.rightDoorClosed;
    }

    private void beginWinSequence() {
        phase = Phase.WIN_SEQUENCE;
        phaseTick = 0;
        nightWon = true;
        audio.stopAll();
    }

    private void updateWinSequence() {
        if (nightFinished) {
            return;
        }
        phaseTick++;
        if (phaseTick == WIN_SEQUENCE_CLOCK_FLIP_TICKS) {
            audio.playOnce(CLOCK_CHIMES_SOUND);
            saveManager.unlockNight(currentNightNumber + 1);
        }
        if (phaseTick == WIN_SEQUENCE_CLOCK_FLIP_TICKS + WIN_CHEER_DELAY_TICKS) {
            audio.playOnce(CHILDREN_CHEER_SOUND);
        }
        if (phaseTick >= WIN_SEQUENCE_TOTAL_TICKS) {
            beginStatic();
        }
    }

    private void beginJumpscare() {
        AnimatronicType attackerType = animatronics.getAttackerType();
        jumpscareType = (attackerType != null) ? attackerType : AnimatronicType.FREDDY;
        jumpscareAnimation = new JumpscareAnimation(jumpscareType);
        jumpscareTickLimit = jumpscareAnimation.getTotalDurationInTicks();
        phase = Phase.JUMPSCARE;
        phaseTick = 0;
        audio.stopAll();
        audio.playOnce(JUMPSCARE_SOUND);
    }
    
    private void updateJumpscare() {
        phaseTick++;
        if (phaseTick >= jumpscareTickLimit) {
            audio.stop(JUMPSCARE_SOUND);
            beginStatic();
        }
    }

    private void beginStatic() {
        phase = Phase.STATIC;
        phaseTick = 0;
        audio.playOnce(STATIC_SOUND);
    }

    private void updateStatic() {
        phaseTick++;

        if (phaseTick >= STATIC_TICKS) {
            audio.stop(STATIC_SOUND);
            
            if (nightWon) {
                finishNight();
            } else {
                beginDeath();
            }
        }
    }

    private void beginDeath() {
        phase = Phase.DEAD;
        phaseTick = 0;
        audio.playOnce(DEATH_SCREEN_SOUND);
        long deathScreenMillis = audio.getDurationMillis(DEATH_SCREEN_SOUND);
        if (deathScreenMillis <= 0) {
            deathTickLimit = FALLBACK_DEAD_TICKS;
            return;
        }
        deathTickLimit = (int) Math.max(60, deathScreenMillis * 60 / 1000);
    }

    private void updateDead() {
        if (nightFinished) {
            return;
        }
        phaseTick++;
        if (phaseTick >= deathTickLimit) {
            finishNight();
        }
    }

    private void finishNight() {
        nightFinished = true;
        stop();
        if (nightWon && nextNightAction != null) {
            nextNightAction.run();
        } else if (exitToMenuAction != null) {
            exitToMenuAction.run();
        }
    }

    public synchronized void render() {
        if (phase != Phase.PLAYING && phase != Phase.WIN_SEQUENCE && phase != Phase.NIGHT_INTRO) {
            return;
        }
        List<Sprite> sprites = new ArrayList<>(animatronics.sprites());
        sprites.add(deskFan);
        renderer.render(getActiveCamera(), sprites);
    }

    private AnimatronicType animatronicOnCamera(int cameraIndex) {
        int[] cameraTile = gameMap.cameraTiles[cameraIndex];
        int[] curtainTile = gameMap.pirateCoveCurtainTile[0];
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

    public boolean isTabletUp() { return currentView == View.TABLET; }
    public boolean isShowingMinimapDebug() { return minimapDebugVisible; }
    public int getCameraIndex() { return cameraIndex; }
    public boolean isLeftDoorClosed() { return playerInput.leftDoorClosed; }
    public boolean isRightDoorClosed() { return playerInput.rightDoorClosed; }
    public BufferedImage getRenderedImage() { return renderer.getImage(); }
    public Player getPlayer() { return player; }
    public GameMap getGameMap() { return gameMap; }
    public AnimatronicManager getAnimatronicManager() { return animatronics; }
}
