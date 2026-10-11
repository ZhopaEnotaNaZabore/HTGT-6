package com.mod.htgt6.common.chemical;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Exact construction and port-count guide shown on the controller item. */
public final class ItemBlockChemicalPlantController extends ItemBlock {
    public ItemBlockChemicalPlantController(Block block) { super(block); }

    @Override @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(EnumChatFormatting.AQUA
                + StatCollector.translateToLocal("tooltip.htgt6.chemical_plant.structure_title"));
        add(tooltip, EnumChatFormatting.WHITE, "dimensions");
        add(tooltip, EnumChatFormatting.YELLOW, "controller");
        add(tooltip, EnumChatFormatting.GRAY, "base");
        add(tooltip, EnumChatFormatting.GOLD, "ports");
        add(tooltip, EnumChatFormatting.GRAY, "lower");
        add(tooltip, EnumChatFormatting.GRAY, "reactor");
        add(tooltip, EnumChatFormatting.GRAY, "upper");
        add(tooltip, EnumChatFormatting.GREEN, "materials");
        add(tooltip, EnumChatFormatting.AQUA, "nei");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void add(List tooltip, EnumChatFormatting color, String suffix) {
        tooltip.add(color + StatCollector.translateToLocal("tooltip.htgt6.chemical_plant." + suffix));
    }
}
