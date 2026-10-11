package com.mod.htgt6.minecraft1202.Module.World.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulGuardian;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;

import cpw.mods.fml.common.IWorldGenerator;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanBiomeRegistry;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.OceanMonumentData120;

/**
 * Chunk-clipped Ocean Monument backport. Placement follows 1.12's 32/5 regional contract and the
 * 58x23x58 building is assembled independently by every intersecting chunk, avoiding 1.7 populate
 * recursion and seams at chunk borders.
 */
public final class OceanMonumentGenerator implements IWorldGenerator {
    private static final int SPACING = 32;
    private static final int SEPARATION = 5;
    private static final int SALT = 10387313;
    private static final int WIDTH = 58;
    private static final int MODERN_BASE_Y = 39;
    // Sea level is an exclusive water ceiling: vanilla water ends at Y=62.
    // The base and water surface receive the same +64 translation in Futura.
    private static final int WATER_TOP_LOCAL_Y = 63 - MODERN_BASE_Y - 1;

    @Override
    public void generate(Random ignored, int chunkX, int chunkZ, World world,
            IChunkProvider generator, IChunkProvider provider) {
        if (world.provider.dimensionId != 0 || !world.getWorldInfo().isMapFeaturesEnabled()) return;
        // Only region candidates can be starts. The former 9x9 chunk scan created
        // 81 Random instances per populated chunk even though this range intersects
        // at most four 32-chunk regions.
        int minRegionX=floorDiv(chunkX-4,SPACING),maxRegionX=floorDiv(chunkX+4,SPACING);
        int minRegionZ=floorDiv(chunkZ-4,SPACING),maxRegionZ=floorDiv(chunkZ+4,SPACING);
        for (int regionX=minRegionX;regionX<=maxRegionX;regionX++) {
            for (int regionZ=minRegionZ;regionZ<=maxRegionZ;regionZ++) {
                int[] candidate=candidateForRegion(world,regionX,regionZ);
                int candidateX=candidate[0],candidateZ=candidate[1];
                int originX = (candidateX << 4) + 8 - 29;
                int originZ = (candidateZ << 4) + 8 - 29;
                if (!intersectsChunk(originX, originZ, chunkX, chunkZ)) continue;
                if (!hasValidBiomes(world, candidateX, candidateZ)) continue;
                OceanMonumentData120.get(world).recordStart(candidateX,candidateZ);
                int baseY=baseY(com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120.isFutura(world));
                Builder builder = new Builder(world, originX, originZ, chunkX, chunkZ,baseY);
                builder.generate();
                spawnElders(world, builder);
            }
        }
    }

    public static int baseY(boolean futura){return MODERN_BASE_Y+(futura
            ?com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.Y_TRANSLATION:0);}

