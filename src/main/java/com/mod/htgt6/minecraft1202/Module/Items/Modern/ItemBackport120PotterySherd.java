package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/** One stable registry item per 1.20.2 pottery pattern. */
public final class ItemBackport120PotterySherd extends Item {
    public ItemBackport120PotterySherd(String name){setUnlocalizedName("backport_1_20."+name);
        setTextureName("htgt6:1_20_backport/"+name);setCreativeTab(CreativeTabs.tabMaterials);}
}
