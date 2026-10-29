import com.example.modid.client.InfinityPulseColors;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;

/** Verify palette-only surfaces, interpolation, alpha, and physical pulse bounds. */
public final class InfinityAppearanceCheck {
    public static void main(String[] args) throws Exception {
        BufferedImage ingot = ImageIO.read(new File(args[1]));
        int size = ingot.getWidth();
        int count = ingot.getHeight() / size;
        int[][] surfaces = new int[count][];
        File output = new File(args[0]); output.mkdirs();
        for (int frame = 0; frame < count; frame++) {
            int[] source = ingot.getRGB(0, frame * size, size, size, null, 0, size);
            int[] minimum = {255, 255, 255}, maximum = {0, 0, 0};
            for (int color : source) if ((color >>> 24) > 127) {
                for (int channel = 0; channel < 3; channel++) {
                    int value = (color >> (channel * 8)) & 255;
                    minimum[channel] = Math.min(minimum[channel], value);
                    maximum[channel] = Math.max(maximum[channel], value);
                }
            }
            surfaces[frame] = InfinityPulseColors.fromIngotFrame(source, size);
            for (int color : surfaces[frame]) {
                if ((color >>> 24) != 255) throw new AssertionError("Fluid must remain opaque");
                for (int channel = 0; channel < 3; channel++) {
                    int value = (color >> (channel * 8)) & 255;
                    if (value < minimum[channel] || value > maximum[channel]) {
                        throw new AssertionError("Fluid colours must stay within the original palette");
                    }
                }
            }
            for (int y = 1; y < 31; y++) for (int x = 1; x < 31; x++) {
                for (int shift = 0; shift <= 16; shift += 8) {
                    int center = (surfaces[frame][y * 32 + x] >> shift) & 255;
                    int left = (surfaces[frame][y * 32 + x - 1] >> shift) & 255;
                    int right = (surfaces[frame][y * 32 + x + 1] >> shift) & 255;
                    int above = (surfaces[frame][(y - 1) * 32 + x] >> shift) & 255;
                    int below = (surfaces[frame][(y + 1) * 32 + x] >> shift) & 255;
                    if (Math.abs(left + right - 2 * center) > 2 || Math.abs(above + below - 2 * center) > 2) {
                        throw new AssertionError("Surface must contain no ingot edges or pixel markings");
                    }
                }
            }
            BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, 32, 32, surfaces[frame], 0, 32);
            ImageIO.write(image, "png", new File(output, "ingot-surface-" + frame + ".png"));
        }
        for (int i = 0; i < count - 1; i++) {
            if (!Arrays.equals(surfaces[i], InfinityPulseColors.interpolate(surfaces[i], surfaces[i + 1], 0))) {
                throw new AssertionError("Interpolation must preserve exact endpoints");
            }
            if (!Arrays.equals(surfaces[i + 1], InfinityPulseColors.interpolate(surfaces[i], surfaces[i + 1], 1))) {
                throw new AssertionError("Interpolation must preserve exact endpoints");
            }
        }
        for (int tick = 0; tick < 1200; tick++) {
            double scale = InfinityPulseColors.pulseScale(tick / 1000.0);
            if (scale < 0.94999 || scale > 1.10001) throw new AssertionError("Unexpected pulse scale");
        }
        if (InfinityPulseColors.pulseScale(0.3) <= InfinityPulseColors.pulseScale(0.9)) {
            throw new AssertionError("The halo copy must physically expand and contract");
        }
        System.out.println("Verified " + count + " smooth palette-only surfaces, opaque fluid, interpolation endpoints, and unchanged 0.95..1.10 scale pulse.");
    }
}
