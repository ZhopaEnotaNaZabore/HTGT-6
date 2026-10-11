package com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

// FIX: Import the shared SpaceUltimate biome wrapper


import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

public class WorldChunkManagerGlieseC extends WorldChunkManager {

    private final BiomeCache biomeCache;
    private final List<BiomeGenBase> biomesToSpawnIn;

    public WorldChunkManagerGlieseC() {
        this.biomeCache = new BiomeCache(this);
        // FIX: Replaced old biome with the wrapper
        this.biomesToSpawnIn = Arrays.asList(BiomeGenSpaceUltimate.glieseC);
    }

    @Override
    public List<BiomeGenBase> getBiomesToSpawnIn() {
        return this.biomesToSpawnIn;
    }

    @Override
    public BiomeGenBase getBiomeGenAt(int x, int z) {
        // FIX: Return the wrapper biome
        return BiomeGenSpaceUltimate.glieseC;
    }

    @Override
    public float[] getRainfall(float[] listToReuse, int x, int z, int width, int length) {
        if (listToReuse == null || listToReuse.length < width * length) {
            listToReuse = new float[width * length];
        }
        Arrays.fill(listToReuse, 1.0F);
        return listToReuse;
    }

    @Override
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] biomes, int x, int z, int width, int length) {
        if (biomes == null || biomes.length < width * length) {
            biomes = new BiomeGenBase[width * length];
        }
        // FIX: Fill the array with the wrapper biome
        Arrays.fill(biomes, BiomeGenSpaceUltimate.glieseC);
        return biomes;
    }

    @Override
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] biomes, int x, int z, int width, int length) {
        return this.getBiomesForGeneration(biomes, x, z, width, length);
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] biomes, int x, int z, int width, int length, boolean cacheFlag) {
        return this.getBiomesForGeneration(biomes, x, z, width, length);
    }

    @Override
    public ChunkPosition findBiomePosition(int x, int z, int range, List list, Random rand) {
        return new ChunkPosition(x, 64, z);
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List list) {
        return true;
    }
}
