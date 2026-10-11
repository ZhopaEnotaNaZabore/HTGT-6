package com.mod.htgt6.common.electronics;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Exact construction guide for the 5x7x10 controller-centred structure. */
public final class ItemBlockElectronicFactoryController extends ItemBlock {
    public ItemBlockElectronicFactoryController(Block block) { super(block); }

    @Override @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(EnumChatFormatting.AQUA
                + StatCollector.translateToLocal("tooltip.htgt6.electronic_factory.structure_title"));
        add(tooltip, EnumChatFormatting.WHITE, "dimensions");
        add(tooltip, EnumChatFormatting.YELLOW, "controller");
        add(tooltip, EnumChatFormatting.GOLD, "output");
        add(tooltip, EnumChatFormatting.GRAY, "shell");
        add(tooltip, EnumChatFormatting.GRAY, "glass");
        add(tooltip, EnumChatFormatting.GREEN, "ports");
        add(tooltip, EnumChatFormatting.AQUA, "nei");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void add(List tooltip, EnumChatFormatting color, String suffix) {
        tooltip.add(color + StatCollector.translateToLocal("tooltip.htgt6.electronic_factory." + suffix));
    }
}
