package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded;

import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysGeology.Profile;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/** World-coordinate fields keep oceans, massifs and cryovolcanoes continuous across chunks. */
public final class SolSysTerrainFeatures {
    public static final int SEA_LEVEL = 64;
    private SolSysTerrainFeatures() { }

    public static final class Sample {
        public final int height;
        public final boolean ocean, shore, flow, vent;
        Sample(int height, boolean ocean, boolean shore, boolean flow, boolean vent) {
            this.height = height; this.ocean = ocean; this.shore = shore;
            this.flow = flow; this.vent = vent;
        }
    }

    public static Sample sample(Profile profile, long worldSeed, int x, int z, int oldHeight) {
        long seed = worldSeed ^ profile.worldName.hashCode() * 104729L;
        double continent = noise(seed, x / 640.0, z / 640.0);
        double ridge = Math.max(0, 1 - Math.abs(noise(seed + 31, x / 180.0, z / 180.0)) * 3);
        double massif = ridge * ridge * profile.massifHeight
                * Math.max(0, noise(seed + 61, x / 420.0, z / 420.0));
        int height = oldHeight + (int) Math.round(massif);
        boolean flow = false, vent = false;
        if (profile.hasOceans()) {
            height = (int) Math.round(SEA_LEVEL + (continent - profile.oceanThreshold) * 58
                    + (oldHeight - 70) * 0.35 + massif);
            int cell = profile.volcanoCell;
            int cx = Math.floorDiv(x, cell), cz = Math.floorDiv(z, cell);
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                long h = hash(seed + 113, cx + dx, cz + dz);
                if ((h & 3) != 0) continue;
                double px = (cx + dx) * (double) cell + ((h >>> 8) & 255) / 255.0 * cell;
                double pz = (cz + dz) * (double) cell + ((h >>> 24) & 255) / 255.0 * cell;
                double radius = 64 + ((h >>> 40) & 63);
                double d = Math.hypot(x - px, z - pz) / radius;
                if (d >= 1) continue;
                double cone = (1 - d) * (1 - d) * 52;
                if (d < 0.18) cone -= (1 - d / 0.18) * 18;
                height += (int) Math.round(cone);
                flow = d < 0.85;
                vent = d < 0.18;
            }
        }
        height = Math.max(12, Math.min(220, height));
        return new Sample(height, profile.hasOceans() && height < SEA_LEVEL,
                profile.hasOceans() && Math.abs(height - SEA_LEVEL) <= 2, flow, vent);
    }

    public static void apply(Profile profile, long seed, int chunkX, int chunkZ,
                             Block[] blocks, byte[] metadata) {
        if (profile == null || profile.strata == null) return;
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            int base = (x * 16 + z) * 256;
            int top = 255;
            while (top > 0 && (blocks[base + top] == null || blocks[base + top] == Blocks.air)) top--;
            Block originalSurface = blocks[base + top];
            Sample sample = sample(profile, seed, chunkX * 16 + x, chunkZ * 16 + z, top);
            Block surface = sample.vent ? profile.cryoVent : sample.flow ? profile.cryoFlow
                    : sample.ocean ? profile.sediment : sample.shore ? profile.salt : originalSurface;
            int bandOffset = (int) Math.round(noise(seed + 211, (chunkX * 16 + x) / 90.0,
                    (chunkZ * 16 + z) / 90.0) * 8);
            for (int y = 1; y <= Math.max(top, sample.height); y++) {
                int index = base + y;
                if (y > sample.height) { blocks[index] = null; metadata[index] = 0; }
                else if (y >= sample.height - 3) { blocks[index] = surface; metadata[index] = 0; }
                else if (y > top || (blocks[index] != null && blocks[index] != Blocks.air
                        && Math.floorMod(y + bandOffset, 19) < 5)) {
                    blocks[index] = profile.strata; metadata[index] = 0;
                }
            }
            if (sample.ocean) for (int y = sample.height + 1; y <= SEA_LEVEL; y++) {
                blocks[base + y] = SolSysGeology.saltWater; metadata[base + y] = 0;
            }
        }
    }

    private static double noise(long seed, double x, double z) {
        int ix = (int) Math.floor(x), iz = (int) Math.floor(z);
        double tx = x - ix, tz = z - iz;
        tx = tx * tx * (3 - 2 * tx); tz = tz * tz * (3 - 2 * tz);
        return lerp(lerp(value(seed, ix, iz), value(seed, ix + 1, iz), tx),
                lerp(value(seed, ix, iz + 1), value(seed, ix + 1, iz + 1), tx), tz);
    }
    private static double value(long seed, int x, int z) { return (hash(seed, x, z) & 0xffffffL) / 8388607.5 - 1; }
    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }
    private static long hash(long seed, int x, int z) {
        long h = seed + x * 341873128712L + z * 132897987541L;
        h = (h ^ h >>> 33) * 0xff51afd7ed558ccdL;
        h = (h ^ h >>> 33) * 0xc4ceb9fe1a85ec53L;
        return h ^ h >>> 33;
    }
}
