package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Bamboo's inventory item places the sapling state, as in modern Minecraft. */
public final class ItemBlockBackport120Bamboo extends ItemBlock {
    public ItemBlockBackport120Bamboo(Block block){super(block);}
    @Override public boolean placeBlockAt(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,
                                          float hitX,float hitY,float hitZ,int metadata){
        Block below=world.getBlock(x,y-1,z);
        if(below==Backport120Content.bamboo||below==Backport120Content.bambooSapling){
            int age=below==Backport120Content.bamboo?(world.getBlockMetadata(x,y-1,z)&1):0;
            if(!world.setBlock(x,y,z,Backport120Content.bamboo,age,3))return false;
            if(below==Backport120Content.bambooSapling)world.setBlock(x,y-1,z,Backport120Content.bamboo,0,3);
            return true;
        }
        return Backport120Content.bambooSapling.canPlaceBlockAt(world,x,y,z)
                && world.setBlock(x,y,z,Backport120Content.bambooSapling,0,3);
    }
}
