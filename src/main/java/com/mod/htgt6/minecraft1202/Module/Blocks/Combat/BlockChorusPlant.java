package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public final class BlockChorusPlant extends Block {
    public BlockChorusPlant() {
        super(Material.plants);
        setBlockName("chorus_plant");
        setBlockTextureName("htgt6:combat_update/chorus_plant");
        setHardness(0.4F);
        setResistance(Backport120Content.legacyResistance(0.4F));
        setStepSound(soundTypeWood);
        setCreativeTab(CreativeTabs.tabDecorations);
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return CombatContent.getChorusRenderId(); }
    @Override public int getRenderBlockPass() { return 0; }
    @Override public void setBlockBoundsBasedOnState(IBlockAccess world,int x,int y,int z){boolean west=connect(world,x-1,y,z,false),east=connect(world,x+1,y,z,false),down=connect(world,x,y-1,z,true),up=connect(world,x,y+1,z,false),north=connect(world,x,y,z-1,false),south=connect(world,x,y,z+1,false);setBlockBounds(west?0:.1875F,down?0:.1875F,north?0:.1875F,east?1:.8125F,up?1:.8125F,south?1:.8125F);}
    private boolean connect(IBlockAccess world,int x,int y,int z,boolean down){Block block=world.getBlock(x,y,z);return block==this||block==CombatContent.chorusFlower||(down&&block==Blocks.end_stone);}
    @Override public boolean canPlaceBlockAt(World world,int x,int y,int z){return super.canPlaceBlockAt(world,x,y,z)&&canBlockStay(world,x,y,z);}
    @Override public boolean canBlockStay(World world,int x,int y,int z){
        Block below=world.getBlock(x,y-1,z);
        boolean blocked=!world.isAirBlock(x,y+1,z)&&!world.isAirBlock(x,y-1,z);
        for(int side=2;side<6;side++){
            int nx=x+net.minecraft.util.Facing.offsetsXForSide[side],nz=z+net.minecraft.util.Facing.offsetsZForSide[side];
            if(world.getBlock(nx,y,nz)!=this)continue;
            if(blocked)return false;
            Block support=world.getBlock(nx,y-1,nz);
            if(support==this||support==Blocks.end_stone)return true;
        }
        return below==this||below==Blocks.end_stone;
    }
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block block){
        if(!world.isRemote&&!canBlockStay(world,x,y,z))world.scheduleBlockUpdate(x,y,z,this,1);
    }
    @Override public void updateTick(World world,int x,int y,int z,Random random){
        if(!world.isRemote&&!canBlockStay(world,x,y,z))world.func_147480_a(x,y,z,true);
    }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) {
        return random.nextInt(2) == 0 ? CombatContent.chorusFruit : null;
    }
}
