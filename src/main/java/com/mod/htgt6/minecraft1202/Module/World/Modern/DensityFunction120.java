package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Small Java-8 density contract, intentionally independent of Mojang codecs. */
public interface DensityFunction120 {
    double sample(DensityContext120 context);

    /** Allocation-free convenience for a caller which owns one mutable context. */
    default double sample(MutableDensityContext120 context, int x, int y, int z) {
        return sample(context.set(x, y, z));
    }
}
