package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Two food points, low saturation, and placement of an age-zero berry bush. */
public final class ItemBackport120SweetBerries extends ItemFood {
    public ItemBackport120SweetBerries() {
        super(2, 0.1F, false);
        setUnlocalizedName("backport_1_20.sweet_berries");
        setTextureName("htgt6:1_20_backport/sweet_berries");
        setCreativeTab(CreativeTabs.tabFood);
    }
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,
                                       int side,float hitX,float hitY,float hitZ) {
        Block clicked=world.getBlock(x,y,z);if(!clicked.isReplaceable(world,x,y,z))y++;
        if(!player.canPlayerEdit(x,y,z,side,stack)||!Backport120Content.sweetBerryBush.canPlaceBlockAt(world,x,y,z))return false;
        if(!world.isRemote&&world.setBlock(x,y,z,Backport120Content.sweetBerryBush,0,3)&&!player.capabilities.isCreativeMode)--stack.stackSize;
        return true;
    }
}
