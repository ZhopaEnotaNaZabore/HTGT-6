package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.*;

public final class BlockCryingObsidian116 extends BlockBackport120EtFGapSimple {
    public BlockCryingObsidian116(){super(Material.rock,"crying_obsidian","crying_obsidian",null,50,1200,Block.soundTypeStone,"pickaxe",3);setLightLevel(10F/15F);}
    @SideOnly(Side.CLIENT) @Override public void randomDisplayTick(World w,int x,int y,int z,java.util.Random r){
        if(r.nextInt(5)!=0)return;
        int side=r.nextInt(6);if(side==1)return;
        int dx=side==4?-1:side==5?1:0,dy=side==0?-1:0,dz=side==2?-1:side==3?1:0;
        if(w.getBlock(x+dx,y+dy,z+dz).isOpaqueCube())return;
        com.mod.htgt6.minecraft1202.Module.Client.Modern.CryingObsidianParticle116.spawn(w,
            x+(dx==0?r.nextDouble():.5+dx*.6),y+(dy==0?r.nextDouble():-.05),z+(dz==0?r.nextDouble():.5+dz*.6));
    }
}
