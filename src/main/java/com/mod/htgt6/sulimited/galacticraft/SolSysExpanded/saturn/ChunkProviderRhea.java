package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import net.minecraft.world.World;

/** Densely overlapping old craters with sparse, radial fresh-impact rays. */
public final class ChunkProviderRhea extends AbstractSaturnMoonChunkProvider {
    public ChunkProviderRhea(World world,long seed,boolean features){super(world,seed,features,
            BiomeGenSpaceUltimate.rhea,SaturnMoonBlocks.rheaRegolith,SaturnMoonBlocks.rheaDenseIce,SaturnMoonBlocks.rheaRockyIce);}
    protected MoonTerrainSample sample(long seed,int x,int z){
        Crater small=crater(seed+17,x,z,70,10,29), medium=crater(seed+53,x,z,210,35,78), large=crater(seed+97,x,z,590,88,154);
        double ancient=noise(seed+5,x,z,0.0035D,3)*11.0D;
        double basin=noise(seed+211,x,z,0.00075D,2);
        double angle=Math.atan2(z,x), radius=Math.sqrt((double)x*x+(double)z*z);
        boolean ray=(Math.abs(Math.sin(angle*11.0D+radius*0.002D))<0.055D&&large.ejecta>0.03D)||large.ejecta>0.34D;
        int plateau=basin>0.28D?8:basin<-0.30D?-10:0;
        int height=72+(int)Math.round(ancient+plateau+small.rim*8.0D+medium.rim*15.0D+large.rim*24.0D-small.depth*13.0D-medium.depth*27.0D-large.depth*47.0D);
        return new MoonTerrainSample(height,ray?SaturnMoonBlocks.rheaRayEjecta:SaturnMoonBlocks.rheaRegolith,
                ray?SaturnMoonBlocks.rheaDenseIce:SaturnMoonBlocks.rheaDenseIce);
    }
}
