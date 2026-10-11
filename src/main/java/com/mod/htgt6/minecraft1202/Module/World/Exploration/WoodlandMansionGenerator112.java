package com.mod.htgt6.minecraft1202.Module.World.Exploration;

import com.mod.htgt6.minecraft1202.Module.Data.Exploration.WoodlandMansionData112;
import com.mod.htgt6.minecraft1202.Module.Data.Exploration.WoodlandMansionTemplateRuntime112;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.WoodlandMansionLayout112;

import com.mod.htgt6.common.util.StructurePlacement120;
import com.mod.htgt6.common.util.WorldSafety;
import cpw.mods.fml.common.IWorldGenerator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

/** Exact 1.12 layout/template placement on the 1.20.2 mansion spread contract. */
public final class WoodlandMansionGenerator112 implements IWorldGenerator {
    private static final int REGION = 80, REGION_REPLAY_RADIUS = 1, REPLAY_CACHE_SIZE = 96;
    private static final Map<ReplayKey, Replay> REPLAY_CACHE =
            new LinkedHashMap<ReplayKey, Replay>(REPLAY_CACHE_SIZE + 1, .75F, true) {
                @Override protected boolean removeEldestEntry(Map.Entry<ReplayKey, Replay> entry) {
                    return size() > REPLAY_CACHE_SIZE;
                }
            };

    public static void validateTemplates() {
        for (String name : WoodlandMansionTemplateRuntime112.TEMPLATE_NAMES)
            WoodlandMansionTemplateRuntime112.load(name);
    }

    @Override public void generate(Random ignored, int chunkX, int chunkZ, World world,
            IChunkProvider generator, IChunkProvider provider) {
        if (world.isRemote || world.provider.dimensionId != 0
                || !world.getWorldInfo().isMapFeaturesEnabled()) return;
        processChunk(world, chunkX, chunkZ);
    }

    /** Catch-up for a previously populated chunk after reload; never loads a neighbour. */
    public static void catchUpLoadedChunk(World world, Chunk chunk) {
        if (world == null || chunk == null || world.isRemote || !chunk.isTerrainPopulated
                || world.provider.dimensionId != 0 || !world.getWorldInfo().isMapFeaturesEnabled()) return;
        processChunk(world, chunk.xPosition, chunk.zPosition);
    }

    private static void processChunk(World world, int targetX, int targetZ) {
        WoodlandMansionData112 data = WoodlandMansionData112.get(world);
        int regionX = StructurePlacement120.floorDiv(targetX, REGION);
        int regionZ = StructurePlacement120.floorDiv(targetZ, REGION);
        for (int dx = -REGION_REPLAY_RADIUS; dx <= REGION_REPLAY_RADIUS; ++dx)
            for (int dz = -REGION_REPLAY_RADIUS; dz <= REGION_REPLAY_RADIUS; ++dz) {
                int rx = regionX + dx, rz = regionZ + dz;
                int[] candidate = candidate(world.getSeed(), rx, rz);
                Replay replay = replay(world.getSeed(), rx, rz, candidate[0], candidate[1]);
                if (!replay.intersects(targetX, targetZ)) continue;
                WoodlandMansionData112.Start start = data.getStart(candidate[0], candidate[1]);
                if (start == null || !start.resolved) {
                    data.addPending(candidate[0], candidate[1], targetX, targetZ);
                    if (candidate[0] != targetX || candidate[1] != targetZ) continue;
                    start = resolveStart(world, data, candidate[0], candidate[1]);
                    if (!start.valid) continue;
                    place(world, data, start, replay, targetX, targetZ);
                    replayPending(world, data, start, replay);
                } else if (start.valid) place(world, data, start, replay, targetX, targetZ);
            }
    }

    private static WoodlandMansionData112.Start resolveStart(World world, WoodlandMansionData112 data,
            int chunkX, int chunkZ) {
        Chunk chunk = WorldSafety.getLoadedChunk(world, chunkX, chunkZ);
        if (chunk == null) return data.resolve(chunkX, chunkZ, 0, false);
        int centerX = (chunkX << 4) + 8, centerZ = (chunkZ << 4) + 8;
        if (!isMansionBiome(world.getBiomeGenForCoords(centerX, centerZ)))
            return data.resolve(chunkX, chunkZ, 0, false);
        int min = Integer.MAX_VALUE;
        int[] offsets = {-5, 5};
        for (int ox : offsets) for (int oz : offsets)
            min = Math.min(min, chunk.getHeightValue((8 + ox) & 15, (8 + oz) & 15));
        return data.resolve(chunkX, chunkZ, min + 1, validSurfaceHeight(min,
                com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120.isExtended(world)));
    }

    public static boolean validSurfaceHeight(int y, boolean futura) {
        return y >= 60 + (futura ? 64 : 0);
    }

