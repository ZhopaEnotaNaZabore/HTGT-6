package com.mod.htgt6.common.crusher;

/** Single source of truth for validation, NEI, the projector and geometry tests. */
public final class IndustrialCrusherGeometry {
    public static final int WIDTH = 7, HEIGHT = 6, DEPTH = 16;
    public static final int AIR = 0, CONTROLLER = 1, BASE = 2, CASING = 3, OUTPUT = 4, RIB = 5;
    private IndustrialCrusherGeometry() { }

    public static int element(int u, int y, int v) {
        if (u < -3 || u > 3 || y < 0 || y >= HEIGHT || v < 0 || v >= DEPTH) return AIR;
        if (u == 0 && y == 0 && v == 0) return CONTROLLER;
        if (u == 0 && y == 1 && v == 15) return OUTPUT;
        boolean side = Math.abs(u) == 3;
        if (y == 0) return side && (v == 0 || v == 15) ? RIB : BASE;
        // Open feed deck, six blocks long: no roof above the three-wide belt.
        if (v < 6) {
            if (side && y <= 3) return y == 3 || v == 0 ? RIB : CASING;
            if (v == 0 && y <= 2) return CASING;
            return AIR;
        }
        if (y == 5) return side || v == 6 || v == 15 ? RIB : CASING;
        if (side && (v == 6 || v == 10 || v == 15)) return RIB;
        if (v == 15) return CASING;
        // Open service side exposes the rollers/chute and the lower product belt.
        if (u == 3) return CASING;
        return AIR;
    }
}
