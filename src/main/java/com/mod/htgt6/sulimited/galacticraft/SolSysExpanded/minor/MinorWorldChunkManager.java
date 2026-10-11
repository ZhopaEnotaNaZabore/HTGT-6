package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.ChunkPosition;

abstract class MinorWorldChunkManager extends WorldChunkManager {
    private final BiomeGenBase biome;
    MinorWorldChunkManager(BiomeGenBase biome) { this.biome = biome; }
    public BiomeGenBase getBiomeGenAt(int x, int z) { return biome; }
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] result, int x, int z, int width, int height) {
        return fill(result, width * height);
    }
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] result, int x, int z, int width, int height) {
        return fill(result, width * height);
    }
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] result, int x, int z, int width, int height, boolean cache) {
        return fill(result, width * height);
    }
    public boolean areBiomesViable(int x, int z, int radius, List biomes) { return biomes.contains(biome); }
    public ChunkPosition findBiomePosition(int x, int z, int radius, List biomes, Random random) {
        return biomes.contains(biome) ? new ChunkPosition(x, 0, z) : null;
    }
    private BiomeGenBase[] fill(BiomeGenBase[] result, int size) {
        if (result == null || result.length < size) result = new BiomeGenBase[size];
        Arrays.fill(result, 0, size, biome);
        return result;
    }
}