    /** Returns the actual nearest valid generated-monument centre, suitable for explorer maps. */
    public static int[] findNearest(World world, int sourceX, int sourceZ, int maxRegions) {
        int sourceChunkX = sourceX >> 4;
        int sourceChunkZ = sourceZ >> 4;
        int sourceRegionX = floorDiv(sourceChunkX, SPACING);
        int sourceRegionZ = floorDiv(sourceChunkZ, SPACING);
        for (int radius = 0; radius <= maxRegions; radius++) {
            int[] nearest=null;long nearestDistance=Long.MAX_VALUE;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius > 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) continue;
                    int[] candidate = candidateForRegion(world, sourceRegionX + dx, sourceRegionZ + dz);
                    if (!hasValidBiomes(world, candidate[0], candidate[1])) continue;
                    int x=(candidate[0]<<4)+8,z=(candidate[1]<<4)+8;
                    long ddx=(long)x-sourceX,ddz=(long)z-sourceZ,distance=ddx*ddx+ddz*ddz;
                    if(distance<nearestDistance){nearestDistance=distance;nearest=new int[]{x,z};}
                }
            }
            if(nearest!=null)return nearest;
        }
        return null;
    }

    static boolean isCandidate(World world, int chunkX, int chunkZ) {
        int regionX = floorDiv(chunkX, SPACING);
        int regionZ = floorDiv(chunkZ, SPACING);
        int[] candidate = candidateForRegion(world, regionX, regionZ);
        return chunkX == candidate[0] && chunkZ == candidate[1];
    }

    private static int[] candidateForRegion(World world, int regionX, int regionZ) {
        Random random = world.setRandomSeed(regionX, regionZ, SALT);
        int bound = SPACING - SEPARATION;
        return new int[] {
                regionX * SPACING + (random.nextInt(bound) + random.nextInt(bound)) / 2,
                regionZ * SPACING + (random.nextInt(bound) + random.nextInt(bound)) / 2
        };
    }

    private static boolean hasValidBiomes(World world, int chunkX, int chunkZ) {
        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;
        for (int dz = -16; dz <= 16; dz += 4) for (int dx = -16; dx <= 16; dx += 4)
            if (!isDeepOcean(world.getWorldChunkManager().getBiomeGenAt(x + dx, z + dz))) return false;
        for (int dz = -29; dz <= 29; dz += 4) for (int dx = -29; dx <= 29; dx += 4)
            if (!isOceanOrRiver(world.getWorldChunkManager().getBiomeGenAt(x + dx, z + dz))) return false;
        return true;
    }

    private static boolean isDeepOcean(BiomeGenBase biome) {
        return biome == BiomeGenBase.deepOcean || biome == OceanBiomeRegistry.deepWarmOcean
                || biome == OceanBiomeRegistry.deepLukewarmOcean
                || biome == OceanBiomeRegistry.deepColdOcean
                || biome == OceanBiomeRegistry.deepFrozenOcean;
    }

    private static boolean isOceanOrRiver(BiomeGenBase biome) {
        return biome == BiomeGenBase.ocean || biome == BiomeGenBase.deepOcean
                || biome == BiomeGenBase.frozenOcean || biome == BiomeGenBase.river
                || biome == BiomeGenBase.frozenRiver || biome == OceanBiomeRegistry.warmOcean
                || biome == OceanBiomeRegistry.deepWarmOcean || biome == OceanBiomeRegistry.lukewarmOcean
                || biome == OceanBiomeRegistry.deepLukewarmOcean || biome == OceanBiomeRegistry.coldOcean
                || biome == OceanBiomeRegistry.deepColdOcean || biome == OceanBiomeRegistry.deepFrozenOcean;
    }

    private static boolean intersectsChunk(int originX, int originZ, int chunkX, int chunkZ) {
        int minX = chunkX << 4;
        int minZ = chunkZ << 4;
        return minX <= originX + WIDTH - 1 && minX + 15 >= originX
                && minZ <= originZ + WIDTH - 1 && minZ + 15 >= originZ;
    }

    private static int floorDiv(int value, int divisor) {
        int result = value / divisor;
        return value < 0 && value % divisor != 0 ? result - 1 : result;
    }

    private static void spawnElders(World world, Builder builder) {
        builder.spawnElder(12, 3, 17);
        builder.spawnElder(45, 6, 17);
        builder.spawnElder(28, 18, 28);
    }

    private static final class Builder {
        private final World world;
        private final int originX;
        private final int originZ;
        private final int chunkMinX;
        private final int chunkMaxX;
        private final int chunkMinZ;
        private final int chunkMaxZ;
        private final int baseY;

        Builder(World world, int originX, int originZ, int chunkX, int chunkZ,int baseY) {
            this.world = world;
            this.originX = originX;
            this.originZ = originZ;
            this.baseY = baseY;
            chunkMinX = chunkX << 4;
            chunkMaxX = chunkMinX + 15;
            chunkMinZ = chunkZ << 4;
            chunkMaxZ = chunkMinZ + 15;
        }

        void generate() {
            // fill() includes both endpoints. Local Y=24 would create an extra
            // 58x58 source-water sheet above the ocean (Y=63, or Futura Y=127).
            fill(0, 0, 0, 57, WATER_TOP_LOCAL_Y, 57, Blocks.water, 0);
            foundations();
            exterior();
            internalRooms();
            treasureRoom();
            spongeRoom();
            lights();
            // The six-wide front entrance must remain flooded and unobstructed.
            fill(26, 1, 0, 31, 5, 5, Blocks.water, 0);
        }

        private void foundations() {
            fill(0, 0, 0, 57, 0, 57, BountifulContent.prismarine, 0);
            for (int gx = 0; gx < 7; gx++) {
                for (int gz = 0; gz < 7; gz++) {
                    if (gz == 0 && gx == 3) continue;
                    support(gx * 9, gz * 9, 4, 4);
                }
            }
        }

        private void exterior() {
            // Front wings and their four official stepped ribs.
            shell(0, 1, 0, 24, 10, 20, 1);
            shell(33, 1, 0, 57, 10, 20, 1);
            for (int i = 0; i < 4; i++) {
                fill(i, i + 1, i, i, i + 1, 20, BountifulContent.prismarine, 1);
                fill(57 - i, i + 1, i, 57 - i, i + 1, 20, BountifulContent.prismarine, 1);
                fill(i + 1, i + 1, i, 23 - i, i + 1, i, BountifulContent.prismarine, 1);
                fill(34 + i, i + 1, i, 56 - i, i + 1, i, BountifulContent.prismarine, 1);
            }

            // Rear and side terraces follow the 0/7/14/21 and 36/43/50/57 outlines of 1.12.
            shell(0, 1, 21, 6, 7, 57, 1);
            shell(51, 1, 21, 57, 7, 57, 1);
            shell(7, 5, 21, 13, 10, 54, 1);
            shell(44, 5, 21, 50, 10, 54, 1);
            shell(14, 9, 21, 20, 14, 43, 1);
            shell(37, 9, 21, 43, 14, 43, 1);
            shell(7, 5, 44, 50, 12, 57, 1);
            shell(14, 9, 37, 43, 14, 50, 1);

            // Central stepped temple and penthouse (official local bounds 21..36).
            shell(21, 1, 21, 36, 13, 43, 0);
            for (int step = 0; step < 4; step++) {
                shell(21 + step, 13 + step, 21 + step,
                        36 - step, 13 + step, 36 - step, 1);
            }
            fill(25, 16, 25, 32, 16, 32, BountifulContent.prismarine, 0);
            shell(25, 17, 25, 32, 21, 32, 1);

            // Dark-prismarine front crest from the original 1.12 facade.
            for (int i = 0; i < 7; i++) {
                set(28 - i, 6 + i, 21, BountifulContent.prismarine, 2);
                set(29 + i, 6 + i, 21, BountifulContent.prismarine, 2);
            }
            for (int i = 0; i < 4; i++) {
                set(28 - i, 9 + i, 21, BountifulContent.prismarine, 2);
                set(29 + i, 9 + i, 21, BountifulContent.prismarine, 2);
            }
        }

        private void internalRooms() {
            // A stable 5x5x3 room graph keeps the monument traversable across chunk boundaries.
            for (int level = 0; level < 3; level++) {
                int floorY = 1 + level * 4;
                fill(14, floorY, 22, 43, floorY, 43, BountifulContent.prismarine, 0);
                for (int x = 14; x <= 44; x += 6) {
                    fill(x, floorY + 1, 22, x, floorY + 3, 43, BountifulContent.prismarine, 1);
                    for (int z = 24; z <= 42; z += 6) {
                        fill(x, floorY + 1, z, x, floorY + 2, z + 1, Blocks.water, 0);
                    }
                }
                for (int z = 22; z <= 44; z += 6) {
                    fill(14, floorY + 1, z, 43, floorY + 3, z, BountifulContent.prismarine, 1);
                    for (int x = 16; x <= 42; x += 6) {
                        fill(x, floorY + 1, z, x + 1, floorY + 2, z, Blocks.water, 0);
                    }
                }
            }
            // Vertical passages connect all three levels.
            fill(27, 2, 27, 30, 12, 30, Blocks.water, 0);
        }

        private void treasureRoom() {
            shell(22, 4, 31, 31, 11, 40, 2);
            fill(25, 6, 34, 28, 9, 37, BountifulContent.prismarine, 2);
            fill(26, 7, 35, 27, 8, 36, Blocks.gold_block, 0);
            fill(26, 5, 31, 27, 7, 31, Blocks.water, 0);
        }

        private void spongeRoom() {
            shell(32, 4, 31, 41, 11, 40, 0);
            Random random = new Random(world.getSeed() ^ (long)originX * 341873128712L
                    ^ (long)originZ * 132897987541L ^ 0x53504F4E47454CL);
            for (int x = 34; x <= 39; x++) {
                for (int z = 33; z <= 38; z++) {
                    if (random.nextInt(3) == 0) {
                        int height = 6 + random.nextInt(4);
                        set(x, height, z, BountifulContent.sponge, 1);
                        if (random.nextBoolean() && height < 9) set(x, height + 1, z, BountifulContent.sponge, 1);
                    }
                }
            }
        }

        private void lights() {
            for (int z = 5; z <= 53; z += 3) {
                set(5, 5, z, BountifulContent.seaLantern, 0);
                set(52, 5, z, BountifulContent.seaLantern, 0);
            }
            for (int x = 12; x <= 45; x += 3) {
                set(x, 9, 45, BountifulContent.seaLantern, 0);
                set(x, 9, 52, BountifulContent.seaLantern, 0);
            }
            int[][] tower = {{27,20,27},{30,20,27},{27,20,30},{30,20,30}};
            for (int[] p : tower) set(p[0], p[1], p[2], BountifulContent.seaLantern, 0);
        }

        private void shell(int x1, int y1, int z1, int x2, int y2, int z2, int meta) {
            fill(x1, y1, z1, x2, y1, z2, BountifulContent.prismarine, meta);
            fill(x1, y2, z1, x2, y2, z2, BountifulContent.prismarine, meta);
            fill(x1, y1 + 1, z1, x1, y2 - 1, z2, BountifulContent.prismarine, meta);
            fill(x2, y1 + 1, z1, x2, y2 - 1, z2, BountifulContent.prismarine, meta);
            fill(x1 + 1, y1 + 1, z1, x2 - 1, y2 - 1, z1, BountifulContent.prismarine, meta);
            fill(x1 + 1, y1 + 1, z2, x2 - 1, y2 - 1, z2, BountifulContent.prismarine, meta);
        }

        private void support(int localX, int localZ, int width, int depth) {
            for (int x = localX; x < localX + width; x++) {
                for (int z = localZ; z < localZ + depth; z++) {
                    int worldX = originX + x;
                    int worldZ = originZ + z;
                    if (!insideChunk(worldX, worldZ)) continue;
                    for (int y = baseY - 1; y >= 5; y--) {
                        Block existing = world.getBlock(worldX, y, worldZ);
                        if (existing != Blocks.air && existing.getMaterial().isSolid()) break;
                        world.setBlock(worldX, y, worldZ, BountifulContent.prismarine, 1, 2);
                    }
                }
            }
        }

        private void fill(int x1, int y1, int z1, int x2, int y2, int z2, Block block, int meta) {
            int minX = Math.max(originX + x1, chunkMinX);
            int maxX = Math.min(originX + x2, chunkMaxX);
            int minZ = Math.max(originZ + z1, chunkMinZ);
            int maxZ = Math.min(originZ + z2, chunkMaxZ);
            if (minX > maxX || minZ > maxZ || y1 > y2) return;
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = baseY + y1; y <= baseY + y2; y++) {
                        world.setBlock(x, y, z, block, meta, 2);
                    }
                }
            }
        }

        private void set(int x, int y, int z, Block block, int meta) {
            int worldX = originX + x;
            int worldZ = originZ + z;
            if (insideChunk(worldX, worldZ)) world.setBlock(worldX, baseY + y, worldZ, block, meta, 2);
        }

        void spawnElder(int x, int y, int z) {
            int worldX = originX + x;
            int worldZ = originZ + z;
            if (!insideChunk(worldX, worldZ)) return;
            int slot=x*961+y*31+z;if(!OceanMonumentData120.get(world).claimMob(
                    (originX+29)>>4,(originZ+29)>>4,slot))return;
            EntityBountifulGuardian elder = new EntityBountifulGuardian(world);
            elder.setElder(true);
            elder.func_110163_bv();
            elder.setLocationAndAngles(worldX + 0.5D, baseY + y, worldZ + 0.5D, 0.0F, 0.0F);
            world.spawnEntityInWorld(elder);
        }

        private boolean insideChunk(int x, int z) {
            return x >= chunkMinX && x <= chunkMaxX && z >= chunkMinZ && z <= chunkMaxZ;
        }
    }
}
