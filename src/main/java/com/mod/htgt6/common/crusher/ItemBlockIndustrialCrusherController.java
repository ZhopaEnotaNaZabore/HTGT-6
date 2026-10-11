package com.mod.htgt6.common.crusher;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Exact construction guide for the 7x6x16 Industrial Crusher. */
public final class ItemBlockIndustrialCrusherController extends ItemBlock {
    public ItemBlockIndustrialCrusherController(Block block) { super(block); }

    @Override @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(EnumChatFormatting.AQUA
                + StatCollector.translateToLocal("tooltip.htgt6.industrial_crusher.structure_title"));
        add(tooltip, EnumChatFormatting.WHITE, "dimensions");
        add(tooltip, EnumChatFormatting.YELLOW, "controller");
        add(tooltip, EnumChatFormatting.GRAY, "base");
        add(tooltip, EnumChatFormatting.GRAY, "chamber");
        add(tooltip, EnumChatFormatting.GREEN, "ports");
        add(tooltip, EnumChatFormatting.AQUA, "nei");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void add(List tooltip, EnumChatFormatting color, String suffix) {
        tooltip.add(color + StatCollector.translateToLocal(
                "tooltip.htgt6.industrial_crusher." + suffix));
    }
}
