package com.mod.htgt6.common.mining;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.api.hitech6.multiblock.FluidGatewayVariant;
import com.mod.htgt6.common.multiblock.gateway.BlockMultiblockGateway;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;

/** Metadata names for casings, frames and delegated ports. */
public final class ItemBlockMiningUnit extends ItemBlockWithMetadata {
    public ItemBlockMiningUnit(Block block) { super(block, block); }
    @Override @SuppressWarnings({"rawtypes", "unchecked"})
    public void addInformation(ItemStack stack, net.minecraft.entity.player.EntityPlayer player,
            java.util.List tooltip, boolean advanced) {
        if (field_150939_a instanceof BlockMultiblockGateway
                && FluidGatewayVariant.type(stack.getItemDamage()) == GatewayType.ENERGY_OUTPUT) {
            for (String key : new String[] {"generator", "optional", "tier"}) {
                tooltip.add(net.minecraft.util.EnumChatFormatting.GRAY
                        + net.minecraft.util.StatCollector.translateToLocal("tooltip.htgt6.energy_output." + key));
            }
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack.getItemDamage();
        if (field_150939_a instanceof BlockMultiblockGateway) {
            return super.getUnlocalizedName() + "." + FluidGatewayVariant.localizationSuffix(meta);
        }
        if (field_150939_a instanceof BlockMiningUnitCasing) {
            return super.getUnlocalizedName() + (meta == 1 ? ".diagnostic" : ".standard");
        }
        if (field_150939_a instanceof BlockMiningUnitFrame) {
            return super.getUnlocalizedName() + (meta == 1 ? ".quarter" : ".standard");
        }
        return super.getUnlocalizedName(stack);
    }
}
