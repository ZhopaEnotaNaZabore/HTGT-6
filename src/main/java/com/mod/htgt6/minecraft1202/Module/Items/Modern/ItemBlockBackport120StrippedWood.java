package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/** Preserves the wood species metadata while placing a stripped log or wood block. */
public final class ItemBlockBackport120StrippedWood extends ItemBlock {
    public ItemBlockBackport120StrippedWood(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override public int getMetadata(int damage) { return damage & 3; }

    @Override public String getUnlocalizedName(ItemStack stack) {
        return field_150939_a.getUnlocalizedName() + "." + (stack.getItemDamage() & 3);
    }
}
