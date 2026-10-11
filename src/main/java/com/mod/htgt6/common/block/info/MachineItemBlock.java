package com.mod.htgt6.common.block.info;

import com.mod.htgt6.api.hitech6.BaseMachines.BlockMachine;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

/** Shared ItemBlock for standalone machines and BlockMachine metadata variants. */
public class MachineItemBlock extends ItemBlockWithMetadata {
    public MachineItemBlock(Block block) { super(block, block); }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (field_150939_a instanceof BlockMachine) {
            switch (stack.getItemDamage()) {
                case 0: return super.getUnlocalizedName() + ".assembler";
                case 1: return super.getUnlocalizedName() + ".em_processor";
                case 2: return super.getUnlocalizedName() + ".metal_processor";
                default: return super.getUnlocalizedName() + "." + stack.getItemDamage();
            }
        }
        return super.getUnlocalizedName(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        MachineDescriptions.Description description = MachineDescriptions.get(field_150939_a, stack.getItemDamage());
        if (description.isEmpty()) return;
        if (GuiScreen.isShiftKeyDown()) description.addShiftTooltip(tooltip);
        else tooltip.add(EnumChatFormatting.GRAY + "Hold " + EnumChatFormatting.YELLOW + "SHIFT"
                + EnumChatFormatting.GRAY + " for more info.");
    }
}
