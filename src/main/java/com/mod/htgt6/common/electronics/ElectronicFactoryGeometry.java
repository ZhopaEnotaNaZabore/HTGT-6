package com.mod.htgt6.common.electronics;

/** Shared, world-independent structure layout. Y is relative to the controller. */
public final class ElectronicFactoryGeometry {
    public static final int AIR=0, CONTROLLER=1, CASING=2, FLOOR=3, GLASS=4, OUTPUT=5, RIB=6;
    private ElectronicFactoryGeometry() { }
    public static int legacyElement(int u, int y, int v) {
        if (u == 0 && y == 0 && v == 0) return CONTROLLER;
        if (u == 0 && y == 0 && v == 9) return OUTPUT;
        if (y == -3) return FLOOR;
        if (y == 3 || v == 0 || v == 9) return CASING;
        return Math.abs(u) == 2 ? GLASS : AIR;
    }
    public static int element(int u, int y, int v) {
        int legacy = legacyElement(u,y,v);
        if (legacy == CONTROLLER || legacy == OUTPUT) return legacy;
        boolean side = Math.abs(u) == 2, cap = Math.abs(y) == 3, end = v == 0 || v == 9;
        if ((side ? 1 : 0) + (cap ? 1 : 0) + (end ? 1 : 0) >= 2
                || (v == 3 || v == 6) && (side || y == 3)) return RIB;
        return legacy;
    }
}
