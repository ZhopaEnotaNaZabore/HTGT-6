package com.mod.htgt6.common.mining;

import cpw.mods.fml.common.registry.GameRegistry;
import ic2.api.item.IC2Items;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

/** Lazy IC2 Experimental/Classic reinforced-glass compatibility lookup. */
public final class IC2ReinforcedGlass {
    private static ItemStack cached;

    private IC2ReinforcedGlass() { }

    public static ItemStack stack() {
        if (cached == null || Block.getBlockFromItem(cached.getItem()) == Blocks.glass) cached = resolve();
        return cached.copy();
    }

    public static boolean matches(Block block, int metadata) {
        ItemStack required = stack();
        Block requiredBlock = Block.getBlockFromItem(required.getItem());
        return requiredBlock != Blocks.glass && block == requiredBlock
                && (required.getItemDamage() == metadata || required.getItemDamage() == 32767);
    }

    private static ItemStack resolve() {
        ItemStack stack = IC2Items.getItem("reinforcedGlass");
        if (stack == null) stack = IC2Items.getItem("blockAlloyGlass");
        if (stack != null && stack.getItem() != null) return stack.copy();

        Block block = GameRegistry.findBlock("IC2", "blockAlloyGlass");
        if (block == null) block = GameRegistry.findBlock("ic2", "blockAlloyGlass");
        // IC2 is mandatory in this pack. Vanilla glass is only a safe display
        // fallback during an abnormally early client lookup, never a valid wall.
        return new ItemStack(block == null ? Blocks.glass : block, 1, 0);
    }
}
