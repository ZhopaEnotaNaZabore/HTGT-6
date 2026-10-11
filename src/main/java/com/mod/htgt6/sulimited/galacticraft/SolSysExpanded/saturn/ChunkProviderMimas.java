package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import net.minecraft.world.World;

/** Low rolling plains dominated by deep bowl craters and bright ejecta rims. */
public final class ChunkProviderMimas extends AbstractSaturnMoonChunkProvider {
    public ChunkProviderMimas(World world,long seed,boolean features){super(world,seed,features,
            BiomeGenSpaceUltimate.mimas,SaturnMoonBlocks.mimasRegolith,SaturnMoonBlocks.mimasFracturedIce,SaturnMoonBlocks.mimasCoreRock);}
    protected MoonTerrainSample sample(long seed,int x,int z){
        Crater small=crater(seed+11,x,z,92,14,38), herschel=crater(seed+71,x,z,720,120,205);
        double plains=noise(seed+3,x,z,0.0035D,4)*9.0D;
        int height=70+(int)Math.round(plains+small.rim*9.0D+herschel.rim*24.0D-small.depth*19.0D-herschel.depth*62.0D);
        boolean ejecta=small.ejecta>0.20D||herschel.ejecta>0.08D;
        return new MoonTerrainSample(height,ejecta?SaturnMoonBlocks.mimasEjecta:SaturnMoonBlocks.mimasRegolith,
                ejecta?SaturnMoonBlocks.mimasFracturedIce:SaturnMoonBlocks.mimasFracturedIce);
    }
}
