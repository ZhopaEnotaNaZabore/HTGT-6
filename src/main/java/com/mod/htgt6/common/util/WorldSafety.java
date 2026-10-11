package com.mod.htgt6.common.util;

import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.World;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

/** Non-loading world reads for generators and other re-entrant-sensitive code. */
public final class WorldSafety {
    private static final Field SERVER_LOADED_CHUNKS = ReflectionHelper.findField(
            ChunkProviderServer.class, "loadedChunkHashMap", "field_73244_f");

    private WorldSafety() { }

    /**
     * Returns a block only from a chunk which is already resident in memory.
     * Unlike World.blockExists followed by World.getBlock, this never calls
     * provideChunk for a server chunk and therefore cannot recursively start
     * population from inside BiomeDecorator.
     */
    public static Block getSafeBlock(World world, int x, int y, int z) {
        if (world == null || y < 0 || y >= world.getActualHeight()) return Blocks.air;
        Chunk chunk = getLoadedChunk(world, x >> 4, z >> 4);
        if (chunk == null) return Blocks.air;
        try {
            Block block = chunk.getBlock(x & 15, y, z & 15);
            return block == null ? Blocks.air : block;
        } catch (RuntimeException exception) {
            log("read", world, x, y, z, exception);
            return Blocks.air;
        }
    }

    /** Returns the resident chunk without generating, loading, or populating it. */
    public static Chunk getLoadedChunk(World world, int chunkX, int chunkZ) {
        if (world == null) return null;
        try {
            IChunkProvider provider = world.getChunkProvider();
            if (provider instanceof ChunkProviderServer) {
                LongHashMap loaded = (LongHashMap) SERVER_LOADED_CHUNKS.get(provider);
                return (Chunk) loaded.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ));
            }
            return provider.chunkExists(chunkX, chunkZ) ? provider.provideChunk(chunkX, chunkZ) : null;
        } catch (ReflectiveOperationException exception) {
            System.err.println("[HTGT6/WorldSafety] Cannot inspect loaded chunks: " + exception);
            return null;
        } catch (RuntimeException exception) {
            System.err.println("[HTGT6/WorldSafety] Cannot access loaded chunk " + chunkX + "," + chunkZ
                    + " in dimension " + world.provider.dimensionId + ": " + exception);
            return null;
        }
    }

    public static boolean isChunkLoaded(World world, int chunkX, int chunkZ) {
        return getLoadedChunk(world, chunkX, chunkZ) != null;
    }

    /**
     * A structure may write into the chunk currently being decorated, or into
     * a resident neighbour only after that neighbour's terrain population has
     * finished. Merely loaded chunks can still be overwritten by population.
     */
    public static boolean isChunkTerrainReady(World world, int chunkX, int chunkZ,
                                               int activeChunkX, int activeChunkZ) {
        Chunk chunk = getLoadedChunk(world, chunkX, chunkZ);
        return chunk != null && (chunkX == activeChunkX && chunkZ == activeChunkZ
                || chunk.isTerrainPopulated);
    }

    /** Highest non-air block in a resident column, or -1 when unavailable. */
    public static int getSafeTopBlock(World world, int x, int z) {
        Chunk chunk = getLoadedChunk(world, x >> 4, z >> 4);
        if (chunk == null) return -1;
        int top = Math.min(world.getActualHeight() - 1, chunk.getHeightValue(x & 15, z & 15));
        for (int y = top; y >= 0; y--) {
            if (getSafeBlock(world, x, y, z) != Blocks.air) return y;
        }
        return -1;
    }

    /** Replaces a block only when its chunk is already resident. */
    public static boolean forceClearBlock(World world, int x, int y, int z) {
        Chunk chunk = getLoadedChunk(world, x >> 4, z >> 4);
        if (chunk == null || y < 0 || y >= world.getActualHeight()) return false;
        try {
            return chunk.func_150807_a(x & 15, y, z & 15, Blocks.air, 0);
        } catch (RuntimeException exception) {
            log("clear", world, x, y, z, exception);
            return false;
        }
    }

    private static void log(String action, World world, int x, int y, int z, RuntimeException exception) {
        System.err.println("[HTGT6/WorldSafety] Failed to " + action + " block at " + x + "," + y + "," + z
                + " in dimension " + world.provider.dimensionId + ": " + exception);
    }
}
