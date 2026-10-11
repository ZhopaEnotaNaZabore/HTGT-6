package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/** Metadata-preserving item form for concrete and concrete powder. */
public final class ItemBlockBackport120Colored extends ItemBlock {
    public ItemBlockBackport120Colored(Block block) { super(block); setHasSubtypes(true); }
    @Override public int getMetadata(int damage) { return damage & 15; }
    @Override public String getUnlocalizedName(ItemStack stack) {
        return "tile.backport_1_20." + Backport120Content.TERRACOTTA_COLORS[stack.getItemDamage()&15]
                + (field_150939_a.getUnlocalizedName().contains("powder") ? "_concrete_powder" : "_concrete");
    }
}
