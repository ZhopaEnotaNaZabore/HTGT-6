package com.mod.htgt6.common.mixer;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Exact construction guide for the 5x10x5 Industrial Mixer. */
public final class ItemBlockIndustrialMixerController extends ItemBlock {
    public ItemBlockIndustrialMixerController(Block block) { super(block); }

    @Override @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(EnumChatFormatting.AQUA
                + StatCollector.translateToLocal("tooltip.htgt6.industrial_mixer.structure_title"));
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
                "tooltip.htgt6.industrial_mixer." + suffix));
    }
}
