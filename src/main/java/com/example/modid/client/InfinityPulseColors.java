package com.example.modid.client;

/** Surface colours sampled from Avaritia's ingot, with a separate geometry pulse. */
public final class InfinityPulseColors {
    public static final int SIZE = 32;

    private InfinityPulseColors() {}

    public static double pulseScale(double seconds) {
        // Avaritia renders a second copy at 0.95..1.10 scale and 60% opacity.
        // Smooth that range over time rather than picking a random size every draw.
        return 1.025 + 0.075 * Math.sin(seconds * Math.PI * 2.0 / 1.2);
    }

    public static int[] fromIngotFrame(int[] source, int size) {
        int minX = size, minY = size, maxX = -1, maxY = -1;
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++) {
                if ((source[y * size + x] >>> 24) > 127) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        if (maxX < 0) return new int[SIZE * SIZE];

        // Keep only the frame's broad colour regions. Copying its individual pixels
        // carries the ingot's bevels and face into the liquid as a recognizable image.
        double[][] colors = new double[4][3];
        int[] counts = new int[4];
        for (int y = minY; y <= maxY; y++)
            for (int x = minX; x <= maxX; x++) {
                int color = source[y * size + x];
                if ((color >>> 24) < 128) continue;
                int region = (x * 2 > minX + maxX ? 1 : 0) + (y * 2 > minY + maxY ? 2 : 0);
                for (int channel = 0; channel < 3; channel++) {
                    colors[region][channel] += (color >> (channel * 8)) & 255;
                }
                counts[region]++;
            }
        for (int region = 0; region < 4; region++) {
            if (counts[region] == 0) {
                for (int other = 0; other < 4; other++) {
                    if (counts[other] > 0) {
                        colors[region] = colors[other].clone();
                        counts[region] = counts[other];
                        break;
                    }
                }
            }
        }
        for (int region = 0; region < 4; region++) {
            for (int channel = 0; channel < 3; channel++) colors[region][channel] /= counts[region];
        }

        int[] result = new int[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++)
            for (int x = 0; x < SIZE; x++) {
                double u = (double) x / (SIZE - 1);
                double v = (double) y / (SIZE - 1);
                int color = 0xFF000000;
                for (int channel = 0; channel < 3; channel++) {
                    double top = colors[0][channel] * (1.0 - u) + colors[1][channel] * u;
                    double bottom = colors[2][channel] * (1.0 - u) + colors[3][channel] * u;
                    color |= (int) Math.round(top * (1.0 - v) + bottom * v) << (channel * 8);
                }
                result[y * SIZE + x] = color;
            }
        return result;
    }

    public static int[] interpolate(int[] from, int[] to, double fraction) {
        fraction = Math.max(0.0, Math.min(1.0, fraction));
        int[] result = new int[from.length];
        for (int i = 0; i < result.length; i++) {
            int color = 0xFF000000;
            for (int shift = 0; shift <= 16; shift += 8) {
                int a = (from[i] >> shift) & 255;
                int b = (to[i] >> shift) & 255;
                color |= (int) (a + (b - a) * fraction) << shift;
            }
            result[i] = color;
        }
        return result;
    }

    public static int[] createFrame(double seconds) {
        // Graceful fallback if Avaritia is absent; runtime uses its actual atlas frames.
        int[] pixels = new int[SIZE * SIZE];
        int[] rainbow = {0xFFEF725A, 0xFFFFDE5F, 0xFF75BD83, 0xFF55D0BE, 0xFF5CBEE9, 0xFFCA55F7};
        for (int y = 0; y < SIZE; y++)
            for (int x = 0; x < SIZE; x++) {
                int index = Math.min(rainbow.length - 1, (x + y) * rainbow.length / (SIZE * 2));
                pixels[y * SIZE + x] = rainbow[index];
            }
        int[] white = new int[pixels.length];
        java.util.Arrays.fill(white, -1);
        return interpolate(pixels, white, 0.5 - 0.5 * Math.cos(seconds * Math.PI / 6.0));
    }
}
