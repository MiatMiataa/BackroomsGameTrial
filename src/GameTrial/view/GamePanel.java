package GameTrial.view;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import GameTrial.core.Game;
import GameTrial.debug.DebugOverlay;
import GameTrial.render.Renderer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GamePanel extends JPanel {
    private final Game game;
    private final DebugOverlay debugOverlay;

    private int framesDrawnThisSecond = 0;
    private int displayedFps = 0;
    private long lastFpsUpdateTimestamp = System.nanoTime();

    public GamePanel(Game game, JFrame gameWindow) {
        this.game = game;
        debugOverlay = new DebugOverlay(game.getPlayer(), game.getGameMap());

        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocus();

        // Hide the mouse cursor
        BufferedImage blankCursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            blankCursorImg, new Point(0, 0), "blank cursor");
        setCursor(blankCursor);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    gameWindow.setExtendedState(JFrame.ICONIFIED);
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
        
        float radius;
        float[] dist;
        Color[] colors;

        if (game.isFlashlightOn()) {
            // Flashlight ON: Wide, bright cone that reveals the hallway
            radius = scaledImageWidth * 0.8f; // Much wider
            dist = new float[] {0.0f, 0.6f, 1.0f};
            colors = new Color[] { 
                new Color(0, 0, 0, 0),      // Center is completely clear
                new Color(0, 0, 0, 10),     // Very subtle mid-transition
                new Color(0, 0, 0, 150)     // Edges darken slightly (not pure black)
            };
        } else {
            // Flashlight OFF: Dimly lit office, dark corners
            radius = scaledImageWidth * 1.2f; // Huge radius so it covers the whole room
            dist = new float[] {0.0f, 0.5f, 1.0f};
            colors = new Color[] { 
                new Color(0, 0, 0, 60),     // Office is dimly visible
                new Color(0, 0, 0, 120),    // Hallways get dark
                new Color(0, 0, 0, 220)     // Corners fade to black
            };
        }

        java.awt.RadialGradientPaint paint = new java.awt.RadialGradientPaint(
            new Point(centerX, centerY), radius, dist, colors);
        
        lightLayer.setPaint(paint);
        lightLayer.fillRect(letterboxOffsetX, letterboxOffsetY, scaledImageWidth, scaledImageHeight);
        lightLayer.dispose();

        // --- HUD SCALING ---
        // Scale the HUD and Minimap so they aren't tiny on a 1080p monitor
        double hudScale = (double) scaledImageWidth / Renderer.RENDER_WIDTH;
        
        Graphics2D hudGraphics = (Graphics2D) graphics.create();
        hudGraphics.translate(letterboxOffsetX, letterboxOffsetY);
        hudGraphics.scale(hudScale, hudScale);

        debugOverlay.drawMinimapOverlay(hudGraphics);
        
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

        // FNAF HUD (Camera Name, Doors, Flashlight)
        if (game.isTablet()) {
            String[] camNames = {
                "CAM 1 - Party 3", "CAM 2 - Party 1", "CAM 3 - Party 4", 
                "CAM 4 - Party 2", "CAM 5 - Central Hall", "CAM 6 - Main Hall", 
                "CAM 7 - Dining", "CAM 8 - Backstage", "CAM 9 - Kitchen"
            };
            hudGraphics.setColor(Color.WHITE);
            hudGraphics.setFont(new Font("Monospaced", Font.BOLD, 24));
            hudGraphics.drawString(camNames[game.getCurrentCameraIndex()], 
                20, Renderer.VIEW_HEIGHT - 20);
        } else {
            int barWidth = 30;
            hudGraphics.setColor(game.isLeftDoorClosed() ? new Color(60, 200, 60) : new Color(200, 60, 60));
            hudGraphics.fillRect(0, 0, barWidth, Renderer.VIEW_HEIGHT);
            
            hudGraphics.setColor(game.isRightDoorClosed() ? new Color(60, 200, 60) : new Color(200, 60, 60));
            hudGraphics.fillRect(Renderer.RENDER_WIDTH - barWidth, 0, barWidth, Renderer.VIEW_HEIGHT);
            
            hudGraphics.setColor(game.isFlashlightOn() ? new Color(255, 240, 160) : new Color(80, 80, 80));
            hudGraphics.fillOval(Renderer.RENDER_WIDTH / 2 - 15, 
                Renderer.VIEW_HEIGHT - 50, 30, 30);
        }
        
        hudGraphics.dispose();

        // Game Over Screen
        if (game.hasWon() || game.hasLost()) {
            String message = game.hasWon() 
            ? "6 AM, You Survived!" 
            : "The animatronics killed you";
            graphics.setColor(new Color(0, 0, 0, 200));
            graphics.fillRect(letterboxOffsetX, letterboxOffsetY, scaledImageWidth, scaledImageHeight);
            graphics.setColor(game.hasWon() ? Color.GREEN : Color.RED);
            graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, 72f)); // Bigger text for 1080p
            FontMetrics metrics = graphics.getFontMetrics();
            graphics.drawString(message, (panelWidth - metrics.stringWidth(message)) / 2, panelHeight / 2 + metrics.getAscent() / 2);
        }
    }
}