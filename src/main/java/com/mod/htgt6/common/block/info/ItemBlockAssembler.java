package com.mod.htgt6.common.block.info;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ItemBlockAssembler extends ItemBlock {

    public ItemBlockAssembler(Block block) {
        super(block);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        // Check if either Shift key is pressed using LWJGL
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.GREEN + "Part of HiTech6 Machinery.");
            list.add(EnumChatFormatting.GRAY + "Продвинутое создание компонентов.");
            list.add(EnumChatFormatting.DARK_PURPLE+ "Базовый тир машины LV");
            list.add(EnumChatFormatting.DARK_PURPLE+ "Для модификации используйте модули");
            list.add(EnumChatFormatting.DARK_PURPLE+ "Трансформатора и тир апа");
        } else {
            list.add(EnumChatFormatting.GRAY + "Hold " + EnumChatFormatting.YELLOW + "SHIFT" + EnumChatFormatting.GRAY + " for more info.");
        }
    }
}