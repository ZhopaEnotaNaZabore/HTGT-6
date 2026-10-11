package com.mod.htgt6.minecraft1202.Module.Client.Exploration;

import com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.BlockShulkerBox112;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

/** Keep item forms volumetric after the placed block switches to a tile renderer. */
public final class ShulkerBoxItemRenderer112 implements IItemRenderer {
    private final RenderShulkerBox112 renderer = new RenderShulkerBox112();
    @Override public boolean handleRenderType(ItemStack stack, ItemRenderType type) { return true; }
    @Override public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper) { return true; }
    @Override public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (block instanceof BlockShulkerBox112) renderer.renderBox(((BlockShulkerBox112)block).getColorName(), 0, 0, 0, 0);
    }
}
