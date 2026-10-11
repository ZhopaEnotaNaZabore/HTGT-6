package com.mod.htgt6.common.centrifuge;

/** Minecraft-independent geometry shared by validation, NEI and the projector. */
public final class IndustrialCentrifugeGeometry {
    public static final int WIDTH = 7, HEIGHT = 5, DEPTH = 7;
    public static final int AIR = 0, CONTROLLER = 1, BASE = 2, CASING = 3, GLASS = 4, RIB = 5;
    private IndustrialCentrifugeGeometry() { }
    public static int element(int u, int y, int v) {
        if (u == 0 && y == 0 && v == 0) return CONTROLLER;
        boolean side = Math.abs(u) == 3;
        boolean end = v == 0 || v == DEPTH - 1;
        boolean cap = y == 0 || y == HEIGHT - 1;
        if ((side ? 1 : 0) + (end ? 1 : 0) + (cap ? 1 : 0) >= 2) return RIB;
        if (y == 0) return BASE;
        if (v == 0 && !side && !cap) return GLASS;
        if (side || end || cap) return CASING;
        return AIR;
    }
}
