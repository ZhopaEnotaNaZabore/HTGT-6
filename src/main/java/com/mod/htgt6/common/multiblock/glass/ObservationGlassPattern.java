package com.mod.htgt6.common.multiblock.glass;

/** Pure CTM geometry: edges L/R/T/B, then missing diagonals LT/RT/LB/RB. */
public final class ObservationGlassPattern {
    public static final int SIZE = 64;
    private static final int[][] RIGHT = {{1,0,0},{1,0,0},{-1,0,0},{1,0,0},{0,0,1},{0,0,-1}};
    private static final int[][] DOWN = {{0,0,1},{0,0,1},{0,-1,0},{0,-1,0},{0,-1,0},{0,-1,0}};
    private static final int[] CORNER_EDGES = {5,6,9,10};
    private ObservationGlassPattern() {}

    public static int offset(int face, int u, int v, int axis) {
        return RIGHT[face][axis] * u + DOWN[face][axis] * v;
    }

    public static int canonical(int mask) {
        int result = mask & 15;
        for (int corner = 0; corner < 4; corner++) {
            if ((mask & CORNER_EDGES[corner]) == 0) result |= mask & (16 << corner);
        }
        return result;
    }

    /** Procedural 64px beveled frame; flat translucent pane never repeats a grid or shine marks. */
    public static int pixel(int mask, boolean clear, int x, int y) {
        int distance = SIZE;
        if ((mask & 1) != 0) distance = Math.min(distance, x);
        if ((mask & 2) != 0) distance = Math.min(distance, SIZE - 1 - x);
        if ((mask & 4) != 0) distance = Math.min(distance, y);
        if ((mask & 8) != 0) distance = Math.min(distance, SIZE - 1 - y);
        if ((mask & 16) != 0) distance = Math.min(distance, Math.max(x, y));
        if ((mask & 32) != 0) distance = Math.min(distance, Math.max(SIZE - 1 - x, y));
        if ((mask & 64) != 0) distance = Math.min(distance, Math.max(x, SIZE - 1 - y));
        if ((mask & 128) != 0) distance = Math.min(distance, Math.max(SIZE - 1 - x, SIZE - 1 - y));
        if (distance == 0) return 0xFF303840;
        if (distance == 1) return 0xFFBBC6CC;
        if (distance == 2) return 0xFF77848C;
        if (distance == 3) return 0xFF46525A;
        return (clear ? 0x24FFFFFF : 0x48FFFFFF);
    }
}
