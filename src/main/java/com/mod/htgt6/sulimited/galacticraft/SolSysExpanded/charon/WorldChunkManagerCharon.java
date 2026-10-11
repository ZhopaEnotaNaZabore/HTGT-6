package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.charon;

import java.util.Arrays;
import java.util.List;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

public final class WorldChunkManagerCharon extends WorldChunkManager {
    private final BiomeGenBase biome;

    public WorldChunkManagerCharon() {
        this.biome = com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate.charon;
    }
    public List<BiomeGenBase> getBiomesToSpawnIn() { return Arrays.asList(biome); }
    public BiomeGenBase getBiomeGenAt(int x, int z) { return biome; }
    public float[] getRainfall(float[] out, int x, int z, int width, int length) {
        if (out == null || out.length < width * length) out = new float[width * length];
        Arrays.fill(out, 0.0F); return out;
    }
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] out, int x, int z, int width, int length) {
        if (out == null || out.length < width * length) out = new BiomeGenBase[width * length];
        Arrays.fill(out, biome); return out;
    }
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] out, int x, int z, int width, int length) { return getBiomesForGeneration(out,x,z,width,length); }
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] out, int x, int z, int width, int length, boolean cache) { return getBiomesForGeneration(out,x,z,width,length); }
    public ChunkPosition findBiomePosition(int x, int z, int range, List list, java.util.Random random) { return new ChunkPosition(x, 64, z); }
    public boolean areBiomesViable(int x, int z, int radius, List list) { return true; }
}
