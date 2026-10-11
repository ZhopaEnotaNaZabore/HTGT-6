package com.mod.htgt6.minecraft1202.Module.Items.Aquatic;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

public final class ItemDriedKelp120 extends ItemFood {
    public ItemDriedKelp120(){super(1,.3F,false);setUnlocalizedName("dried_kelp");setTextureName("htgt6:dried_kelp");setCreativeTab(CreativeTabs.tabFood);}
    @Override public int getMaxItemUseDuration(ItemStack stack){return 16;}
    @Override public EnumAction getItemUseAction(ItemStack stack){return EnumAction.eat;}
}
