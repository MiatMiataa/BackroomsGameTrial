package GameTrial.audio;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Plays the .wav files in src/audio/. A missing or unreadable file is a warning,
 * never a crash: the game stays completely playable without any sound.
 */
public class AudioPlayer {
    private static final String AUDIO_FOLDER = "src/audio/";

    private final Map<String, Clip> loadedClips = new HashMap<>();
    private final Set<String> unusableFiles = new HashSet<>();

    public void playOnce(String fileName) {
        Clip clip = load(fileName);
        if (clip == null) {
            return;
        }
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public void playLooping(String fileName) {
        Clip clip = load(fileName);
        if (clip == null) {
            return;
        }
        if (!clip.isRunning()) {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop(String fileName) {
        Clip clip = loadedClips.get(fileName);
        if (clip != null) {
            clip.stop();
        }
    }

    public void stopAll() {
        for (Clip clip : loadedClips.values()) {
            clip.stop();
        }
    }

    public void close() {
        for (Clip clip : loadedClips.values()) {
            clip.stop();
            clip.close();
        }
        loadedClips.clear();
    }

    private Clip load(String fileName) {
        Clip alreadyLoaded = loadedClips.get(fileName);
        if (alreadyLoaded != null) {
            return alreadyLoaded;
        }
        if (unusableFiles.contains(fileName)) {
            return null;
        }

        File audioFile = new File(AUDIO_FOLDER + fileName);
        if (!audioFile.exists()) {
            unusableFiles.add(fileName);
            System.out.println("WARN: no audio file at " + audioFile.getPath() + ", playing silently");
            return null;
        }
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            audioStream.close();
            loadedClips.put(fileName, clip);
            return clip;
        } catch (Exception exception) {
            unusableFiles.add(fileName);
            System.out.println("WARN: could not load audio " + audioFile.getPath() + ": " + exception.getMessage());
            return null;
        }
    }
}
