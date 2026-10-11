package com.mod.htgt6.minecraft1202.Module.Items.Combat;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Brewing ingredient that converts splash potions into lingering potions. */
public final class ItemDragonBreath extends Item {
    public ItemDragonBreath() {
        setUnlocalizedName("dragon_breath");
        setTextureName("htgt6:combat_update/dragon_breath");
        setCreativeTab(CreativeTabs.tabBrewing);
    }
    @Override public boolean isPotionIngredient(ItemStack stack) { return true; }
    // The brewing-stand hook handles conversion, including water and instant potions.
    // Keep a valid legacy ingredient expression for third-party callers.
    @Override public String getPotionEffect(ItemStack stack) { return "-14"; }
}
