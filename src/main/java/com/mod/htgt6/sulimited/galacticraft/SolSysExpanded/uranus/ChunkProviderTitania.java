package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.uranus;

import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import com.mod.htgt6.sulimited.worldgen.constructor.RavineConstructor;
import com.mod.htgt6.sulimited.worldgen.constructor.SurfaceLayerConstructor;
import com.mod.htgt6.sulimited.worldgen.constructor.SUWorldConstructorPipeline;
import net.minecraft.world.World;

/** Broad plains split by fault scarps, troughs and long icy chasm systems. */
public final class ChunkProviderTitania extends AbstractUranusMoonChunkProvider {
    public ChunkProviderTitania(World world,long seed,boolean features){super(world,seed,features,BiomeGenSpaceUltimate.titania,UranusMoonBlocks.titaniaRegolith,UranusMoonBlocks.titaniaRockyIce,UranusMoonBlocks.titaniaCoreRock);}
    protected Terrain sample(long seed,int x,int z){double plains=noise(seed+103,x,z,.0022,5)*8,ridges=Math.abs(noise(seed+127,x,z,.007,3))*10;double fault=Math.pow(Math.abs(Math.sin((x*.72+z)/46.0+noise(seed+149,x,z,.0015,2)*4)),22);double cross=Math.pow(Math.abs(Math.sin((z*.84-x*.21)/83.0)),28);Crater crater=crater(seed+173,x,z,250,35,74);int h=74+(int)Math.round(plains+ridges+crater.rim*7-crater.depth*13-fault*18-cross*10);boolean chasm=fault>.42||cross>.52;return new Terrain(h,chasm?UranusMoonBlocks.titaniaFaultIce:UranusMoonBlocks.titaniaRegolith,chasm?UranusMoonBlocks.titaniaFaultIce:UranusMoonBlocks.titaniaRockyIce);}
    protected void configureConstructors(SUWorldConstructorPipeline p){p.add(new RavineConstructor(4,25,59,0x544954414e4941L,UranusMoonBlocks.titaniaCoreRock,UranusMoonBlocks.titaniaRockyIce,UranusMoonBlocks.titaniaFaultIce));p.add(new SurfaceLayerConstructor().add(UranusMoonBlocks.titaniaRegolith,0,UranusMoonBlocks.titaniaRegolith,0,1,0).add(UranusMoonBlocks.titaniaFaultIce,0,UranusMoonBlocks.titaniaRockyIce,0,1,1));}
}
