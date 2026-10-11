package com.mod.htgt6.sulimited.world;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.ChunkProviderSpace;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.MapGenBaseMeta;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
public abstract class GlieseChunkProvider extends ChunkProviderSpace {
    private final World terrainWorld;
    protected GlieseChunkProvider(World world,long seed,boolean features) { super(world,seed,features); terrainWorld=world; }
    public final Chunk provideChunk(int x,int z) {
        Block[] blocks=new Block[65536]; byte[] meta=new byte[65536];
        onChunkProvide(x,z,blocks,meta);
        for(MapGenBaseMeta generator:getWorldGenerators()) generator.generate(this,terrainWorld,x,z,blocks,meta);
        decorateTerrain(x,z,blocks,meta);
        Chunk chunk=new Chunk(terrainWorld,blocks,meta,x,z);
        BiomeGenBase[] biomes=terrainWorld.getWorldChunkManager().loadBlockGeneratorData(null,x*16,z*16,16,16);
        byte[] ids=chunk.getBiomeArray(); for(int i=0;i<ids.length;i++) ids[i]=(byte)biomes[i].biomeID;
        chunk.generateSkylightMap(); return chunk;
    }
    protected abstract void decorateTerrain(int x,int z,Block[] blocks,byte[] meta);

    @Override
    public final void decoratePlanet(World world,java.util.Random ignored,int blockX,int blockZ) {
        // Terrain decoration already ran in the owned chunk arrays. Only the
        // compatibility events belong here. GT6's Post listener may populate a
        // neighbour recursively, so neither decorator fields nor provider.rand
        // may be shared with that nested invocation.
        java.util.Random seedRandom=new java.util.Random(world.getSeed());
        long a=seedRandom.nextLong()/2L*2L+1L, b=seedRandom.nextLong()/2L*2L+1L;
        java.util.Random local=new java.util.Random(((blockX>>4)*a+(blockZ>>4)*b)^world.getSeed());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new micdoodle8.mods.galacticraft.api.event.wgen.GCCoreEventPopulate.Pre(world,local,blockX,blockZ));
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
                new micdoodle8.mods.galacticraft.api.event.wgen.GCCoreEventPopulate.Post(world,local,blockX,blockZ));
    }

    @Override
    public void populate(net.minecraft.world.chunk.IChunkProvider provider,int x,int z) {
        boolean previous=net.minecraft.block.BlockFalling.fallInstantly;
        try { super.populate(provider,x,z); }
        finally { net.minecraft.block.BlockFalling.fallInstantly=previous; }
    }
}
