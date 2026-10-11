package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.NormalNoise120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.Xoroshiro128PlusPlus120;

/** Production adapter from the 1.20.2 seeded noise registry to the ported NormalNoise runtime. */
public final class VanillaNormalNoiseFactory120 implements NoiseFactory120 {
    private final com.mod.htgt6.minecraft1202.Module.World.Modern.PositionalRandomFactory120 positionalRandom;

    public VanillaNormalNoiseFactory120(long worldSeed) {
        this.positionalRandom = new com.mod.htgt6.minecraft1202.Module.World.Modern.PositionalRandomFactory120(worldSeed);
    }

    @Override
    public Noise120 create(NoiseParameters120 parameters) {
        if (parameters == null) throw new NullPointerException("parameters");
        Xoroshiro128PlusPlus120 random = positionalRandom.fromHashOf(parameters.resourceLocation());
        final NormalNoise120 noise = new NormalNoise120(random, parameters.firstOctave(), parameters.amplitudes());
        return new Noise120() {
            @Override
            public double sample(double x, double y, double z) {
                return noise.sample(x, y, z);
            }
        };
    }
}
