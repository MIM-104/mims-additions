package com.example.modid.common;

public final class WearUtil {

    public static final long TICKS_PER_HOUR = 20L * 60L * 60L;
    public static final double WEAR_PER_HOUR = 0.05d;

    private WearUtil() {}

    public static int scaleEuT(int base, long wearTicks) {
        if (base <= 0) {
            return base;
        }
        double hours = wearTicks / (double) TICKS_PER_HOUR;
        double scaled = base * (1.0d + (WEAR_PER_HOUR * hours));
        if (scaled >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        int result = (int) Math.ceil(scaled);
        return Math.max(1, result);
    }

    public static String formatWithCommas(int value) {
        String raw = Integer.toString(value);
        StringBuilder out = new StringBuilder(raw.length() + raw.length() / 3);
        int len = raw.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) {
                out.append(',');
            }
            out.append(raw.charAt(i));
        }
        return out.toString();
    }
}
