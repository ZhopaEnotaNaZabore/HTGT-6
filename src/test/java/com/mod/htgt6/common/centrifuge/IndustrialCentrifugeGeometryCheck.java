package com.mod.htgt6.common.centrifuge;

/** Standalone regression checks without starting Minecraft or OpenGL. */
public final class IndustrialCentrifugeGeometryCheck {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        int[] counts = new int[6];
        for (int u = -3; u <= 3; u++) for (int y = 0; y < 5; y++) for (int v = 0; v < 7; v++) {
            int e = IndustrialCentrifugeGeometry.element(u, y, v);
            counts[e]++;
            check(e == IndustrialCentrifugeGeometry.element(-u, y, v), "Asymmetric shell");
            if (e == IndustrialCentrifugeGeometry.GLASS) check(v == 0 && y > 0 && y < 4, "Misplaced glass");
            if (Math.abs(u) < 3 && y > 0 && y < 4 && v > 0 && v < 6) check(e == 0, "Rotor chamber obstructed");
        }
        int[] expected = {75, 1, 25, 70, 15, 59};
        for (int i = 0; i < counts.length; i++) check(counts[i] == expected[i], "Wrong element count " + i + ": " + counts[i]);
        for (int u = -2; u <= 2; u++) check(IndustrialCentrifugeGeometry.element(u, 0, 1) == 2, "NEI port outside service deck");
        check(IndustrialCentrifugeGeometry.element(0,0,0) == 1, "Controller origin changed");
        System.out.println("Centrifuge geometry OK: 245 positions, 75 air, 59 ribs, 15 glass, 25 service cells.");
    }
}
