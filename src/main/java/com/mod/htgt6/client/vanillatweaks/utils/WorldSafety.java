package com.mod.htgt6.client.vanillatweaks.utils;


import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class WorldSafety {

    /**
     * Safely gets a block, returning AIR if the chunk is unloaded or corrupt.
     */
    public static Block getSafeBlock(World world, int x, int y, int z) {
        try {
            // Ensure Y is within world bounds
            if (y < 0 || y >= 256) return Blocks.air;

            // Check if chunk is actually loaded in memory
            if (!world.blockExists(x, y, z)) {
                return Blocks.air;
            }

            Block block = world.getBlock(x, y, z);
            return block != null ? block : Blocks.air;

        } catch (Exception e) {
            // Log the error to console instead of crashing the game
            System.err.println("[Safety] Caught generation error at " + x + ", " + y + ", " + z + ": " + e.getMessage());
            return Blocks.air;
        }
    }

    /**
     * Force-removes a corrupt block by replacing it with AIR.
     */
    public static void forceClearBlock(World world, int x, int y, int z) {
        try {
            world.setBlock(x, y, z, Blocks.air, 0, 2);
        } catch (Exception e) {
            System.err.println("[Safety] Could not clear corrupt block at " + x + ", " + y + ", " + z);
        }
    }
}