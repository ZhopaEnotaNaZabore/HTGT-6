package com.mod.htgt6.minecraft1202.Module.Items.Exploration;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/** Minecraft 1.12 totem item; activation is server-authoritative in the event handler. */
public final class ItemTotemOfUndying112 extends Item {
    public ItemTotemOfUndying112() {
        setUnlocalizedName("totem_of_undying");
        setTextureName("htgt6:exploration_update/totem_of_undying");
        setCreativeTab(CreativeTabs.tabCombat);
        setMaxStackSize(1);
    }
}
