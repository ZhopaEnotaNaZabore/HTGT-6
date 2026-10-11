package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120CopperFamily;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/** Named metadata item for oxidation and wax variants. */
public final class ItemBlockBackport120CopperFamily extends ItemBlock {
    private final BlockBackport120CopperFamily family;
    public ItemBlockBackport120CopperFamily(Block block){super(block);family=(BlockBackport120CopperFamily)block;setHasSubtypes(true);}
    @Override public int getMetadata(int damage){return damage&7;}
    @Override public String getUnlocalizedName(ItemStack stack){return "tile.backport_1_20."+family.variantName(stack.getItemDamage());}
}
