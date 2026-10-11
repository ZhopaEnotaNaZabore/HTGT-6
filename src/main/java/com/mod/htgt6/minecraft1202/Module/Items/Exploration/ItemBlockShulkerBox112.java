package com.mod.htgt6.minecraft1202.Module.Items.Exploration;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public final class ItemBlockShulkerBox112 extends ItemBlock {
    public ItemBlockShulkerBox112(Block block) { super(block); setMaxStackSize(1); }

    @Override public boolean onItemUseFirst(net.minecraft.item.ItemStack stack,
            net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world,
            int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        Block neutral = com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112.undyedShulkerBox;
        if (Block.getBlockFromItem(stack.getItem()) == neutral
                || world.getBlock(x,y,z) != net.minecraft.init.Blocks.cauldron
                || world.getBlockMetadata(x,y,z) <= 0
                || !player.canPlayerEdit(x,y,z,side,stack)) return false;
        // Forge calls this before BlockCauldron consumes the interaction.
        if (!world.isRemote) {
            net.minecraft.item.ItemStack washed = new net.minecraft.item.ItemStack(neutral);
            if (stack.hasTagCompound()) washed.setTagCompound(
                    (net.minecraft.nbt.NBTTagCompound)stack.getTagCompound().copy());
            player.inventory.setInventorySlotContents(player.inventory.currentItem, washed);
            player.inventory.markDirty();
            net.minecraft.init.Blocks.cauldron.func_150024_a(world,x,y,z,world.getBlockMetadata(x,y,z)-1);
        }
        // A true client return suppresses C08PacketPlayerBlockPlacement in Forge 1.7.
        return !world.isRemote;
    }
}
