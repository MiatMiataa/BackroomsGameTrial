package GameTrial;

import GameTrial.core.Game;
import GameTrial.core.SaveManager;
import GameTrial.view.GamePanel;
import GameTrial.view.MainMenuPanel;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.JFrame;

public class GameTrial {
    private static final SaveManager SAVE_MANAGER = new SaveManager();

    private static JFrame gameWindow;

    public static void main(String[] args) {
        gameWindow = new JFrame("Game");
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameWindow.setUndecorated(true);

        Rectangle screenBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        gameWindow.setBounds(screenBounds);

        gameWindow.setVisible(true);
        showMainMenu();
    }

    private static void showMainMenu() {
        MainMenuPanel mainMenuPanel = new MainMenuPanel(
            SAVE_MANAGER.loadHighestUnlockedNight(),
            nightNumber -> startNight(nightNumber));

        gameWindow.setContentPane(mainMenuPanel);
        gameWindow.revalidate();
        gameWindow.repaint();
        mainMenuPanel.requestFocusInWindow();
    }

    private static void startNight(int nightNumber) {
        Game game = new Game(nightNumber);
        Runnable exitToMenu = () -> {
            game.stop();
            showMainMenu();
        };
        GamePanel gamePanel = new GamePanel(game, exitToMenu);
        game.setExitToMenuAction(exitToMenu);

        gameWindow.setContentPane(gamePanel);
        gameWindow.revalidate();
        gameWindow.requestFocus();
        gamePanel.requestFocusInWindow();
        game.start(gamePanel::repaint);
    }
}
