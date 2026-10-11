package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenOcean;

/** 1.7.10-compatible identities for the warm ocean family introduced later by vanilla. */
public final class BiomeGenWarmOcean extends BiomeGenOcean {

    public BiomeGenWarmOcean(int id, boolean deep) {
        super(id);
        this.setBiomeName(deep ? "Deep Warm Ocean" : "Warm Ocean");
        // 1.20.2 warm_ocean.json: temperature=0.5, downfall=0.5.
        // "Warm" is the MultiNoise/ocean-temperature identity, not the
        // Biome climate temperature used for weather and height cooling.
        this.setTemperatureRainfall(0.5F, 0.5F);
        this.rootHeight = deep ? -1.8F : -1.0F;
        this.heightVariation = 0.1F;
        this.topBlock = Blocks.sand;
        this.fillerBlock = Blocks.sand;
        // Java 1.20 Warm Ocean water colour.
        this.waterColorMultiplier = 0x43D5EE;
    }
}
