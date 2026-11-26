package fnaf.audio;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;

/**
 * Plays the .wav files in src/audio/. A missing or unreadable file is a warning,
 * never a crash: the game stays completely playable without any sound.
 */
public class AudioPlayer {
    private static final String AUDIO_FOLDER = "src/audio/";

    private static final double FULL_VOLUME = 1.0;
    private static final double SILENT_VOLUME = 0.0001;

    private final Map<String, Clip> loadedClips = new HashMap<>();
    private final Set<String> unusableFiles = new HashSet<>();

    public void playOnce(String fileName) {
        playOnce(fileName, FULL_VOLUME);
    }

    /** Plays a sound from the start once, at the given volume (0 is silence, 1 is full). */
    public void playOnce(String fileName, double volume) {
        Clip clip = load(fileName);
        if (clip == null) {
            return;
        }
        applyVolume(clip, volume);
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public void playLooping(String fileName) {
        playLooping(fileName, FULL_VOLUME);
    }

    /** Plays a sound again and again until it is stopped, at the given volume. */
    public void playLooping(String fileName, double volume) {
        Clip clip = load(fileName);
        if (clip == null) {
            return;
        }
        applyVolume(clip, volume);
        if (!clip.isRunning()) {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /** True while the sound is still running, so a music track can pick a new one when it ends. */
    public boolean isPlaying(String fileName) {
        Clip clip = loadedClips.get(fileName);
        return clip != null && clip.isRunning();
    }

    /** Length of a sound in milliseconds, or 0 when it cannot be played. */
    public long getDurationMillis(String fileName) {
        Clip clip = load(fileName);
        if (clip == null) {
            return 0;
        }
        return clip.getMicrosecondLength() / 1000;
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

    /**
     * Sets a clip's own volume. The gain control is not available on every sound
     * card, so a clip without one simply stays at the volume it was recorded at.
     */
    private void applyVolume(Clip clip, double volume) {
        double clampedVolume = Math.max(SILENT_VOLUME, Math.min(FULL_VOLUME, volume));
        try {
            Control control = clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (!(control instanceof FloatControl)) {
                return;
            }
            FloatControl gainControl = (FloatControl) control;
            gainControl.setValue((float) (20.0 * Math.log10(clampedVolume)));
        } catch (IllegalArgumentException exception) {
            // No gain control on this clip: play it at full volume.
        }
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
