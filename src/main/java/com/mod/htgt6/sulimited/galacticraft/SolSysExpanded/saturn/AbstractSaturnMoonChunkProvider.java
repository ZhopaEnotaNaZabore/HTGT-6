package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import com.mod.htgt6.sulimited.world.GT6PlanetOreGeneration;
import com.mod.htgt6.sulimited.worldgen.constructor.SUChunkContext;
import com.mod.htgt6.sulimited.worldgen.constructor.SUWorldConstructorPipeline;
import java.util.Collections;
import java.util.List;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysChunkProvider;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.MapGenBaseMeta;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

/** Shared column writer; each moon supplies an independent terrain sampler. */
abstract class AbstractSaturnMoonChunkProvider extends SolSysChunkProvider {
    protected final World world;
    private final BiomeGenBase biome;
    private final Block surface;
    private final Block subsurface;
    private final Block core;
    private SUWorldConstructorPipeline constructors;

    AbstractSaturnMoonChunkProvider(World world, long seed, boolean features, BiomeGenBase biome,
                                    Block surface, Block subsurface, Block core) {
        super(world, seed, features);
        this.world = world;
        this.biome = biome;
        this.surface = surface;
        this.subsurface = subsurface;
        this.core = core;
    }

    protected abstract MoonTerrainSample sample(long seed, int x, int z);
    protected void configureConstructors(SUWorldConstructorPipeline pipeline) { }
    private SUWorldConstructorPipeline constructors() {
        if (constructors == null) {
            constructors = new SUWorldConstructorPipeline();
            configureConstructors(constructors);
        }
        return constructors;
    }
    protected BiomeGenBase.SpawnListEntry[] getMonsters() { return new BiomeGenBase.SpawnListEntry[0]; }
    protected BiomeGenBase.SpawnListEntry[] getCreatures() { return new BiomeGenBase.SpawnListEntry[0]; }
    protected List<MapGenBaseMeta> getWorldGenerators() { return Collections.emptyList(); }
    protected BiomeGenBase[] getBiomesForGeneration() { return new BiomeGenBase[] { biome }; }
    protected int getSeaLevel() { return 0; }
    protected BlockMetaPair getGrassBlock() { return new BlockMetaPair(surface, (byte) 0); }
    protected BlockMetaPair getDirtBlock() { return new BlockMetaPair(subsurface, (byte) 0); }
    protected BlockMetaPair getStoneBlock() { return new BlockMetaPair(core, (byte) 0); }

    public final void onChunkProvide(int chunkX, int chunkZ, Block[] blocks, byte[] metadata) {
        long seed = world.getSeed();
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            MoonTerrainSample terrain = sample(seed, chunkX * 16 + x, chunkZ * 16 + z);
            int height = Math.max(8, Math.min(190, terrain.height));
            int base = (x * 16 + z) * 256;
            // ChunkProviderSpace has already written its default terrain.
            // Clear it so deep craters and chasms cannot leave floating shelves.
            for (int y = 1; y < 256; y++) {
                blocks[base + y] = null;
                metadata[base + y] = 0;
            }
            blocks[base] = Blocks.bedrock;
            for (int y = 1; y <= height; y++) {
                blocks[base + y] = y == height ? terrain.surface
                        : y >= height - 4 ? terrain.subsurface : core;
            }
        }
        constructors().construct(new SUChunkContext(world, blocks, metadata, chunkX, chunkZ));
    }

    public void onPopulate(IChunkProvider provider, int chunkX, int chunkZ) { }
    public void populate(IChunkProvider provider, int chunkX, int chunkZ) {
        super.populate(provider, chunkX, chunkZ);
        GT6PlanetOreGeneration.generate(world, chunkX, chunkZ);
        constructors().populate(world, chunkX, chunkZ, provider);
    }
    public double getHeightModifier() { return 0.0D; }
    public double getSmallFeatureHeightModifier() { return 0.0D; }
    public double getMountainHeightModifier() { return 0.0D; }
    public double getValleyHeightModifier() { return 0.0D; }
    public int getCraterProbability() { return 0; }

    protected final double noise(long seed, double x, double z, double scale, int octaves) {
        double sum = 0.0D, amplitude = 1.0D, total = 0.0D;
        for (int octave = 0; octave < octaves; octave++) {
            sum += value(seed + octave * 1013L, x * scale, z * scale) * amplitude;
            total += amplitude; amplitude *= 0.5D; scale *= 2.0D;
        }
        return sum / total;
    }
    protected final Crater crater(long seed, int x, int z, int cell, int minRadius, int maxRadius) {
        int cx = floorDiv(x, cell), cz = floorDiv(z, cell);
        double depth = 0.0D, rim = 0.0D, ejecta = 0.0D;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            long hash = hash(seed, cx + dx, cz + dz);
            double centerX = (cx + dx) * cell + ((hash >>> 8) & 255L) / 255.0D * cell;
            double centerZ = (cz + dz) * cell + ((hash >>> 24) & 255L) / 255.0D * cell;
            double radius = minRadius + ((hash >>> 40) & 127L) / 127.0D * (maxRadius - minRadius);
            double distance = Math.sqrt((x-centerX)*(x-centerX)+(z-centerZ)*(z-centerZ)) / radius;
            if (distance < 1.0D) depth = Math.max(depth, 1.0D-distance*distance);
            else if (distance < 1.18D) rim = Math.max(rim, 1.0D-Math.abs(distance-1.08D)/0.10D);
            else if (distance < 2.2D) ejecta = Math.max(ejecta, (2.2D-distance)/1.02D);
        }
        return new Crater(depth, rim, ejecta);
    }
    protected static final class Crater {
        final double depth, rim, ejecta;
        Crater(double depth, double rim, double ejecta) { this.depth=depth; this.rim=rim; this.ejecta=ejecta; }
    }
    private double value(long seed, double x, double z) {
        int x0=(int)Math.floor(x), z0=(int)Math.floor(z);
        double tx=smooth(x-x0), tz=smooth(z-z0);
        return lerp(lerp(random(seed,x0,z0),random(seed,x0+1,z0),tx),
                lerp(random(seed,x0,z0+1),random(seed,x0+1,z0+1),tx),tz);
    }
    private double random(long seed,int x,int z){return(hash(seed,x,z)&0x7fffffffL)/1073741823.5D-1.0D;}
    private long hash(long seed,int x,int z){long h=seed+x*341873128712L+z*132897987541L;h=(h^(h>>>33))*0xff51afd7ed558ccdL;h=(h^(h>>>33))*0xc4ceb9fe1a85ec53L;return h^(h>>>33);}
    private double smooth(double v){return v*v*(3.0D-2.0D*v);} private double lerp(double a,double b,double t){return a+(b-a)*t;}
    private int floorDiv(int a,int b){int q=a/b;return(a^b)<0&&q*b!=a?q-1:q;}
}
