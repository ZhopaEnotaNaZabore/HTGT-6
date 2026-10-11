package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import com.mod.htgt6.sulimited.world.BiomeGenSpaceUltimate;
import net.minecraft.world.World;

/** Brilliant ice plains cut by a long branching Ithaca-like chasm network. */
public final class ChunkProviderTethys extends AbstractSaturnMoonChunkProvider {
    public ChunkProviderTethys(World world,long seed,boolean features){super(world,seed,features,
            BiomeGenSpaceUltimate.tethys,SaturnMoonBlocks.tethysFrost,SaturnMoonBlocks.tethysPorousIce,SaturnMoonBlocks.tethysAncientIce);}
    protected MoonTerrainSample sample(long seed,int x,int z){
        double plate=noise(seed+19,x,z,0.0015D,3)*10.0D;
        double terraces=Math.rint(plate/2.5D)*2.5D;
        double warp=noise(seed+29,x,z,0.0007D,3)*170.0D;
        double primary=Math.abs(Math.sin((x+warp)*0.0085D));
        double branch=Math.abs(Math.sin((z-x*0.31D+warp*0.55D)*0.014D));
        double chasm=Math.min(primary,branch);
        boolean trench=chasm<0.085D;
        int height=78+(int)Math.round(terraces-(trench?(0.085D-chasm)*520.0D:0.0D));
        return new MoonTerrainSample(height,trench?SaturnMoonBlocks.tethysChasmIce:SaturnMoonBlocks.tethysFrost,
                trench?SaturnMoonBlocks.tethysChasmIce:SaturnMoonBlocks.tethysPorousIce);
    }
}
