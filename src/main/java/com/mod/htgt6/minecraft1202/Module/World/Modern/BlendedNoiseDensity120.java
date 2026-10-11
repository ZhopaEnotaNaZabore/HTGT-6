package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.BlendedNoise120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.PositionalRandomFactory120;

/** Standard overworld/base_3d_noise wired from RandomState's minecraft:terrain stream. */
public final class BlendedNoiseDensity120 implements DensityFunction120 {
    private final BlendedNoise120 noise;
    public BlendedNoiseDensity120(long seed){
        noise=new BlendedNoise120(new PositionalRandomFactory120(seed).fromHashOf("minecraft:terrain"),
                0.25D,0.125D,80.0D,160.0D,8.0D);
    }
    @Override public double sample(DensityContext120 context){
        return noise.sample(context.blockX(),FuturaMountainDensityRouter120.futuraToModernY(context.blockY()),context.blockZ());
    }
}
