package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.neptune;

import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import java.util.Collections;
import java.util.List;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysChunkProvider;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.MapGenBaseMeta;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;

/** A pressure-layer world: liquid atmosphere to Y=159 and a fifteen-block core. */
public final class ChunkProviderNeptune extends SolSysChunkProvider {
    public ChunkProviderNeptune(World world,long seed,boolean features){super(world,seed,features);}
    protected BiomeGenBase.SpawnListEntry[] getMonsters(){return new BiomeGenBase.SpawnListEntry[0];}
    protected BiomeGenBase.SpawnListEntry[] getCreatures(){return new BiomeGenBase.SpawnListEntry[0];}
    protected List<MapGenBaseMeta> getWorldGenerators(){return Collections.emptyList();}
    protected BiomeGenBase[] getBiomesForGeneration(){return new BiomeGenBase[]{BiomeGenSpaceUltimate.neptune};}
    protected int getSeaLevel(){return 160;}
    protected BlockMetaPair getGrassBlock(){return new BlockMetaPair(NeptuneContent.core,(byte)0);}
    protected BlockMetaPair getDirtBlock(){return new BlockMetaPair(NeptuneContent.core,(byte)0);}
    protected BlockMetaPair getStoneBlock(){return new BlockMetaPair(NeptuneContent.core,(byte)0);}
    public void onChunkProvide(int chunkX,int chunkZ,Block[] blocks,byte[] meta){
        for(int x=0;x<16;x++)for(int z=0;z<16;z++){
            int base=(x*16+z)*256;
            // ChunkProviderSpace has already produced its normal terrain before
            // this hook. Neptune has no continents, so discard it completely.
            for(int y=0;y<256;y++){blocks[base+y]=null;meta[base+y]=0;}
            for(int y=0;y<15;y++)blocks[base+y]=y<5?NeptuneContent.core:y<10?NeptuneContent.mantle:NeptuneContent.pressureIce;
            for(int y=15;y<160;y++)blocks[base+y]=NeptuneContent.liquefiedAtmosphericGasBlock;
        }
    }
    public void onPopulate(IChunkProvider provider,int x,int z){ }
    public double getHeightModifier(){return 0;} public double getSmallFeatureHeightModifier(){return 0;}
    public double getMountainHeightModifier(){return 0;} public double getValleyHeightModifier(){return 0;}
    public int getCraterProbability(){return 0;}
}
