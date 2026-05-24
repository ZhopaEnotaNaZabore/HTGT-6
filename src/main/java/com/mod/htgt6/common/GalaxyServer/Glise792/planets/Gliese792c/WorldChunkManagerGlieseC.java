package com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c;




import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.mod.htgt6.common.GalaxyServer.Glise792.planets.Gliese792c.biomeGliese792c.BiomeGenBaseGlieseC;

import net.minecraft.world.ChunkPosition;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

public class WorldChunkManagerGlieseC extends WorldChunkManager {

    private final BiomeCache biomeCache;

    private final List<BiomeGenBase> biomesToSpawnIn;

    public WorldChunkManagerGlieseC() {

        this.biomeCache = new BiomeCache(this);

        this.biomesToSpawnIn =
                Arrays.asList(
                        BiomeGenBaseGlieseC.gliesec
                );
    }

    @Override
    public List<BiomeGenBase> getBiomesToSpawnIn() {

        return this.biomesToSpawnIn;
    }

    @Override
    public BiomeGenBase getBiomeGenAt(
            int x,
            int z) {

        return BiomeGenBaseGlieseC.gliesec;
    }

    @Override
    public float[] getRainfall(
            float[] listToReuse,
            int x,
            int z,
            int width,
            int length) {

        if (listToReuse == null
                || listToReuse.length < width * length) {

            listToReuse =
                    new float[width * length];
        }

        Arrays.fill(listToReuse, 1.0F);

        return listToReuse;
    }

    @Override
    public BiomeGenBase[] getBiomesForGeneration(
            BiomeGenBase[] biomes,
            int x,
            int z,
            int width,
            int length) {

        if (biomes == null
                || biomes.length < width * length) {

            biomes =
                    new BiomeGenBase[width * length];
        }

        Arrays.fill(
                biomes,
                BiomeGenBaseGlieseC.gliesec
        );

        return biomes;
    }

    @Override
    public BiomeGenBase[] loadBlockGeneratorData(
            BiomeGenBase[] biomes,
            int x,
            int z,
            int width,
            int length) {

        return this.getBiomesForGeneration(
                biomes,
                x,
                z,
                width,
                length
        );
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(
            BiomeGenBase[] biomes,
            int x,
            int z,
            int width,
            int length,
            boolean cacheFlag) {

        return this.getBiomesForGeneration(
                biomes,
                x,
                z,
                width,
                length
        );
    }

    @Override
    public ChunkPosition findBiomePosition(
            int x,
            int z,
            int range,
            List list,
            Random rand) {

        return new ChunkPosition(
                x,
                64,
                z
        );
    }

    @Override
    public boolean areBiomesViable(
            int x,
            int z,
            int radius,
            List list) {

        return true;
    }
}
