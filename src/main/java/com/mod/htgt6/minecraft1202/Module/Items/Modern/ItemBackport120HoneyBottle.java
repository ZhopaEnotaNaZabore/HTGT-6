package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Stackable food bottle that returns an empty glass bottle after use. */
public final class ItemBackport120HoneyBottle extends ItemFood {
    public ItemBackport120HoneyBottle(){super(6,0.1F,false);setAlwaysEdible();setMaxStackSize(16);setContainerItem(Items.glass_bottle);
        setUnlocalizedName("backport_1_20.honey_bottle");setTextureName("htgt6:1_20_backport/honey_bottle");}
    @Override public ItemStack onEaten(ItemStack stack,World world,EntityPlayer player){
        if(world.isRemote)return stack;
        player.getFoodStats().func_151686_a(this,stack);
        player.removePotionEffect(net.minecraft.potion.Potion.poison.id);
        world.playSoundAtEntity(player,"htgt6:item.honey_bottle.drink",1F,1F);
        if(player.capabilities.isCreativeMode)return stack;
        if(--stack.stackSize<=0)return new ItemStack(Items.glass_bottle);
        if(!player.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle)))
            player.dropPlayerItemWithRandomChoice(new ItemStack(Items.glass_bottle),false);
        return stack;
    }
    @Override public int getMaxItemUseDuration(ItemStack stack){return 40;}
    @Override public net.minecraft.item.EnumAction getItemUseAction(ItemStack stack){return net.minecraft.item.EnumAction.drink;}
}
