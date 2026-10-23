package GameTrial;

import GameTrial.core.Game;
import GameTrial.view.GamePanel;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.JFrame;

public class GameTrial {
    public static void main(String[] args) {
        JFrame gameWindow = new JFrame("Game");
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameWindow.setUndecorated(true);

        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        gameWindow.setBounds(screenBounds);

        Game game = new Game();
        GamePanel gamePanel = new GamePanel(game, gameWindow);

        gameWindow.add(gamePanel);
        gameWindow.setVisible(true);

        gamePanel.requestFocusInWindow();
        game.start(() -> gamePanel.repaint());
    }
}