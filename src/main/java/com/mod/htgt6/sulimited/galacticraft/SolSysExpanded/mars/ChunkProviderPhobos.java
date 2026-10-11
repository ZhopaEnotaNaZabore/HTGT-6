package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.mars;

import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import com.mod.htgt6.sulimited.worldgen.constructor.CaveConstructor;
import com.mod.htgt6.sulimited.worldgen.constructor.SurfaceLayerConstructor;
import com.mod.htgt6.sulimited.worldgen.constructor.SUWorldConstructorPipeline;
import net.minecraft.world.World;

/** Angular ridges, parallel grooves and a dominant Stickney-class impact basin. */
public final class ChunkProviderPhobos extends AbstractMarsMoonChunkProvider {
    public ChunkProviderPhobos(World world,long seed,boolean features){super(world,seed,features,BiomeGenSpaceUltimate.phobos,MarsMoonBlocks.phobosRegolith,MarsMoonBlocks.phobosSubsoil,MarsMoonBlocks.phobosStone);}
    protected void configureConstructors(SUWorldConstructorPipeline pipeline) {
        pipeline.add(new CaveConstructor(3, 14, 52, 0x50484f424f534cL,
                MarsMoonBlocks.phobosStone, MarsMoonBlocks.phobosSubsoil));
        pipeline.add(new SurfaceLayerConstructor()
                .add(MarsMoonBlocks.phobosRegolith, 0, MarsMoonBlocks.phobosRegolith, 0, 1, 0)
                .add(MarsMoonBlocks.phobosBreccia, 0, MarsMoonBlocks.phobosSubsoil, 0, 1, 1));
    }
    protected Terrain sample(long seed,int x,int z){
        double angular=Math.rint(noise(seed+11,x,z,0.008,3)*7.0)/7.0;
        double longRidges=Math.abs(noise(seed+23,x+z*0.22,z-x*0.12,0.0032,4))*19.0;
        double grooves=Math.pow(Math.abs(Math.sin((x+z*0.37)/17.0)),18.0)*7.0;
        Crater small=crater(seed+41,x,z,95,12,34),stickney=crater(seed+83,x,z,720,95,175);
        int height=68+(int)Math.round(angular*13+longRidges+small.rim*9+stickney.rim*25-small.depth*18-stickney.depth*52-grooves);
        boolean ejecta=small.ejecta>0.22||stickney.ejecta>0.10;
        return new Terrain(height,ejecta?MarsMoonBlocks.phobosBreccia:MarsMoonBlocks.phobosRegolith,ejecta?MarsMoonBlocks.phobosBreccia:MarsMoonBlocks.phobosSubsoil);
    }
}