    private static void replayPending(World world, WoodlandMansionData112 data,
            WoodlandMansionData112.Start start, Replay replay) {
        for (Long key : data.pending(start)) {
            int chunkX = WoodlandMansionData112.keyX(key), chunkZ = WoodlandMansionData112.keyZ(key);
            Chunk loaded = WorldSafety.getLoadedChunk(world, chunkX, chunkZ);
            if (loaded != null && loaded.isTerrainPopulated) place(world, data, start, replay, chunkX, chunkZ);
        }
    }

    private static void place(World world, WoodlandMansionData112 data, WoodlandMansionData112.Start start,
            Replay replay, int chunkX, int chunkZ) {
        if (data.isComplete(start, chunkX, chunkZ)) return;
        int originX = (start.chunkX << 4) + 8, originZ = (start.chunkZ << 4) + 8;
        for (int i = 0; i < replay.pieces.size(); ++i) {
            WoodlandMansionLayout112.Piece piece = replay.pieces.get(i);
            WoodlandMansionTemplateRuntime112.Template template = WoodlandMansionTemplateRuntime112.load(piece.name);
            if (!WoodlandMansionTemplateRuntime112.intersectsChunk(template, piece, originX, originZ, chunkX, chunkZ)) continue;
            Random markerRandom = new Random(mix(world.getSeed(), start.chunkX, start.chunkZ, i));
            WoodlandMansionTemplateRuntime112.placeInChunk(world, template, piece,
                    originX, start.baseY, originZ, chunkX, chunkZ, markerRandom);
        }
        fillFoundations(world, replay.pieces, originX, start.baseY, originZ, chunkX, chunkZ);
        data.complete(start, chunkX, chunkZ);
    }

    /** Vanilla afterPlace: support non-air floor cells inside the union of piece boxes. */
    public static void fillFoundations(World world, List<WoodlandMansionLayout112.Piece> pieces,
            int originX, int originY, int originZ, int chunkX, int chunkZ) {
        List<int[]> boxes = new ArrayList<int[]>();
        int floor = Integer.MAX_VALUE;
        for (WoodlandMansionLayout112.Piece piece : pieces) {
            WoodlandMansionTemplateRuntime112.Template template = WoodlandMansionTemplateRuntime112.load(piece.name);
            int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            for (int x : new int[]{0, template.sizeX - 1}) for (int z : new int[]{0, template.sizeZ - 1}) {
                WoodlandMansionTemplateRuntime112.Point p = WoodlandMansionTemplateRuntime112.transform(
                        x, 0, z, piece.mirror, piece.rotation);
                minX = Math.min(minX, originX + piece.x + p.x); maxX = Math.max(maxX, originX + piece.x + p.x);
                minZ = Math.min(minZ, originZ + piece.z + p.z); maxZ = Math.max(maxZ, originZ + piece.z + p.z);
            }
            int minY = originY + piece.y;
            floor = Math.min(floor, minY);
            boxes.add(new int[]{minX, minY, minZ, maxX, minY + template.sizeY - 1, maxZ});
        }
        if (floor <= 0 || floor >= world.getActualHeight()) return;
        for (int x = chunkX << 4; x < (chunkX << 4) + 16; x++)
            for (int z = chunkZ << 4; z < (chunkZ << 4) + 16; z++) {
                if (world.isAirBlock(x, floor, z)) continue;
                boolean inside = false;
                for (int[] box : boxes) if (x >= box[0] && x <= box[3] && floor >= box[1]
                        && floor <= box[4] && z >= box[2] && z <= box[5]) { inside = true; break; }
                if (!inside) continue;
                for (int y = floor - 1; y > 0; y--) {
                    if (!world.isAirBlock(x, y, z) && !world.getBlock(x, y, z).getMaterial().isLiquid()) break;
                    world.setBlock(x, y, z, net.minecraft.init.Blocks.cobblestone, 0, 2);
                }
            }
    }

    private static synchronized Replay replay(long seed, int regionX, int regionZ, int startX, int startZ) {
        ReplayKey key = new ReplayKey(seed, regionX, regionZ);
        Replay value = REPLAY_CACHE.get(key);
        if (value != null) return value;
        Random random = StructurePlacement120.randomAfterCandidate(seed, regionX, regionZ,
                StructurePlacement120.WOODLAND_MANSION);
        WoodlandMansionLayout112.Rotation[] rotations = WoodlandMansionLayout112.Rotation.values();
        WoodlandMansionLayout112.Rotation rotation = rotations[random.nextInt(rotations.length)];
        value = new Replay(startX, startZ, WoodlandMansionLayout112.generate(random, rotation));
        REPLAY_CACHE.put(key, value);
        return value;
    }

