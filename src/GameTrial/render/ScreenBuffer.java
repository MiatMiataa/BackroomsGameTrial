package GameTrial.render;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class ScreenBuffer {
    public final int width;
    public final int height;
    public final int[] pixels;
    private final BufferedImage image;

    public ScreenBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    public BufferedImage getImage() {
        return image;
    }
}