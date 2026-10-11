package com.mod.htgt6.common.block.greg.technological.hulls;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

public final class ItemBlockTieredHull extends ItemBlock {
    private final BlockTieredHull hull;
    public ItemBlockTieredHull(Block block) { super(block); hull=(BlockTieredHull)block; }
    @Override @SuppressWarnings({"rawtypes", "unchecked"})
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        String voltage=String.format(java.util.Locale.ROOT,"%,d",hull.getVoltage()).replace(',', ' ');
        lines.add(EnumChatFormatting.GREEN + StatCollector.translateToLocalFormatted("tooltip.htgt6.hull.power",voltage));
        lines.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocalFormatted("tooltip.htgt6.hull.voltage",voltage));
        String capacity=String.format(java.util.Locale.ROOT,"%,d",hull.getEnergyCapacity()).replace(',', ' ');
        lines.add(EnumChatFormatting.AQUA + StatCollector.translateToLocalFormatted("tooltip.htgt6.hull.buffer",capacity));
        lines.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("tooltip.htgt6.hull.direction"));
        lines.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("tooltip.htgt6.hull.no_transform"));
    }
}
