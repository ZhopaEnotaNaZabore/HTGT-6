package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeAdapter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaClimateSampler120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeZoomer120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.OverworldBiomeParameterMap120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.MutableDensityContext120;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

import java.util.List;
import java.util.Random;

/**
 * Futura surface-biome manager backed by the 1.20.2 climate sampler and
 * OverworldBiomeBuilder parameter map. Chunk storage remains one biome per
 * column, therefore this manager queries the vanilla surface depth point 0;
 * cave-biome depth points remain available through the climate runtime.
 */
public final class FuturaWorldChunkManager120 extends WorldChunkManager {
    private static boolean contractValidated;

    private final FuturaClimateSampler120 climate;
    private final OverworldBiomeParameterMap120 parameters;
    private final FuturaBiomeZoomer120 biomeZoomer;
    private final long seed;
    private volatile FuturaMountainDensityRouter120 commandTerrain;
    private final ThreadLocal<MutableDensityContext120> depthSample=
            new ThreadLocal<MutableDensityContext120>(){
                @Override protected MutableDensityContext120 initialValue(){return new MutableDensityContext120();}
            };

    public FuturaWorldChunkManager120(long seed) {
        this.seed=seed;
        climate=new FuturaClimateSampler120(seed);
        parameters=new OverworldBiomeParameterMap120();
        biomeZoomer=new FuturaBiomeZoomer120(seed);
    }

    @Override
    public BiomeGenBase getBiomeGenAt(int x, int z) {
        return getSurfaceBiomeAt(x,z);
    }

    /** Creation-order-independent 1.7 column projection of the 1.20 3D biome source. */
    public BiomeGenBase getSurfaceBiomeAt(int blockX,int blockZ) {
        MutableDensityContext120 sample=depthSample.get();
        FuturaMountainDensityRouter120 terrain=terrain();
        return getEstimatedSurfaceBiomeAt(blockX,blockZ,terrain,sample);
    }

    /**
     * Resolves the biome used by SurfaceSystem at the actual surface quart.
     * 1.20 chunks store biomes in 4x4x4 cells and SurfaceSystem asks the
     * biome source at the block being replaced.  A 1.7 chunk can retain only
     * one ID per x/z column, so the least-lossy representation is the exact
     * noise biome at that column's surface, not an arbitrary modern Y=0
     * slice.
     */
    public BiomeGenBase getSurfaceBiomeAt(int blockX,int physicalSurfaceY,int blockZ,
                                    FuturaMountainDensityRouter120 terrain,
                                    MutableDensityContext120 sample){
        int modernY=physicalSurfaceY-FuturaClimateSampler120.MODERN_TO_FUTURA_Y;
        FuturaBiomeZoomer120.QuartPosition quart=biomeZoomer.select(blockX,modernY,blockZ);
        int sampleX=quart.x*4;
        int sampleY=quart.y*4+FuturaClimateSampler120.MODERN_TO_FUTURA_Y;
        int sampleZ=quart.z*4;
        float depth=(float)terrain.depth().sample(sample.set(sampleX,sampleY,sampleZ));
        return FuturaBiomeAdapter120.resolve(parameters.select(
                climate.sample(sampleX,sampleY,sampleZ,depth)));
    }

    public BiomeGenBase[] loadSurfaceBiomeData(BiomeGenBase[] out,int x,int z,int width,int depth,
                                         int[] physicalSurfaceHeights,
                                         FuturaMountainDensityRouter120 terrain){
        if(physicalSurfaceHeights==null||physicalSurfaceHeights.length<width*depth)
            throw new IllegalArgumentException("Surface height array is smaller than biome area");
        if(out==null||out.length<width*depth)out=new BiomeGenBase[width*depth];
        MutableDensityContext120 sample=new MutableDensityContext120();
        for(int dz=0;dz<depth;dz++)for(int dx=0;dx<width;dx++){
            int index=dx+dz*width;
            // Do not derive the persisted identity from post-cave/post-aquifer
            // blocks. Unloaded WorldChunkManager queries cannot reproduce that
            // mutable result. The reference preliminary surface is deterministic
            // from the NoiseRouter and keeps both paths identical.
            out[index]=getEstimatedSurfaceBiomeAt(x+dx,z+dz,terrain,sample);
        }
        return out;
    }

