package com.example.modid.firmware;

/** One set of bounds for rendering and clicking; fits Minecraft's 320 x 240 minimum GUI. */
public final class FirmwareWorkbenchLayout {
    public static final int WIDTH = 320, HEIGHT = 240;
    public static final Rect BOARD = new Rect(8, 22, 146, 116);
    public static final Rect CATALOG = new Rect(164, 22, 148, 116);
    public static final Rect SEARCH = new Rect(168, 26, 140, 14);
    public static final Rect PREVIOUS = new Rect(168, 120, 18, 14);
    public static final Rect NEXT = new Rect(290, 120, 18, 14);
    public static final Rect RESET = new Rect(110, 120, 38, 14);
    public static final Rect SOLDER = new Rect(254, 180, 54, 14);
    public static final Rect FLASH = new Rect(254, 198, 54, 14);
    public static final Rect STATUS = new Rect(80, 145, 162, 10);

    public static Rect row(int row) {
        return new Rect(168, 44 + row * 18, 140, 18);
    }

    private FirmwareWorkbenchLayout() {}

    public static final class Rect {
        public final int x, y, w, h;

        public Rect(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public boolean contains(int px, int py) {
            return px >= x && py >= y && px < x + w && py < y + h;
        }

        public boolean overlaps(Rect other) {
            return x < other.x + other.w && other.x < x + w && y < other.y + other.h && other.y < y + h;
        }
    }
}
