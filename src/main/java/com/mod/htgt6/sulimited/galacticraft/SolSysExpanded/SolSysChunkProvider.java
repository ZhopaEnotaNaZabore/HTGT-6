package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.ChunkProviderSpace;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeDecoratorSpace;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

/** SolSys terrain hooks own the complete column, including bedrock and surface. */
public abstract class SolSysChunkProvider extends ChunkProviderSpace {
    private final World terrainWorld;
    private final BiomeDecoratorSpace decorator = new BiomeDecoratorSpace() {
        private World world;
        protected void decorate() { }
        protected void setCurrentWorld(World value) { world = value; }
        protected World getCurrentWorld() { return world; }
    };

    protected SolSysChunkProvider(World world, long seed, boolean features) {
        super(world, seed, features);
        terrainWorld = world;
    }

    @Override protected final BiomeDecoratorSpace getBiomeGenerator() { return decorator; }

    @Override
    public final Chunk provideChunk(int chunkX, int chunkZ) {
        Block[] blocks = new Block[65536];
        byte[] metadata = new byte[65536];
        // Do not generate Galacticraft terrain/craters just to overwrite them.
        // These providers implement all terrain features in onChunkProvide.
        onChunkProvide(chunkX, chunkZ, blocks, metadata);
        SolSysTerrainFeatures.apply(SolSysGeology.forWorld(terrainWorld.provider.getDimensionName()),
                terrainWorld.getSeed(), chunkX, chunkZ, blocks, metadata);
        BiomeGenBase[] biomes = terrainWorld.getWorldChunkManager().loadBlockGeneratorData(
                null, chunkX * 16, chunkZ * 16, 16, 16);
        Chunk chunk = new Chunk(terrainWorld, blocks, metadata, chunkX, chunkZ);
        byte[] ids = chunk.getBiomeArray();
        for (int i = 0; i < ids.length; i++) ids[i] = (byte) biomes[i].biomeID;
        chunk.generateSkylightMap();
        return chunk;
    }
}
