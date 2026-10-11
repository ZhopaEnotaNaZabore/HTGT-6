package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.uranus;

import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import com.mod.htgt6.sulimited.worldgen.constructor.CaveConstructor;
import com.mod.htgt6.sulimited.worldgen.constructor.SurfaceLayerConstructor;
import com.mod.htgt6.sulimited.worldgen.constructor.SUWorldConstructorPipeline;
import net.minecraft.world.World;

/** Ancient dark highlands, overlapping basins and rare bright crater deposits. */
public final class ChunkProviderUmbriel extends AbstractUranusMoonChunkProvider {
    public ChunkProviderUmbriel(World world,long seed,boolean features){super(world,seed,features,BiomeGenSpaceUltimate.umbriel,UranusMoonBlocks.umbrielRegolith,UranusMoonBlocks.umbrielBreccia,UranusMoonBlocks.umbrielCoreRock);}
    protected Terrain sample(long seed,int x,int z){double ancient=noise(seed+11,x,z,.0035,5)*11,rough=Math.abs(noise(seed+29,x,z,.018,3))*7;Crater small=crater(seed+47,x,z,76,10,28),large=crater(seed+71,x,z,310,48,102);double relief=ancient+rough+small.rim*7+large.rim*15-small.depth*15-large.depth*31;int h=70+(int)Math.round(relief);boolean bright=large.ejecta>.25&&noise(seed+97,x,z,.035,2)>.12;return new Terrain(h,bright?UranusMoonBlocks.umbrielFracturedIce:UranusMoonBlocks.umbrielRegolith,small.ejecta>.2?UranusMoonBlocks.umbrielBreccia:UranusMoonBlocks.umbrielCoreRock);}
    protected void configureConstructors(SUWorldConstructorPipeline p){p.add(new CaveConstructor(5,14,46,0x554d425249454cL,UranusMoonBlocks.umbrielCoreRock,UranusMoonBlocks.umbrielBreccia));p.add(new SurfaceLayerConstructor().add(UranusMoonBlocks.umbrielRegolith,0,UranusMoonBlocks.umbrielRegolith,0,1,0).add(UranusMoonBlocks.umbrielBreccia,0,UranusMoonBlocks.umbrielCoreRock,0,1,2));}
}
