package fnaf.assets;

import fnaf.entity.AnimatronicType;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * The frame-by-frame jumpscare of one animatronic. The frames are only read
 * when that animatronic actually kills the guard, so loading a night never
 * decodes every jumpscare in the game.
 *
 * The delay of a frame is part of its file name, for example
 * "frame_00_delay-0.05s.gif", and that delay is what decides how long the
 * jumpscare lasts and how fast it plays.
 */
public class JumpscareAnimation {
    private static final String JUMPSCARE_FOLDER = "src/jumpscare/";
    private static final String DEFAULT_FRAME_DELAY_SECONDS = "0.05";
    private static final Pattern FRAME_DELAY_PATTERN =
        Pattern.compile("delay-([0-9]+(?:\\.[0-9]+)?)s");
    private static final int TICKS_PER_SECOND = 60;
    private static final double DECODED_IMAGE_SCALE = 0.5;

    private final File[] frameFiles;
    private final int[] frameDurationsInTicks;
    private final BufferedImage[] decodedFrames;
    private final int totalDurationInTicks;

    public JumpscareAnimation(AnimatronicType type) {
        File folder = new File(JUMPSCARE_FOLDER + type.jumpscareFolder());
        frameFiles = listFrameFiles(folder);
        frameDurationsInTicks = new int[frameFiles.length];
        decodedFrames = new BufferedImage[frameFiles.length];

        int totalTicks = 0;
        for (int frameIndex = 0; frameIndex < frameFiles.length; frameIndex++) {
            frameDurationsInTicks[frameIndex] = readFrameDurationInTicks(frameFiles[frameIndex]);
            totalTicks += frameDurationsInTicks[frameIndex];
        }
        totalDurationInTicks = Math.max(1, totalTicks);
    }

    public int getTotalDurationInTicks() {
        return totalDurationInTicks;
    }

    public int getFrameCount() {
        return frameFiles.length;
    }

    /** The frame that is on screen at the given tick of the jumpscare. */
    public synchronized BufferedImage getFrameForTick(int tick) {
        if (frameFiles.length == 0) {
            return null;
        }
        int remainingTicks = Math.max(0, tick);
        for (int frameIndex = 0; frameIndex < frameFiles.length; frameIndex++) {
            if (remainingTicks < frameDurationsInTicks[frameIndex]) {
                return decodedFrame(frameIndex);
            }
            remainingTicks -= frameDurationsInTicks[frameIndex];
        }
        return decodedFrame(frameFiles.length - 1);
    }

    /** Decodes one frame the first time it is needed, and keeps it for the rest of the night. */
    private BufferedImage decodedFrame(int frameIndex) {
        if (decodedFrames[frameIndex] != null) {
            return decodedFrames[frameIndex];
        }
        try {
            BufferedImage fullSizeFrame = ImageIO.read(frameFiles[frameIndex]);
            if (fullSizeFrame == null) {
                System.out.println("WARN: could not decode jumpscare frame " + frameFiles[frameIndex].getPath());
                return null;
            }
            decodedFrames[frameIndex] = scaledDown(fullSizeFrame);
            return decodedFrames[frameIndex];
        } catch (IOException exception) {
            System.out.println("WARN: could not read jumpscare frame " + frameFiles[frameIndex].getPath()
                + ": " + exception.getMessage());
            return null;
        }
    }

    private BufferedImage scaledDown(BufferedImage fullSizeFrame) {
        int width = Math.max(1, (int) Math.round(fullSizeFrame.getWidth() * DECODED_IMAGE_SCALE));
        int height = Math.max(1, (int) Math.round(fullSizeFrame.getHeight() * DECODED_IMAGE_SCALE));
        BufferedImage scaledFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaledFrame.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(fullSizeFrame, 0, 0, width, height, null);
        graphics.dispose();
        return scaledFrame;
    }

    private static File[] listFrameFiles(File folder) {
        File[] foundFiles = folder.listFiles(
            (directory, fileName) -> fileName.toLowerCase().endsWith(".gif"));
        if (foundFiles == null) {
            System.out.println("WARN: no jumpscare frames at " + folder.getPath());
            return new File[0];
        }
        Arrays.sort(foundFiles, Comparator.comparing(File::getName));
        return foundFiles;
    }

    private static int readFrameDurationInTicks(File frameFile) {
        String frameDelaySeconds = DEFAULT_FRAME_DELAY_SECONDS;
        Matcher matcher = FRAME_DELAY_PATTERN.matcher(frameFile.getName());
        if (matcher.find()) {
            frameDelaySeconds = matcher.group(1);
        }
        double delaySeconds = Double.parseDouble(frameDelaySeconds);
        return Math.max(1, (int) Math.round(delaySeconds * TICKS_PER_SECOND));
    }
}
