package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.Noise120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.NoiseParameters120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.VanillaNormalNoiseFactory120;

/** Six climate coordinates consumed by Minecraft 1.20.2 MultiNoiseBiomeSource. */
public final class FuturaClimateSampler120 {
    public static final int MODERN_TO_FUTURA_Y = 64;

    public static final class Point {
        public final float temperature, humidity, continentalness, erosion, depth, weirdness;
        public Point(float temperature, float humidity, float continentalness,
              float erosion, float depth, float weirdness) {
            this.temperature=temperature;this.humidity=humidity;this.continentalness=continentalness;
            this.erosion=erosion;this.depth=depth;this.weirdness=weirdness;
        }
    }

    private final Noise120 offset, temperature, humidity, continentalness, erosion, weirdness;

    public FuturaClimateSampler120(long seed) {
        VanillaNormalNoiseFactory120 noises=new VanillaNormalNoiseFactory120(seed);
        offset=noises.create(NoiseParameters120.OFFSET);
        temperature=noises.create(NoiseParameters120.TEMPERATURE);
        humidity=noises.create(NoiseParameters120.VEGETATION);
        continentalness=noises.create(NoiseParameters120.CONTINENTALNESS);
        erosion=noises.create(NoiseParameters120.EROSION);
        weirdness=noises.create(NoiseParameters120.RIDGE);
    }

    /** Surface entries in OverworldBiomeBuilder use the exact depth point 0. */
    public Point sampleSurface(int blockX,int blockZ) { return sample(blockX,127,blockZ,0.0F); }

    /**
     * Samples the vanilla shifted-noise climate router. Futura Y is translated
     * back by 64 before the density coordinate is formed; the built-in
     * overworld climate noises have yScale=0, while depth remains explicit.
     */
    public Point sample(int blockX,int futuraY,int blockZ,float depth) {
        int modernY=futuraY-MODERN_TO_FUTURA_Y;
        double shiftX=offset.sample(blockX*0.25D,0.0D,blockZ*0.25D)*4.0D;
        double shiftZ=offset.sample(blockZ*0.25D,blockX*0.25D,0.0D)*4.0D;
        double x=blockX*0.25D+shiftX, z=blockZ*0.25D+shiftZ;
        // Keep modernY in the API contract even though these registered
        // shifted-noise functions intentionally use yScale=0 in 1.20.2.
        double ignoredModernY=modernY*0.0D;
        return new Point((float)temperature.sample(x,ignoredModernY,z),
                (float)humidity.sample(x,ignoredModernY,z),
                (float)continentalness.sample(x,ignoredModernY,z),
                (float)erosion.sample(x,ignoredModernY,z),depth,
                (float)weirdness.sample(x,ignoredModernY,z));
    }
}
