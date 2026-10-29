package com.example.modid.firmware;

import java.util.Random;

/** Trace layout and rotation checks for the workbench. */
public final class FirmwarePuzzle {
    public static final int[] COLORS = {0x55DADD, 0xE9B358, 0xB997F2, 0x9ED371, 0xED8CAF};
    public static final int[][] TERMINALS = {{0, 0}, {10, 0}, {0, 6}, {10, 6}, {5, 8}};
    public static final int[][] PORTS = {{4, 2}, {6, 2}, {4, 4}, {6, 4}, {5, 4}};
    public static final int[][][] PATHS = {
        {{1, 0}, {2, 0}, {3, 0}, {3, 1}, {3, 2}},
        {{9, 0}, {8, 0}, {7, 0}, {7, 1}, {7, 2}},
        {{1, 6}, {2, 6}, {3, 6}, {3, 5}, {3, 4}},
        {{9, 6}, {8, 6}, {7, 6}, {7, 5}, {7, 4}},
        {{5, 7}, {5, 6}, {5, 5}}
    };
    public static final int STRIDE = 5;

    private FirmwarePuzzle() {}

    public static int branches(int tier) {
        return Math.min(5, 2 + Math.max(0, tier - 1));
    }

    public static byte[] shuffled(long seed, int branches) {
        Random random = new Random(seed);
        byte[] rotations = new byte[STRIDE * 5];
        for (int branch = 0; branch < branches; branch++) {
            for (int node = 0; node < PATHS[branch].length; node++)
                rotations[branch * STRIDE + node] = (byte) random.nextInt(4);
            if (connected(branch, rotations)) rotations[branch * STRIDE] ^= 1;
        }
        return rotations;
    }

    public static int goalMask(int branch, int node) {
        int[] at = PATHS[branch][node];
        int[] before = node == 0 ? TERMINALS[branch] : PATHS[branch][node - 1];
        int[] after = node + 1 == PATHS[branch].length ? PORTS[branch] : PATHS[branch][node + 1];
        return direction(at, before) | direction(at, after);
    }

    private static int direction(int[] from, int[] to) {
        if (to[1] < from[1]) return 1;
        if (to[0] > from[0]) return 2;
        if (to[1] > from[1]) return 4;
        return 8;
    }

    public static int rotatedMask(int mask, int rotation) {
        rotation &= 3;
        return ((mask << rotation) | (mask >> (4 - rotation))) & 15;
    }

    public static boolean connected(int branch, byte[] rotations) {
        if (rotations.length != 25) return false;
        for (int i = 0; i < PATHS[branch].length; i++) {
            int goal = goalMask(branch, i);
            if (rotatedMask(goal, rotations[branch * STRIDE + i]) != goal) return false;
        }
        return true;
    }
}
