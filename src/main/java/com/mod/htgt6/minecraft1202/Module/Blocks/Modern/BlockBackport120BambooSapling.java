package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import java.util.Random;

public final class BlockBackport120BambooSapling extends BlockBush implements IGrowable {
    public BlockBackport120BambooSapling(){setBlockName("backport_1_20.bamboo_sapling");setBlockTextureName("htgt6:1_20_backport/bamboo_stage0");setHardness(0);setStepSound(Block.soundTypeGrass);setTickRandomly(true);}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @Override protected boolean canPlaceBlockOn(Block b){return b==Blocks.grass||b==Blocks.dirt||b==Blocks.sand||b==Blocks.gravel||b==Backport120Content.mossBlock;}
    @Override public Item getItemDropped(int m,Random r,int f){return Item.getItemFromBlock(Backport120Content.bamboo);}
    @Override public void updateTick(World w,int x,int y,int z,Random r){super.updateTick(w,x,y,z,r);if(!w.isRemote&&w.getBlockLightValue(x,y+1,z)>=9&&canGrowHere(w,x,y,z)&&r.nextInt(3)==0)grow(w,x,y,z);}
    private void grow(World w,int x,int y,int z){if(!canGrowHere(w,x,y,z))return;if(w.setBlock(x,y+1,z,Backport120Content.bamboo,BlockBackport120Bamboo.state(0,1,0),3))w.setBlock(x,y,z,Backport120Content.bamboo,0,3);}
    private boolean canGrowHere(World w,int x,int y,int z){return w.getBlock(x,y,z)==this&&canBlockStay(w,x,y,z)&&y+1<w.getHeight()&&w.isAirBlock(x,y+1,z);}
    @Override public boolean func_149851_a(World w,int x,int y,int z,boolean c){return canGrowHere(w,x,y,z);}@Override public boolean func_149852_a(World w,Random r,int x,int y,int z){return true;}
    @Override public void func_149853_b(World w,Random r,int x,int y,int z){grow(w,x,y,z);}
}
