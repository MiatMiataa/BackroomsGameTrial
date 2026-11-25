package GameTrial.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Reads and writes the one thing the game remembers between runs: the highest
 * night the player has unlocked. 0 means there is no save yet, so the Continue
 * button stays disabled.
 */
public class SaveManager {
    private static final String DEFAULT_SAVE_FILE = "save.properties";
    private static final String HIGHEST_UNLOCKED_NIGHT_KEY = "highestUnlockedNight";
    private static final int LAST_NIGHT = 5;

    private final String saveFilePath;

    public SaveManager() {
        this(DEFAULT_SAVE_FILE);
    }

    public SaveManager(String saveFilePath) {
        this.saveFilePath = saveFilePath;
    }

    public int loadHighestUnlockedNight() {
        File saveFile = new File(saveFilePath);
        if (!saveFile.exists()) {
            return 0;
        }
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(saveFile)) {
            properties.load(inputStream);
        } catch (IOException exception) {
            System.out.println("WARN: could not read " + saveFilePath + ": " + exception.getMessage());
            return 0;
        }
        try {
            int savedNight = Integer.parseInt(properties.getProperty(HIGHEST_UNLOCKED_NIGHT_KEY, "0").trim());
            return Math.max(0, Math.min(LAST_NIGHT, savedNight));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    public boolean hasSave() {
        return loadHighestUnlockedNight() >= 1;
    }

    public void unlockNight(int nightNumber) {
        int unlockedNight = Math.max(1, Math.min(LAST_NIGHT, nightNumber));
        if (unlockedNight <= loadHighestUnlockedNight()) {
            return;
        }
        Properties properties = new Properties();
        properties.setProperty(HIGHEST_UNLOCKED_NIGHT_KEY, String.valueOf(unlockedNight));
        try (OutputStream outputStream = new FileOutputStream(saveFilePath)) {
            properties.store(outputStream, "BackroomsGameTrial progress");
        } catch (IOException exception) {
            System.out.println("WARN: could not write " + saveFilePath + ": " + exception.getMessage());
        }
    }
}
