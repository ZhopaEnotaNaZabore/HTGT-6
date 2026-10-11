package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import net.minecraft.world.World;

/** Two-tone terrain dominated by long equatorial ridge chains. */
public final class ChunkProviderIapetus extends AbstractSaturnMoonChunkProvider {
    public ChunkProviderIapetus(World world,long seed,boolean features){super(world,seed,features,
            BiomeGenSpaceUltimate.iapetus,SaturnMoonBlocks.iapetusRegolith,
            SaturnMoonBlocks.iapetusBrightIce,SaturnMoonBlocks.iapetusDarkRock);}

    protected MoonTerrainSample sample(long seed,int x,int z){
        double continental=noise(seed+401,x,z,0.00055D,4);
        double detail=noise(seed+419,x,z,0.0032D,3)*9.0D;
        double warp=noise(seed+433,x,z,0.0008D,2)*210.0D;
        double ridgeDistance=Math.abs(Math.sin((z+warp)*Math.PI/1536.0D));
        double ridge=ridgeDistance<0.13D?(0.13D-ridgeDistance)*355.0D:0.0D;
        Crater sparse=crater(seed+449,x,z,310,38,104);
        boolean dark=continental+Math.sin(x*0.00075D)>0.20D;
        int height=70+(int)Math.round(detail+ridge+sparse.rim*12.0D-sparse.depth*25.0D);
        return new MoonTerrainSample(height,ridge>8.0D?SaturnMoonBlocks.iapetusRidgeRock:
                dark?SaturnMoonBlocks.iapetusDarkRock:SaturnMoonBlocks.iapetusRegolith,
                dark?SaturnMoonBlocks.iapetusDarkRock:SaturnMoonBlocks.iapetusBrightIce);
    }
}
