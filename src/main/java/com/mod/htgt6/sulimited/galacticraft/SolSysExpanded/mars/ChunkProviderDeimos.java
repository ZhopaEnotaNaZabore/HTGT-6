package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.mars;

import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import com.mod.htgt6.sulimited.worldgen.constructor.BoulderConstructor;
import com.mod.htgt6.sulimited.worldgen.constructor.RavineConstructor;
import com.mod.htgt6.sulimited.worldgen.constructor.SurfaceLayerConstructor;
import com.mod.htgt6.sulimited.worldgen.constructor.SUWorldConstructorPipeline;
import net.minecraft.world.World;

/** Rounded dust-covered relief with subdued craters and sparse exposed boulder fields. */
public final class ChunkProviderDeimos extends AbstractMarsMoonChunkProvider {
    public ChunkProviderDeimos(World world,long seed,boolean features){super(world,seed,features,BiomeGenSpaceUltimate.deimos,MarsMoonBlocks.deimosRegolith,MarsMoonBlocks.deimosBreccia,MarsMoonBlocks.deimosCoreRock);}
    protected void configureConstructors(SUWorldConstructorPipeline pipeline) {
        pipeline.add(new RavineConstructor(5, 24, 53, 0x4445494d4f534cL,
                MarsMoonBlocks.deimosCoreRock, MarsMoonBlocks.deimosBreccia,
                MarsMoonBlocks.deimosFracturedRock));
        pipeline.add(new SurfaceLayerConstructor()
                .add(MarsMoonBlocks.deimosRegolith, 0, MarsMoonBlocks.deimosRegolith, 0, 1, 0)
                .add(MarsMoonBlocks.deimosFracturedRock, 0, MarsMoonBlocks.deimosBreccia, 0, 1, 2));
        pipeline.add(new BoulderConstructor(MarsMoonBlocks.deimosFracturedRock, 0, 4, 1, 2));
    }
    protected Terrain sample(long seed,int x,int z){
        double broad=noise(seed+101,x,z,0.0022,5)*12.0;
        double rounded=noise(seed+131,x,z,0.009,3)*4.0;
        Crater shallow=crater(seed+173,x,z,180,18,48);
        int height=72+(int)Math.round(broad+rounded+shallow.rim*3.5-shallow.depth*7.0);
        boolean exposed=Math.abs(rounded)>3.0||shallow.ejecta>0.36;
        return new Terrain(height,exposed?MarsMoonBlocks.deimosFracturedRock:MarsMoonBlocks.deimosRegolith,MarsMoonBlocks.deimosBreccia);
    }
}
