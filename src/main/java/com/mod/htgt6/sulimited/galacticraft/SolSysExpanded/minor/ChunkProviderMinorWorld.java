package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor;

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
import com.mod.htgt6.sulimited.world.GT6PlanetOreGeneration;

/** Seed-stable shared terrain engine with a distinct profile for each world. */
public final class ChunkProviderMinorWorld extends SolSysChunkProvider {
    private final World world;
    private final MinorWorlds.Definition definition;

    public ChunkProviderMinorWorld(World world, long seed, boolean features) {
        super(world, seed, features);
        this.world = world;
        this.definition = MinorWorlds.forDimension(world.provider.dimensionId);
    }

    protected BiomeGenBase.SpawnListEntry[] getMonsters() { return new BiomeGenBase.SpawnListEntry[0]; }
    protected BiomeGenBase.SpawnListEntry[] getCreatures() { return new BiomeGenBase.SpawnListEntry[0]; }
    protected List<MapGenBaseMeta> getWorldGenerators() { return Collections.emptyList(); }
    protected BiomeGenBase[] getBiomesForGeneration() {
        // ChunkProviderSpace calls this virtual method from its own constructor,
        // before Java is allowed to initialise this.definition.  The array is
        // only a reusable buffer: provide it eagerly and let the dimension's
        // WorldChunkManager fill the actual biome values during chunk creation.
        return initialBiomeBuffer();
    }
    public static BiomeGenBase[] initialBiomeBuffer() { return new BiomeGenBase[256]; }
    protected int getSeaLevel() { return 0; }
    protected BlockMetaPair getGrassBlock() { return new BlockMetaPair(definition.surface, (byte) 0); }
    protected BlockMetaPair getDirtBlock() { return new BlockMetaPair(definition.crust, (byte) 0); }
    protected BlockMetaPair getStoneBlock() { return new BlockMetaPair(definition.core, (byte) 0); }

    public void onChunkProvide(int chunkX, int chunkZ, Block[] blocks, byte[] meta) {
        long seed = world.getSeed() + definition.terrainStyle * 104729L;
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            int wx = chunkX * 16 + x, wz = chunkZ * 16 + z;
            double broad = noise(seed + 11, wx, wz, 0.0022D, 4);
            double detail = noise(seed + 37, wx, wz, 0.013D, 3);
            double special = specialRelief(seed, wx, wz, definition.terrainStyle);
            int height = Math.max(18, Math.min(178, 70 + (int) Math.round(broad * 17 + detail * 7 + special)));
            int base = (x * 16 + z) * 256;
            blocks[base] = Blocks.bedrock;
            for (int y = 1; y <= height; y++) {
                int depth = height - y;
                blocks[base + y] = depth == 0 ? definition.surface : depth < 5 ? definition.crust : definition.core;
            }
        }
    }

    private double specialRelief(long seed, int x, int z, int style) {
        double crater = crater(seed + 71, x, z, style == 3 ? 330 : 180, style == 0 ? 38 : 25);
        switch (style) {
            case 0: // Ariel: broad graben and bright fault scarps.
                return -Math.max(0.0D, 0.13D - Math.abs(noise(seed + 89, x, z, 0.010D, 2))) * 190.0D
                        + crater;
            case 1: // Haumea: pronounced longitudinal ridges.
                return Math.sin((x + noise(seed, x, z, 0.004D, 2) * 90.0D) * 0.026D) * 12.0D + crater;
            case 2: // Makemake: subdued methane-ice plains.
                return noise(seed + 103, x, z, 0.0009D, 3) * 8.0D + crater * 0.55D;
            case 3: // Eris: ancient, large impact basins.
                return crater * 1.55D + Math.abs(noise(seed + 127, x, z, 0.007D, 2)) * 8.0D;
            default: // DeeDee: distant, heavily cratered mixed ice-rock.
                return crater + crater(seed + 151, x, z, 92, 13) + noise(seed, x, z, 0.020D, 2) * 5.0D;
        }
    }

    private double crater(long seed, int x, int z, int cell, int depth) {
        int cx = floorDiv(x, cell), cz = floorDiv(z, cell);
        double relief = 0.0D;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            long hash = hash(seed, cx + dx, cz + dz);
            double px = (cx + dx) * cell + ((hash >>> 8) & 255) / 255.0D * cell;
            double pz = (cz + dz) * cell + ((hash >>> 24) & 255) / 255.0D * cell;
            double radius = cell * (0.18D + ((hash >>> 40) & 63) / 512.0D);
            double distance = Math.sqrt((x - px) * (x - px) + (z - pz) * (z - pz)) / radius;
            if (distance < 1.0D) relief = Math.min(relief, -depth * (1.0D - distance * distance));
            else if (distance < 1.22D) relief = Math.max(relief, depth * 0.28D * (1.22D - distance) / 0.22D);
        }
        return relief;
    }

    private double noise(long seed, double x, double z, double scale, int octaves) {
        double sum = 0, amplitude = 1, total = 0;
        for (int octave = 0; octave < octaves; octave++) {
            sum += value(seed + octave * 1013L, x * scale, z * scale) * amplitude;
            total += amplitude; amplitude *= 0.5D; scale *= 2.0D;
        }
        return sum / total;
    }
    private double value(long seed, double x, double z) {
        int x0 = (int) Math.floor(x), z0 = (int) Math.floor(z);
        double tx = smooth(x - x0), tz = smooth(z - z0);
        return lerp(lerp(random(seed, x0, z0), random(seed, x0 + 1, z0), tx),
                lerp(random(seed, x0, z0 + 1), random(seed, x0 + 1, z0 + 1), tx), tz);
    }
    private double random(long seed, int x, int z) { return (hash(seed, x, z) & 0x7fffffffL) / 1073741823.5D - 1.0D; }
    private long hash(long seed, int x, int z) { long h = seed + x * 341873128712L + z * 132897987541L; h = (h ^ h >>> 33) * 0xff51afd7ed558ccdL; h = (h ^ h >>> 33) * 0xc4ceb9fe1a85ec53L; return h ^ h >>> 33; }
    private double smooth(double value) { return value * value * (3.0D - 2.0D * value); }
    private double lerp(double first, double second, double amount) { return first + (second - first) * amount; }
    private int floorDiv(int value, int divisor) { int result = value / divisor; return (value ^ divisor) < 0 && result * divisor != value ? result - 1 : result; }

    public void onPopulate(IChunkProvider provider, int x, int z) { }
    public void populate(IChunkProvider provider, int x, int z) {
        super.populate(provider, x, z);
        GT6PlanetOreGeneration.generate(world, x, z);
    }
    public double getHeightModifier() { return 0; }
    public double getSmallFeatureHeightModifier() { return 0; }
    public double getMountainHeightModifier() { return 0; }
    public double getValleyHeightModifier() { return 0; }
    public int getCraterProbability() { return 0; }
}
