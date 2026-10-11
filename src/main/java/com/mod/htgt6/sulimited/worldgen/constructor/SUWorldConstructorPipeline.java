package com.mod.htgt6.sulimited.worldgen.constructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * Per-provider stage pipeline inspired by GalaxySpace WorldEngine.
 * It deliberately owns no static world/provider state.
 */
public final class SUWorldConstructorPipeline {
    private final List<SUChunkConstructor> chunks = new ArrayList<SUChunkConstructor>();
    private final List<SUColumnConstructor> columns = new ArrayList<SUColumnConstructor>();
    private final List<SUBlockConstructor> blocks = new ArrayList<SUBlockConstructor>();
    private final List<SUPopulationConstructor> population = new ArrayList<SUPopulationConstructor>();

    public SUWorldConstructorPipeline add(SUChunkConstructor constructor) { chunks.add(constructor); return this; }
    public SUWorldConstructorPipeline add(SUColumnConstructor constructor) { columns.add(constructor); return this; }
    public SUWorldConstructorPipeline add(SUBlockConstructor constructor) { blocks.add(constructor); return this; }
    public SUWorldConstructorPipeline add(SUPopulationConstructor constructor) { population.add(constructor); return this; }

    public void construct(SUChunkContext context) {
        for (SUChunkConstructor constructor : chunks) constructor.generate(context);
        for (SUColumnConstructor constructor : columns) {
            for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) constructor.generate(context, x, z);
        }
        // Unlike WorldEngine, the XYZ hook skips air to avoid 65,536 calls when it is not needed.
        for (SUBlockConstructor constructor : blocks) {
            for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
                int top = context.topSolidY(x, z);
                for (int y = 1; y <= top; y++) if (context.getBlock(x, y, z) != null) constructor.generate(context, x, y, z);
            }
        }
    }

    public void populate(World world, int chunkX, int chunkZ, IChunkProvider provider) {
        Random random = new Random(world.getSeed() ^ chunkX * 341873128712L ^ chunkZ * 132897987541L);
        for (SUPopulationConstructor constructor : population) constructor.populate(world, random, chunkX, chunkZ, provider);
    }
}
