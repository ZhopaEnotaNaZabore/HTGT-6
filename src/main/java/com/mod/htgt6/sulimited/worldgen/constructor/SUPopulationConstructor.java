package com.mod.htgt6.sulimited.worldgen.constructor;

import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/** Runs during populate, after the chunk is already part of the world. */
public interface SUPopulationConstructor {
    void populate(World world, Random random, int chunkX, int chunkZ, IChunkProvider provider);
}
