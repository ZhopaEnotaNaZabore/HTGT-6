package com.mod.htgt6.common.mining;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Exact inventory/NEI construction guide attached to the Mining Unit controller item. */
public final class ItemBlockMiningUnitController extends ItemBlock {
    public ItemBlockMiningUnitController(Block block) {
        super(block);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(EnumChatFormatting.AQUA + StatCollector.translateToLocal("tooltip.htgt6.mining_unit.structure_title"));
        add(tooltip, EnumChatFormatting.WHITE, "dimensions");
        add(tooltip, EnumChatFormatting.YELLOW, "controller");
        add(tooltip, EnumChatFormatting.GRAY, "base");
        add(tooltip, EnumChatFormatting.GOLD, "ports_fluid");
        add(tooltip, EnumChatFormatting.GOLD, "ports_item_energy");
        add(tooltip, EnumChatFormatting.GRAY, "layers_2_4");
        add(tooltip, EnumChatFormatting.GRAY, "layer_5");
        add(tooltip, EnumChatFormatting.GRAY, "layers_6_7");
        add(tooltip, EnumChatFormatting.GRAY, "layers_8_9");
        add(tooltip, EnumChatFormatting.GRAY, "layers_10_11");
        add(tooltip, EnumChatFormatting.GRAY, "layer_12");
        add(tooltip, EnumChatFormatting.GREEN, "materials");
        add(tooltip, EnumChatFormatting.AQUA, "nei");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void add(List tooltip, EnumChatFormatting color, String suffix) {
        tooltip.add(color + StatCollector.translateToLocal("tooltip.htgt6.mining_unit." + suffix));
    }
}
