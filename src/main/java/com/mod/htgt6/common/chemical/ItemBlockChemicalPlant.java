package com.mod.htgt6.common.chemical;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;

/** Metadata names for Chemical Plant structural blocks. */
public final class ItemBlockChemicalPlant extends ItemBlockWithMetadata {
    public ItemBlockChemicalPlant(Block block) { super(block, block); }

    @Override public String getUnlocalizedName(ItemStack stack) {
        if (field_150939_a instanceof BlockChemicalPlantCasing) {
            int metadata = stack.getItemDamage();
            return super.getUnlocalizedName()
                    + (metadata == BlockChemicalPlantCasing.THERMOCHEMICAL ? ".thermochemical" : ".standard");
        }
        return super.getUnlocalizedName(stack);
    }
}
