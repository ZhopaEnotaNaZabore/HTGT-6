package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import java.util.Random;

/** 1.20.2 target pulse, entered only through the projectile's resolved impact. */
public final class BlockBackport120Target extends BlockBackport120EtFGapSimple {
    public BlockBackport120Target(){
        // Target does not require a correct tool to drop; rock incorrectly required a pickaxe.
        super(Material.grass,"target","target_side","target_top",.5F,.5F,Block.soundTypeGrass,"hoe",0);
    }
    @Override public boolean canProvidePower(){return true;}
    @Override public boolean canConnectRedstone(IBlockAccess world,int x,int y,int z,int side){return side>=0;}
    @Override public int isProvidingWeakPower(IBlockAccess world,int x,int y,int z,int side){return world.getBlockMetadata(x,y,z)&15;}
    @Override public int isProvidingStrongPower(IBlockAccess world,int x,int y,int z,int side){return 0;}

    public void hit(World world,int x,int y,int z,int strength,int duration){
        if(world.isRemote||strength<1||strength>15||world.getBlock(x,y,z)!=this)return;
        // Nonzero metadata denotes the outstanding pulse. Do not extend or replace it.
        if((world.getBlockMetadata(x,y,z)&15)!=0)return;
        if(world.setBlockMetadataWithNotify(x,y,z,strength,3))world.scheduleBlockUpdate(x,y,z,this,duration);
    }
    @Override public void onBlockAdded(World world,int x,int y,int z){
        super.onBlockAdded(world,x,y,z);
        // Placing a copied powered state must not create an unscheduled permanent signal.
        if(!world.isRemote&&(world.getBlockMetadata(x,y,z)&15)!=0)world.setBlockMetadataWithNotify(x,y,z,0,3);
    }
    @Override public void updateTick(World world,int x,int y,int z,Random random){
        if(!world.isRemote&&world.getBlock(x,y,z)==this&&(world.getBlockMetadata(x,y,z)&15)!=0)
            world.setBlockMetadataWithNotify(x,y,z,0,3);
    }
    @Override public void breakBlock(World world,int x,int y,int z,Block old,int metadata){
        super.breakBlock(world,x,y,z,old,metadata);
        if((metadata&15)!=0)world.notifyBlocksOfNeighborChange(x,y,z,this);
    }
}
