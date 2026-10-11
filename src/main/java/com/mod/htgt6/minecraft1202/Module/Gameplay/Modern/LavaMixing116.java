package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.IWaterloggedBlock150;
/** The LiquidBlock neighbor reaction, also used by stationary lava. */
public final class LavaMixing116 {
    private static final int[][] SIDES={{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
    public static void mix(Block lava,World w,int x,int y,int z){
        if(w.isRemote||w.getBlock(x,y,z)!=lava||lava.getMaterial()!=Material.lava)return;
        boolean soul=w.blockExists(x,y-1,z)&&w.getBlock(x,y-1,z)==EtFVanillaGapContent120.soulSoil;
        for(int[] side:SIDES){int px=x+side[0],py=y+side[1],pz=z+side[2];if(!w.blockExists(px,py,pz))continue;
            Block adjacent=w.getBlock(px,py,pz);Block result=null;
            if(adjacent.getMaterial()==Material.water||adjacent instanceof IWaterloggedBlock150&&((IWaterloggedBlock150)adjacent).isWaterlogged(w,px,py,pz))result=w.getBlockMetadata(x,y,z)==0?Blocks.obsidian:Blocks.cobblestone;
            else if(soul&&adjacent==Backport120Content.blueIce)result=EtFVanillaGapContent120.basalt;
            if(result==null)continue;
            if(w.setBlock(x,y,z,result,0,3)){
                w.playSoundEffect(x+.5,y+.5,z+.5,"htgt6:block.lava.extinguish",.5F,2.6F+(w.rand.nextFloat()-w.rand.nextFloat())*.8F);
                w.playAuxSFX(2000,x,y,z,4);
            }return;
        }
    }
    private LavaMixing116(){}
}
