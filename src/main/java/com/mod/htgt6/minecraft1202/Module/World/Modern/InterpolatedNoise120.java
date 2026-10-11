package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Legacy-compatible blended density noise retained by the 1.18+ noise router. */
public final class InterpolatedNoise120 {
    private final ImprovedNoise120[] lower = new ImprovedNoise120[16];
    private final ImprovedNoise120[] upper = new ImprovedNoise120[16];
    private final ImprovedNoise120[] interpolation = new ImprovedNoise120[8];

    public InterpolatedNoise120(long seed) {
        Xoroshiro128PlusPlus120 random = new Xoroshiro128PlusPlus120(seed);
        for (int i = 0; i < lower.length; i++) lower[i] = new ImprovedNoise120(random);
        for (int i = 0; i < upper.length; i++) upper[i] = new ImprovedNoise120(random);
        for (int i = 0; i < interpolation.length; i++) interpolation[i] = new ImprovedNoise120(random);
    }

    public double sample(double x, double y, double z) {
        double blend = 0.0;
        double frequency = 1.0;
        for (ImprovedNoise120 noise : interpolation) {
            blend += noise.sample(wrap(x * frequency / 80.0), wrap(y * frequency / 160.0),
                    wrap(z * frequency / 80.0)) / frequency;
            frequency *= 0.5;
        }
        double alpha = clamp(blend / 10.0 + 0.5);
        double low = 0.0, high = 0.0;
        frequency = 1.0;
        for (int i = 0; i < lower.length; i++) {
            double sx = wrap(x * frequency), sy = wrap(y * frequency), sz = wrap(z * frequency);
            if (alpha < 1.0) low += lower[i].sample(sx, sy, sz) / frequency;
            if (alpha > 0.0) high += upper[i].sample(sx, sy, sz) / frequency;
            frequency *= 0.5;
        }
        return lerp(alpha, low / 512.0, high / 512.0) / 128.0;
    }

    private static double wrap(double value) {
        return value - Math.floor(value / 33554432.0 + 0.5) * 33554432.0;
    }
    private static double clamp(double value) { return value < 0.0 ? 0.0 : value > 1.0 ? 1.0 : value; }
    private static double lerp(double t, double a, double b) { return a + t * (b - a); }
}
