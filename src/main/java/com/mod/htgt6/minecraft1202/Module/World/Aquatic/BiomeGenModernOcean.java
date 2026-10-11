package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenOcean;

/** A 1.13 ocean temperature/depth identity adapted to the 1.7.10 biome model. */
public final class BiomeGenModernOcean extends BiomeGenOcean {
    public BiomeGenModernOcean(int id, String name, float temperature, boolean deep,
                        int waterColor, boolean sandyFloor) {
        super(id);
        setBiomeName(name);
        setTemperatureRainfall(temperature, 0.5F);
        rootHeight = deep ? -1.8F : -1.0F;
        heightVariation = 0.1F;
        topBlock = sandyFloor ? Blocks.sand : Blocks.gravel;
        fillerBlock = sandyFloor ? Blocks.sand : Blocks.stone;
        waterColorMultiplier = waterColor;
    }
}