    private BiomeGenBase getEstimatedSurfaceBiomeAt(int blockX,int blockZ,
                                                      FuturaMountainDensityRouter120 terrain,
                                                      MutableDensityContext120 sample){
        int surfaceY=terrain.preliminarySurfaceLevel(blockX,blockZ,
                com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.PHYSICAL_HEIGHT-1,sample);
        if(surfaceY==Integer.MAX_VALUE)surfaceY=FuturaMountainDensityRouter120.SEA_LEVEL;
        return getSurfaceBiomeAt(blockX,surfaceY,blockZ,terrain,sample);
    }

    private FuturaMountainDensityRouter120 terrain(){
        FuturaMountainDensityRouter120 terrain=commandTerrain;
        if(terrain==null)synchronized(this){
            terrain=commandTerrain;
            if(terrain==null)commandTerrain=terrain=new FuturaMountainDensityRouter120(seed);
        }
        return terrain;
    }

    /**
     * 3D climate lookup seam for cave feature runtimes.  It deliberately does
     * not write a cave ID into 1.7's one-biome-per-column storage.
     */
    public BiomeGenBase getUndergroundBiomeAt(int blockX,int physicalY,int blockZ,
                                               FuturaMountainDensityRouter120 terrain,MutableDensityContext120 sample){
        return FuturaBiomeAdapter120.resolve(getBiomeKeyAt(blockX,physicalY,blockZ,terrain,sample));
    }

    public BiomeGenBase getUndergroundBiomeAt(int blockX,int physicalY,int blockZ,float depth) {
        int modernY=physicalY-FuturaClimateSampler120.MODERN_TO_FUTURA_Y;
        FuturaBiomeZoomer120.QuartPosition quart=biomeZoomer.select(blockX,modernY,blockZ);
        return FuturaBiomeAdapter120.resolve(parameters.select(climate.sample(
                quart.x*4,quart.y*4+FuturaClimateSampler120.MODERN_TO_FUTURA_Y,quart.z*4,depth)));
    }

    /**
     * Exact cave-biome query for generation code which already owns the terrain
     * router.  Depth must be sampled at the quart selected by BiomeManager, not
     * at the unzoomed feature position; otherwise cave-biome seams disagree with
     * 1.20 at cell boundaries.
     */
    public OverworldBiomeParameterMap120.Biome getBiomeKeyAt(int blockX,int physicalY,int blockZ,
                                               FuturaMountainDensityRouter120 terrain,
                                               MutableDensityContext120 sample) {
        if(terrain==null)throw new NullPointerException("terrain");
        if(sample==null)throw new NullPointerException("sample");
        int modernY=physicalY-FuturaClimateSampler120.MODERN_TO_FUTURA_Y;
        FuturaBiomeZoomer120.QuartPosition quart=biomeZoomer.select(blockX,modernY,blockZ);
        int sampleX=quart.x*4;
        int sampleY=quart.y*4+FuturaClimateSampler120.MODERN_TO_FUTURA_Y;
        int sampleZ=quart.z*4;
        float depth=(float)terrain.depth().sample(sample.set(sampleX,sampleY,sampleZ));
        return parameters.select(climate.sample(sampleX,sampleY,sampleZ,depth));
    }

    /** Unzoomed biome source used by structure placement, in physical block coordinates. */
    public OverworldBiomeParameterMap120.Biome noiseBiomeKey(int x,int y,int z,FuturaMountainDensityRouter120 terrain,MutableDensityContext120 sample){
        x=(x>>2)<<2;z=(z>>2)<<2;y=((y-64)>>2)*4+64;
        float depth=(float)terrain.depth().sample(sample.set(x,y,z));
        return parameters.select(climate.sample(x,y,z,depth));
    }

    /**
     * Exact 1.20.2 NoiseRouter-backed 3D biome query.  Commands and cave
     * runtimes can use this without touching a chunk; unlike the 1.7 column
     * biome API it supplies terrain.depth at the queried Y coordinate.
     */
    public BiomeGenBase getUndergroundBiomeAt(int blockX,int physicalY,int blockZ) {
        MutableDensityContext120 sample=depthSample.get();
        return getUndergroundBiomeAt(blockX,physicalY,blockZ,terrain(),sample);
    }

