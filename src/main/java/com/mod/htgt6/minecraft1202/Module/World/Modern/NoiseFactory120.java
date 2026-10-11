package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Registry-keyed noise source factory matching the 1.20.2 NoiseData bootstrap. */
public interface NoiseFactory120 {
    Noise120 create(NoiseParameters120 parameters);
}
