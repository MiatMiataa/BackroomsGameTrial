package GameTrial.assets;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Texture {
    public final int width;
    public final int height;

    public final int widthMask;
    public final int heightMask;

    private static final int BRIGHTNESS_PERCENT = 80;
    private static final int SHADED_SIDE_PERCENT = 75;

    private final int[] normalPixels;

    private final int[] darkPixels;

    public Texture(String filePath) throws IOException {
        File imageFile = new File(filePath);
        if (!imageFile.exists()) {
            throw new IOException("Texture file not found: " + imageFile.getAbsolutePath());
        }

        BufferedImage loadedImage = ImageIO.read(imageFile);
        if (loadedImage == null) {
            throw new IOException("Could not decode image: " + imageFile.getAbsolutePath());
        }

        width = loadedImage.getWidth();
        height = loadedImage.getHeight();

        if (!isPowerOfTwo(width) || !isPowerOfTwo(height)) {
            throw new IllegalArgumentException(filePath + " is " + width + "x" + height
                    + " pixels. Width and height must each be a power of two (64, 128, 256, 512...).");
        }

        widthMask = width - 1;
        heightMask = height - 1;

        normalPixels = loadedImage.getRGB(0, 0, width, height, null, 0, width);

        darkPixels = new int[normalPixels.length];
        for (int pixelIndex = 0; pixelIndex < normalPixels.length; pixelIndex++) {
            int originalColor = normalPixels[pixelIndex];
            normalPixels[pixelIndex] = scaleColor(originalColor, BRIGHTNESS_PERCENT);
            darkPixels[pixelIndex] = scaleColor(originalColor, SHADED_SIDE_PERCENT);
        }
    }

    public int getPixel(int x, int y) {
        return normalPixels[y * width + x];
    }

    public int getDarkPixel(int x, int y) {
        return darkPixels[y * width + x];
    }

    private static boolean isPowerOfTwo(int number) {
        return number > 0 && (number & (number - 1)) == 0;
    }

    private static int scaleColor(int color, int percentage) {
        int red = ((color >> 16) & 0xFF) * percentage / 100;
        int green = ((color >> 8) & 0xFF) * percentage / 100;
        int blue = (color & 0xFF) * percentage / 100;
        return (0xFF << 24) | (red << 16) | (green << 8) | blue;
    }
}