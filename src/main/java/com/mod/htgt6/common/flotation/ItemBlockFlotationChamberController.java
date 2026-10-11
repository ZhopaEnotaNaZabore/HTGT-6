package com.mod.htgt6.common.flotation;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Construction guide for the 13-wide, 11-deep, 7-high reference layout. */
public final class ItemBlockFlotationChamberController extends ItemBlock {
    public ItemBlockFlotationChamberController(Block block) { super(block); }

    @Override @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(EnumChatFormatting.AQUA
                + StatCollector.translateToLocal("tooltip.htgt6.flotation_chamber.structure_title"));
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
                "tooltip.htgt6.flotation_chamber." + suffix));
    }
}