    public static int[] findNearest(World world, int sourceX, int sourceZ, int radiusRegions) {
        int baseX = StructurePlacement120.floorDiv(sourceX >> 4, REGION);
        int baseZ = StructurePlacement120.floorDiv(sourceZ >> 4, REGION);
        for (int radius = 0; radius <= radiusRegions; ++radius)
            for (int dx = -radius; dx <= radius; ++dx) for (int dz = -radius; dz <= radius; ++dz) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                int[] point = candidate(world.getSeed(), baseX + dx, baseZ + dz);
                int x = (point[0] << 4) + 8, z = (point[1] << 4) + 8;
                if (isMansionBiome(world.getBiomeGenForCoords(x, z))) return new int[]{x, z};
            }
        return null;
    }

    public static List<String> replayFingerprint(long seed, int regionX, int regionZ) {
        int[] point = candidate(seed, regionX, regionZ);
        Replay replay = replay(seed, regionX, regionZ, point[0], point[1]);
        List<String> result = new ArrayList<String>(replay.pieces.size());
        for (WoodlandMansionLayout112.Piece piece : replay.pieces) result.add(piece.toString());
        return result;
    }

    public static List<Long> replayChunks(long seed, int regionX, int regionZ) {
        int[] point = candidate(seed, regionX, regionZ);
        Replay replay = replay(seed, regionX, regionZ, point[0], point[1]);
        Set<Long> result = new TreeSet<Long>();
        for (WoodlandMansionLayout112.Piece piece : replay.pieces) {
            WoodlandMansionTemplateRuntime112.Template template = WoodlandMansionTemplateRuntime112.load(piece.name);
            int[] xs = {0, template.sizeX - 1}, zs = {0, template.sizeZ - 1};
            int minX=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE,minZ=Integer.MAX_VALUE,maxZ=Integer.MIN_VALUE;
            for(int x:xs)for(int z:zs){WoodlandMansionTemplateRuntime112.Point p=
                    WoodlandMansionTemplateRuntime112.transform(x,0,z,piece.mirror,piece.rotation);
                int wx=replay.originX+piece.x+p.x,wz=replay.originZ+piece.z+p.z;
                minX=Math.min(minX,wx);maxX=Math.max(maxX,wx);minZ=Math.min(minZ,wz);maxZ=Math.max(maxZ,wz);}
            for(int cx=StructurePlacement120.floorDiv(minX,16);cx<=StructurePlacement120.floorDiv(maxX,16);cx++)
                for(int cz=StructurePlacement120.floorDiv(minZ,16);cz<=StructurePlacement120.floorDiv(maxZ,16);cz++)
                    if(WoodlandMansionTemplateRuntime112.intersectsChunk(template,piece,replay.originX,replay.originZ,cx,cz))
                        result.add(WoodlandMansionData112.key(cx,cz));
        }
        return new ArrayList<Long>(result);
    }

    private static int[] candidate(long seed, int regionX, int regionZ) {
        return StructurePlacement120.candidate(seed, regionX, regionZ, StructurePlacement120.WOODLAND_MANSION);
    }
    /** Vanilla 1.12 accepts both Roofed Forest variants; Futura maps Dark Forest to ID 29. */
    public static boolean isMansionBiome(BiomeGenBase biome) {
        return biome != null && (biome == BiomeGenBase.roofedForest || biome.biomeID == 157);
    }
    private static long mix(long seed, int x, int z, int piece) {
        long value = seed ^ (long)x * 341873128712L ^ (long)z * 132897987541L
                ^ (long)piece * 0x9e3779b97f4a7c15L;
        value ^= value >>> 30; value *= 0xbf58476d1ce4e5b9L;
        value ^= value >>> 27; value *= 0x94d049bb133111ebL;
        return value ^ value >>> 31;
    }

    private static final class Replay {
        final int originX, originZ; final List<WoodlandMansionLayout112.Piece> pieces;
        Replay(int startX, int startZ, List<WoodlandMansionLayout112.Piece> pieces) {
            originX = (startX << 4) + 8; originZ = (startZ << 4) + 8; this.pieces = pieces;
        }
        boolean intersects(int chunkX, int chunkZ) {
            for (WoodlandMansionLayout112.Piece piece : pieces)
                if (WoodlandMansionTemplateRuntime112.intersectsChunk(
                        WoodlandMansionTemplateRuntime112.load(piece.name), piece,
                        originX, originZ, chunkX, chunkZ)) return true;
            return false;
        }
    }
    private static final class ReplayKey {
        final long seed; final int x,z;
        ReplayKey(long seed,int x,int z){this.seed=seed;this.x=x;this.z=z;}
        @Override public int hashCode(){long h=seed^(seed>>>32);return (int)h*31*31+x*31+z;}
        @Override public boolean equals(Object o){if(!(o instanceof ReplayKey))return false;ReplayKey k=(ReplayKey)o;return seed==k.seed&&x==k.x&&z==k.z;}
    }
}
