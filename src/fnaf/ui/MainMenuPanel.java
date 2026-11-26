package fnaf.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MainMenuPanel extends JPanel {
    public interface NightStarter {
        void startNight(int nightNumber);
    }

    private static final int BUTTON_WIDTH = 260;
    private static final int BUTTON_HEIGHT = 48;

    public MainMenuPanel(int highestUnlockedNight, NightStarter nightStarter) {
        setBackground(Color.BLACK);
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(0, 0, 40, 0);

        JLabel titleLabel = new JLabel("BackroomsGameTrial", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 40));
        add(titleLabel, constraints);

        constraints.gridy = 1;
        constraints.insets = new Insets(0, 0, 16, 0);
        JButton newGameButton = createMenuButton("New Game");
        newGameButton.addActionListener(event -> nightStarter.startNight(1));
        add(newGameButton, constraints);

        constraints.gridy = 2;
        constraints.insets = new Insets(0, 0, 0, 0);
        JButton continueButton = createMenuButton("Continue");
        if (highestUnlockedNight >= 1) {
            continueButton.setText("Continue - Night " + highestUnlockedNight);
            continueButton.addActionListener(event -> nightStarter.startNight(highestUnlockedNight));
        } else {
            continueButton.setEnabled(false);
            continueButton.setForeground(Color.DARK_GRAY);
        }
        add(continueButton, constraints);
    }

    private JButton createMenuButton(String buttonText) {
        JButton button = new JButton(buttonText);
        button.setPreferredSize(new java.awt.Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Monospaced", Font.BOLD, 20));
        button.setFocusPainted(false);
        return button;
    }
}
