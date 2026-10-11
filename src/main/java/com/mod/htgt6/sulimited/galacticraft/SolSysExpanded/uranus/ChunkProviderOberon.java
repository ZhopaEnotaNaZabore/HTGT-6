package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.uranus;

import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import com.mod.htgt6.sulimited.worldgen.constructor.BoulderConstructor;
import com.mod.htgt6.sulimited.worldgen.constructor.CaveConstructor;
import com.mod.htgt6.sulimited.worldgen.constructor.SurfaceLayerConstructor;
import com.mod.htgt6.sulimited.worldgen.constructor.SUWorldConstructorPipeline;
import net.minecraft.world.World;

/** Contrasted dark highlands with steep crater walls, central peaks and bright ejecta. */
public final class ChunkProviderOberon extends AbstractUranusMoonChunkProvider {
    public ChunkProviderOberon(World world,long seed,boolean features){super(world,seed,features,BiomeGenSpaceUltimate.oberon,UranusMoonBlocks.oberonRegolith,UranusMoonBlocks.oberonBreccia,UranusMoonBlocks.oberonDarkRock);}
    protected Terrain sample(long seed,int x,int z){double highlands=noise(seed+211,x,z,.0028,5)*14,blocks=Math.rint(noise(seed+227,x,z,.013,2)*6)/6.0*8;Crater small=crater(seed+251,x,z,92,13,34),large=crater(seed+277,x,z,390,55,116);double peak=Math.max(0,large.depth-.72)*110;int h=76+(int)Math.round(highlands+blocks+small.rim*9+large.rim*21+peak-small.depth*18-large.depth*39);boolean ray=small.ejecta>.22||large.ejecta>.12;return new Terrain(h,ray?UranusMoonBlocks.oberonCraterIce:UranusMoonBlocks.oberonRegolith,ray?UranusMoonBlocks.oberonBreccia:UranusMoonBlocks.oberonDarkRock);}
    protected void configureConstructors(SUWorldConstructorPipeline p){p.add(new CaveConstructor(6,13,49,0x4f4245524f4eL,UranusMoonBlocks.oberonDarkRock,UranusMoonBlocks.oberonBreccia));p.add(new SurfaceLayerConstructor().add(UranusMoonBlocks.oberonRegolith,0,UranusMoonBlocks.oberonRegolith,0,1,0).add(UranusMoonBlocks.oberonBreccia,0,UranusMoonBlocks.oberonDarkRock,0,1,2));p.add(new BoulderConstructor(UranusMoonBlocks.oberonDarkRock,0,6,1,2));}
}