    @Override
    public float[] getRainfall(float[] out, int x, int z, int width, int depth) {
        if (out == null || out.length < width * depth) out = new float[width * depth];
        BiomeGenBase[] biomes = loadBlockGeneratorData(null, x, z, width, depth);
        for (int i = 0; i < width * depth; i++)
            out[i] = Math.min(1.0F, biomes[i].getIntRainfall() / 65536.0F);
        return out;
    }

    @Override
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] out, int x, int z,
                                                  int width, int depth) {
        if (out == null || out.length < width * depth) out = new BiomeGenBase[width * depth];
        FuturaMountainDensityRouter120 terrain=terrain();
        MutableDensityContext120 sample=depthSample.get();
        for (int dz=0;dz<depth;dz++) for(int dx=0;dx<width;dx++)
            out[dx+dz*width]=getEstimatedSurfaceBiomeAt((x+dx)<<2,(z+dz)<<2,terrain,sample);
        return out;
    }

    @Override
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] out, int x, int z,
                                                  int width, int depth) {
        return transform(out,x,z,width,depth);
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] out, int x, int z,
                                         int width, int depth, boolean cache) {
        return transform(out,x,z,width,depth);
    }

    private BiomeGenBase[] transform(BiomeGenBase[] out,int x,int z,int width,int depth) {
        if (out == null || out.length < width * depth) out = new BiomeGenBase[width * depth];
        FuturaMountainDensityRouter120 terrain=terrain();
        MutableDensityContext120 sample=depthSample.get();
        for (int dz = 0; dz < depth; dz++) for (int dx = 0; dx < width; dx++) {
            int index = dx + dz * width;
            out[index]=getEstimatedSurfaceBiomeAt(x+dx,z+dz,terrain,sample);
        }
        return out;
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List allowed) {
        int minX = (x - radius) >> 2, minZ = (z - radius) >> 2;
        int maxX = (x + radius) >> 2, maxZ = (z + radius) >> 2;
        int width = maxX - minX + 1, depth = maxZ - minZ + 1;
        BiomeGenBase[] biomes = getBiomesForGeneration(null, minX, minZ, width, depth);
        for (BiomeGenBase biome : biomes) if (!allowed.contains(biome)) return false;
        return true;
    }

    @Override
    public ChunkPosition findBiomePosition(int x, int z, int radius, List allowed, Random random) {
        int minX = (x - radius) >> 2, minZ = (z - radius) >> 2;
        int maxX = (x + radius) >> 2, maxZ = (z + radius) >> 2;
        int width = maxX - minX + 1, depth = maxZ - minZ + 1, count = 0;
        ChunkPosition found = null;
        BiomeGenBase[] biomes = getBiomesForGeneration(null, minX, minZ, width, depth);
        for (int index = 0; index < biomes.length; index++) {
            if (allowed.contains(biomes[index]) && random.nextInt(++count) == 0)
                found = new ChunkPosition((minX + index % width) << 2, 0,
                        (minZ + index / width) << 2);
        }
        return found;
    }

    @Override
    public void cleanupCache() {
        // The climate sampler and immutable parameter map own no region cache.
    }

    public static synchronized void validateContract() {
        if (contractValidated) return;
        FuturaWorldChunkManager120 manager = new FuturaWorldChunkManager120(0x465554555241120L);
        boolean cold = false, warm = false, ocean=false, land=false, extended=false;
        for (int z = -8192; z <= 8192; z += 1021) for (int x = -8192; x <= 8192; x += 997) {
            BiomeGenBase biome = manager.getBiomeGenAt(x, z);
            if (biome == null) throw new IllegalStateException("Futura climate sampler returned null biome");
            cold |= biome.temperature <= 0.25F;
            warm |= biome.temperature >= 0.8F;
            ocean|=biome==BiomeGenBase.ocean||biome==BiomeGenBase.deepOcean||biome==BiomeGenBase.frozenOcean;
            land|=biome!=BiomeGenBase.ocean&&biome!=BiomeGenBase.deepOcean&&biome!=BiomeGenBase.frozenOcean;
            extended|=biome.biomeID>=FuturaBiomeIds120.MIN_ID;
        }
        if (!cold || !warm || !ocean || !land || !extended)
            throw new IllegalStateException("Futura climate parameter map lacks required biome families");
        contractValidated = true;
    }
}
