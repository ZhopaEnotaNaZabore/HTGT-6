package com.mod.htgt6.sulimited.worldgen.constructor;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.world.World;

/** Mutable view of one 16x16x256 chunk during SU terrain construction. */
public final class SUChunkContext {
    public final World world;
    public final Block[] blocks;
    public final byte[] metadata;
    public final int chunkX;
    public final int chunkZ;
    public final long worldSeed;

    public SUChunkContext(World world, Block[] blocks, byte[] metadata, int chunkX, int chunkZ) {
        this.world = world;
        this.blocks = blocks;
        this.metadata = metadata;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.worldSeed = world.getSeed();
    }

    public int worldX(int localX) { return chunkX * 16 + localX; }
    public int worldZ(int localZ) { return chunkZ * 16 + localZ; }
    public boolean contains(int localX, int y, int localZ) {
        return localX >= 0 && localX < 16 && localZ >= 0 && localZ < 16 && y >= 0 && y < 256;
    }
    public Block getBlock(int localX, int y, int localZ) {
        return contains(localX, y, localZ) ? blocks[index(localX, y, localZ)] : null;
    }
    public byte getMetadata(int localX, int y, int localZ) {
        return contains(localX, y, localZ) ? metadata[index(localX, y, localZ)] : 0;
    }
    public void setBlock(int localX, int y, int localZ, Block block, int meta) {
        if (!contains(localX, y, localZ)) return;
        int index = index(localX, y, localZ);
        blocks[index] = block;
        metadata[index] = (byte) meta;
    }
    public int topSolidY(int localX, int localZ) {
        for (int y = 255; y > 0; y--) if (getBlock(localX, y, localZ) != null) return y;
        return 0;
    }
    public Random random(long salt) {
        return new Random(mix(worldSeed ^ salt, chunkX, chunkZ));
    }
    public Random featureRandom(long salt, int cellX, int cellZ) {
        return new Random(mix(worldSeed ^ salt, cellX, cellZ));
    }
    private int index(int localX, int y, int localZ) { return (localX * 16 + localZ) * 256 + y; }
    private static long mix(long seed, int x, int z) {
        long value = seed + x * 341873128712L + z * 132897987541L;
        value = (value ^ value >>> 33) * 0xff51afd7ed558ccdL;
        value = (value ^ value >>> 33) * 0xc4ceb9fe1a85ec53L;
        return value ^ value >>> 33;
    }
}
