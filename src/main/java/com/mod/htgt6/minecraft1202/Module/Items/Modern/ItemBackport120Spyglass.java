package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Minecraft 1.20.2 spyglass use lifecycle adapted to the 1.7 item API. */
public final class ItemBackport120Spyglass extends Item {
    public ItemBackport120Spyglass(){setUnlocalizedName("backport_1_20.spyglass");setTextureName("htgt6:1_20_backport/spyglass");
        setMaxStackSize(1);setCreativeTab(CreativeTabs.tabTools);}
    @Override public int getMaxItemUseDuration(ItemStack stack){return 1200;}
    @Override public EnumAction getItemUseAction(ItemStack stack){return EnumAction.none;}
    @Override public ItemStack onItemRightClick(ItemStack stack,World world,EntityPlayer player){
        player.setItemInUse(stack,getMaxItemUseDuration(stack));
        if(!world.isRemote)world.playSoundAtEntity(player,"htgt6:item.spyglass.use",1.0F,1.0F);
        return stack;
    }
    @Override public void onPlayerStoppedUsing(ItemStack stack,World world,EntityPlayer player,int remaining){
        if(!world.isRemote)world.playSoundAtEntity(player,"htgt6:item.spyglass.stop_using",1.0F,1.0F);
    }
    @Override public ItemStack onEaten(ItemStack stack,World world,EntityPlayer player){
        if(!world.isRemote)world.playSoundAtEntity(player,"htgt6:item.spyglass.stop_using",1.0F,1.0F);
        return stack;
    }
}
