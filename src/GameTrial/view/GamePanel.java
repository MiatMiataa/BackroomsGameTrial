package GameTrial.view;

import GameTrial.core.Game;
import GameTrial.debug.DebugOverlay;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
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
        debugOverlay = new DebugOverlay(game.getPlayer(), game.getGameMap(), game.getRaycaster());

        setBackground(Color.BLACK);
        setFocusable(true);
        requestFocus();

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
        debugOverlay.drawMinimapOverlay(graphics);
            framesDrawnThisSecond++;
            long now = System.nanoTime();
        if (now - lastFpsUpdateTimestamp >= 1000000000L) {
            displayedFps = framesDrawnThisSecond;
            framesDrawnThisSecond = 0;
            lastFpsUpdateTimestamp = now;
        }
        graphics.setColor(Color.WHITE);
        graphics.drawString("FPS: " + displayedFps, 20, 30);
    }
}