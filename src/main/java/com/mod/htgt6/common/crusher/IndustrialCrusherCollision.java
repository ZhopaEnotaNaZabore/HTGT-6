package com.mod.htgt6.common.crusher;

import java.util.ArrayList;
import java.util.List;

/** Static mechanical envelopes in the same coordinates as the crusher renderer. */
public final class IndustrialCrusherCollision {
    private static final List<double[]> SHAPES = new ArrayList<double[]>();
    static {
        box(-1.73, 2.83, .52, 1.73, 3.10, 6.25);
        box(-1.75, 3.10, .52, -1.55, 3.27, 6.25);
        box(1.55, 3.10, .52, 1.75, 3.27, 6.25);
        box(-1.48, 1.01, 8.55, 1.48, 1.28, 14.98);
        box(-1.50, 1.28, 8.55, -1.30, 1.45, 14.98);
        box(1.30, 1.28, 8.55, 1.50, 1.45, 14.98);
        for (double x : new double[] {-2.35, 1.93}) {
            box(x, 1.02, .60, x + .42, 2.98, 5.95);
            for (double z : new double[] {7.1, 8.7}) {
                box(x, 2.50, z - .40, x + .42, 4.45, z + .40);
                box(x - .06, 3.38, z - .28, x + .48, 3.96, z + .28);
            }
        }
        // Stepped cylinder envelope: static, independent of animated teeth.
        for (double z : new double[] {7.1, 8.7}) {
            box(-2.45, 3.51, z - .14, 2.45, 3.79, z + .14);
            for (int i = 0; i < 8; i++) {
                double lo = -.94 + i * .235, hi = lo + .235;
                double near = lo <= 0 && hi >= 0 ? 0 : Math.min(Math.abs(lo), Math.abs(hi));
                double width = Math.sqrt(.94 * .94 - near * near);
                box(-1.99, 3.65 + lo, z - width, 1.99, 3.65 + hi, z + width);
            }
        }
        box(-1.8, 2.48, 10.25, 1.8, 4.60, 11.50);
        box(-1.46, 2.60, 11.50, 1.46, 4.46, 14.50);
        box(-2.42, 4.76, 6.60, 2.42, 4.94, 9.55);
        box(-2, 1.55, 8.65, -1.75, 2.52, 9.70);
        box(1.75, 1.55, 8.65, 2, 2.52, 9.70);
        for (int i = 0; i < 8; i++) {
            double z = 7.8 + i * 1.85 / 8, top = 2.65 - i * 1.22 / 8;
            box(-1.75, top - .24, z, 1.75, top, z + 1.85 / 8);
        }
    }

    private IndustrialCrusherCollision() { }
    private static void box(double a, double b, double c, double d, double e, double f) {
        SHAPES.add(new double[] {a, b, c, d, e, f});
    }

    /** Clip to an air cell, then rotate around its centre. Never extend into adjacent blocks. */
    public static List<double[]> cell(int u, int y, int v, int facing) {
        List<double[]> result = new ArrayList<double[]>();
        if (IndustrialCrusherGeometry.element(u, y, v) != IndustrialCrusherGeometry.AIR) return result;
        for (double[] s : SHAPES) {
            double a = Math.max(s[0], u - .5) - u, b = Math.max(s[1], y) - y;
            double c = Math.max(s[2], v - .5) - v;
            double d = Math.min(s[3], u + .5) - u, e = Math.min(s[4], y + 1) - y;
            double f = Math.min(s[5], v + .5) - v;
            if (a >= d || b >= e || c >= f) continue;
            if (facing == 3) result.add(new double[] {.5-d,b,.5-f,.5-a,e,.5-c});
            else if (facing == 4) result.add(new double[] {.5+c,b,.5-d,.5+f,e,.5-a});
            else if (facing == 5) result.add(new double[] {.5-f,b,.5+a,.5-c,e,.5+d});
            else result.add(new double[] {.5+a,b,.5+c,.5+d,e,.5+f});
        }
        return result;
    }
}
