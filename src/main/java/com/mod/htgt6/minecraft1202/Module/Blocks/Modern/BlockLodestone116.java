package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.LodestoneBinding116;

public final class BlockLodestone116 extends BlockBackport120EtFGapSimple {
    private static final Block.SoundType SOUNDS=new Block.SoundType("stone",1F,1F){
        @Override public String getBreakSound(){return "htgt6:block.lodestone.break";}
        @Override public String getStepResourcePath(){return "htgt6:block.lodestone.step";}
        @Override public String func_150496_b(){return "htgt6:block.lodestone.place";}
    };
    public BlockLodestone116(){super(Material.rock,"lodestone","lodestone_side","lodestone_top",3.5F,3.5F,SOUNDS,"pickaxe",0);}
    @Override public int getMobilityFlag(){return 2;}
    @Override public int getRenderType(){int id=Backport120Content.getJsonRenderId();return id>0?id:0;}
    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,int side,float hx,float hy,float hz){
        return LodestoneBinding116.bind(player,world,x,y,z,side);
    }
}
