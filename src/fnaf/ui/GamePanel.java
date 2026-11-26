package fnaf.ui;

import fnaf.assets.JumpscareAnimation;
import fnaf.game.Game;
import fnaf.debug.DebugOverlay;
import fnaf.render.Renderer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.JPanel;

public class GamePanel extends JPanel {
    private static final String[] CAMERA_NAMES = {
        "CAM 1A - Show Stage", "CAM 1B - Dining Area", "CAM 1C - Pirate Cove",
        "CAM 2A - West Hall", "CAM 2B - West Hall Corner", "CAM 3 - Supply Closet",
        "CAM 4A - East Hall", "CAM 4B - East Hall Corner", "CAM 5 - Backstage",
        "CAM 6 - Kitchen", "CAM 7 - Restrooms"
    };

    private final Game game;
    private final DebugOverlay debugOverlay;
    private final Random staticRandom = new Random();

    private int framesDrawnThisSecond = 0;
    private int displayedFps = 0;
    private long lastFpsUpdateTimestamp = System.nanoTime();

    public GamePanel(Game game, Runnable exitToMenu) {
        this.game = game;
        debugOverlay = new DebugOverlay(game);

        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocus();

        // Hide the mouse cursor
        BufferedImage blankCursorImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            blankCursorImage, new Point(0, 0), "blank cursor");
        setCursor(blankCursor);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    exitToMenu.run();
                }
            }
        });

        game.attachInput(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics = (Graphics2D) g;

        // THIS IS THE MAGIC LINE: Forces crisp pixels instead of blurry scaling
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        game.render();
        BufferedImage renderedImage = game.getRenderedImage();
        int renderWidth = renderedImage.getWidth();
        int renderHeight = renderedImage.getHeight();

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        double targetAspectRatio = (double) renderWidth / renderHeight;

        int scaledImageWidth = panelWidth;
        int scaledImageHeight = (int) (scaledImageWidth / targetAspectRatio);
        if (scaledImageHeight > panelHeight) {
            scaledImageHeight = panelHeight;
            scaledImageWidth = (int) (scaledImageHeight * targetAspectRatio);
        }

        int letterboxOffsetX = (panelWidth - scaledImageWidth) / 2;
        int letterboxOffsetY = (panelHeight - scaledImageHeight) / 2;

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, panelWidth, panelHeight);

        graphics.drawImage(renderedImage,
            letterboxOffsetX, letterboxOffsetY,
            letterboxOffsetX + scaledImageWidth, letterboxOffsetY + scaledImageHeight,
            0, 0, renderWidth, renderHeight, null);

        // --- FLASHLIGHT / VIGNETTE EFFECT ---
        Graphics2D lightLayer = (Graphics2D) graphics.create();
        lightLayer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = letterboxOffsetX + scaledImageWidth / 2;
        int centerY = letterboxOffsetY + scaledImageHeight / 2;

        float radius = scaledImageWidth * 1.2f;
        float[] colorStops = new float[] {0.0f, 0.5f, 1.0f};
        Color[] colors = new Color[] {
            new Color(0, 0, 0, 60),
            new Color(0, 0, 0, 120),
            new Color(0, 0, 0, 220)
        };

        RadialGradientPaint paint = new RadialGradientPaint(
            new Point(centerX, centerY), radius, colorStops, colors);

        lightLayer.setPaint(paint);
        lightLayer.fillRect(letterboxOffsetX, letterboxOffsetY, scaledImageWidth, scaledImageHeight);
        lightLayer.dispose();

        // --- HUD SCALING ---
        // Scale the HUD and Minimap so they aren't tiny on a 1080p monitor
        double hudScale = (double) scaledImageWidth / Renderer.RENDER_WIDTH;

        Graphics2D hudGraphics = (Graphics2D) graphics.create();
        hudGraphics.translate(letterboxOffsetX, letterboxOffsetY);
        hudGraphics.scale(hudScale, hudScale);

        debugOverlay.drawMinimap(hudGraphics);

        framesDrawnThisSecond++;
        long now = System.nanoTime();
        if (now - lastFpsUpdateTimestamp >= 1000000000L) {
            displayedFps = framesDrawnThisSecond;
            framesDrawnThisSecond = 0;
            lastFpsUpdateTimestamp = now;
        }

        hudGraphics.setColor(Color.WHITE);
        hudGraphics.setFont(new Font("Monospaced", Font.BOLD, 16));
        hudGraphics.drawString("FPS: " + displayedFps, 20, 30);

        // Clock, top centre, like the FNAF 1 HUD
        String clockText = game.getDisplayedClockHour() + " AM";
        hudGraphics.setFont(new Font("Monospaced", Font.BOLD, 24));
        FontMetrics clockMetrics = hudGraphics.getFontMetrics();
        hudGraphics.drawString(clockText,
            (Renderer.RENDER_WIDTH - clockMetrics.stringWidth(clockText)) / 2, 40);

        // FNAF HUD (Camera Name, Doors, Flashlight)
        if (game.isTabletUp()) {
            hudGraphics.setColor(Color.WHITE);
            hudGraphics.setFont(new Font("Monospaced", Font.BOLD, 24));
            hudGraphics.drawString(CAMERA_NAMES[game.getCameraIndex()],
                20, Renderer.RENDER_HEIGHT - 20);
        } else {
            int barWidth = 12;
            int barHeight = Renderer.RENDER_HEIGHT / 3;
            int barTopY = (Renderer.RENDER_HEIGHT - barHeight) / 2;

            hudGraphics.setColor(game.isLeftDoorClosed() ? new Color(60, 200, 60) : new Color(200, 60, 60));
            hudGraphics.fillRect(0, barTopY, barWidth, barHeight);

            hudGraphics.setColor(game.isRightDoorClosed() ? new Color(60, 200, 60) : new Color(200, 60, 60));
            hudGraphics.fillRect(Renderer.RENDER_WIDTH - barWidth, barTopY, barWidth, barHeight);
        }

        hudGraphics.dispose();

        drawPhaseOverlay(graphics, panelWidth, panelHeight);
    }

    private void drawPhaseOverlay(Graphics2D graphics, int panelWidth, int panelHeight) {
        switch (game.getPhase()) {
            case NIGHT_INTRO: {
                float brightness = game.getNightIntroBrightness();
                graphics.setColor(new Color(0, 0, 0, (int) (255 * (1.0f - brightness))));
                graphics.fillRect(0, 0, panelWidth, panelHeight);

                String nightMessage = "NIGHT " + game.getCurrentNightNumber();
                graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, 72f));
                FontMetrics nightMetrics = graphics.getFontMetrics();
                graphics.setColor(new Color(255, 255, 255, (int) (255 * brightness)));
                graphics.drawString(nightMessage,
                    (panelWidth - nightMetrics.stringWidth(nightMessage)) / 2,
                    panelHeight / 2 + nightMetrics.getAscent() / 2);
                break;
            }
            case PLAYING:
                break;
            case WIN_SEQUENCE: {
                String message = "6 AM, You Survived!";
                graphics.setColor(new Color(0, 0, 0, 200));
                graphics.fillRect(0, 0, panelWidth, panelHeight);
                graphics.setColor(Color.GREEN);
                graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, 72f));
                FontMetrics metrics = graphics.getFontMetrics();
                graphics.drawString(message,
                    (panelWidth - metrics.stringWidth(message)) / 2,
                    panelHeight / 2 + metrics.getAscent() / 2);
                break;
            }
            case JUMPSCARE: {
                graphics.setColor(Color.BLACK);
                graphics.fillRect(0, 0, panelWidth, panelHeight);
                JumpscareAnimation jumpscareAnimation = game.getJumpscareAnimation();
                if (jumpscareAnimation != null) {
                    BufferedImage jumpscareImage = jumpscareAnimation.getFrameForTick(game.getPhaseTick());
                    if (jumpscareImage != null) {
                        drawScaledToFit(graphics, jumpscareImage, 0, 0, panelWidth, panelHeight);
                    }
                }
                break;
            }
            case STATIC: {
                graphics.setColor(Color.BLACK);
                graphics.fillRect(0, 0, panelWidth, panelHeight);
                drawStaticNoise(graphics, 0, 0, panelWidth, panelHeight);
                break;
            }
            case DEAD: {
                String message = "YOU DIED";
                graphics.setColor(Color.BLACK);
                graphics.fillRect(0, 0, panelWidth, panelHeight);
                graphics.setColor(Color.RED);
                graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, 72f));
                FontMetrics metrics = graphics.getFontMetrics();
                graphics.drawString(message,
                    (panelWidth - metrics.stringWidth(message)) / 2,
                    panelHeight / 2 + metrics.getAscent() / 2);
                break;
            }
        }
    }

    private void drawStaticNoise(Graphics2D graphics, int startX, int startY, int width, int height) {
        staticRandom.setSeed(System.nanoTime());
        int dotCount = Math.max(2000, width * height / 900);
        for (int dotIndex = 0; dotIndex < dotCount; dotIndex++) {
            int grayLevel = staticRandom.nextInt(256);
            graphics.setColor(new Color(grayLevel, grayLevel, grayLevel));
            graphics.fillRect(startX + staticRandom.nextInt(width),
                startY + staticRandom.nextInt(height), 2, 2);
        }
    }

    private void drawScaledToFit(Graphics2D graphics, BufferedImage image,
                                 int boxX, int boxY, int boxWidth, int boxHeight) {
        double imageAspectRatio = (double) image.getWidth() / image.getHeight();
        int drawWidth = boxWidth;
        int drawHeight = (int) (drawWidth / imageAspectRatio);
        if (drawHeight > boxHeight) {
            drawHeight = boxHeight;
            drawWidth = (int) (drawHeight * imageAspectRatio);
        }
        graphics.drawImage(image,
            boxX + (boxWidth - drawWidth) / 2, boxY + (boxHeight - drawHeight) / 2,
            drawWidth, drawHeight, null);
    }
}
