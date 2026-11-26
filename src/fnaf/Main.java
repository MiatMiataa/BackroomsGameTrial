package fnaf;

import fnaf.game.Game;
import fnaf.game.NightSettings;
import fnaf.game.SaveManager;
import fnaf.ui.GamePanel;
import fnaf.ui.MainMenuPanel;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.JFrame;

public class Main {
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
        Runnable nextNight = () -> {
            game.stop();
            if (nightNumber < NightSettings.nightCount()) {
                startNight(nightNumber + 1);
            } else {
                showMainMenu();
            }
        };
        GamePanel gamePanel = new GamePanel(game, exitToMenu);
        game.setExitToMenuAction(exitToMenu);
        game.setNextNightAction(nextNight);

        gameWindow.setContentPane(gamePanel);
        gameWindow.revalidate();
        gameWindow.requestFocus();
        gamePanel.requestFocusInWindow();
        game.start(gamePanel::repaint);
    }
}
