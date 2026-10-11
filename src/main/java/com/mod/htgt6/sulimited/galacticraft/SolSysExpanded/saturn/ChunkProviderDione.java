package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import net.minecraft.world.World;

/** Asymmetric ridges and bright wispy fault scarps over darker cratered ice. */
public final class ChunkProviderDione extends AbstractSaturnMoonChunkProvider {
    public ChunkProviderDione(World world,long seed,boolean features){super(world,seed,features,
            BiomeGenSpaceUltimate.dione,SaturnMoonBlocks.dioneRegolith,SaturnMoonBlocks.dioneStripedIce,SaturnMoonBlocks.dioneRockyIce);}
    protected MoonTerrainSample sample(long seed,int x,int z){
        double plain=noise(seed+7,x,z,0.0022D,4)*7.0D;
        double fold=noise(seed+47,x,z,0.006D,3);
        double faultPhase=(x*0.012D)+(z*0.0045D)+noise(seed+83,x,z,0.0013D,2)*8.0D;
        double fault=Math.abs(Math.sin(faultPhase));
        boolean wispy=fault<0.14D;
        double scarp=wispy?(0.14D-fault)*210.0D:0.0D;
        double shelf=fold>0.28D?9.0D:fold<-0.32D?-7.0D:0.0D;
        int height=73+(int)Math.round(plain+shelf+scarp);
        return new MoonTerrainSample(height,wispy?SaturnMoonBlocks.dioneWispyCliff:SaturnMoonBlocks.dioneRegolith,
                wispy?SaturnMoonBlocks.dioneStripedIce:SaturnMoonBlocks.dioneStripedIce);
    }
}
