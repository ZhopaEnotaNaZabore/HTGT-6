package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120SmallDripleaf;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public final class ItemBlockBackport120SmallDripleaf extends ItemBlock {
    public ItemBlockBackport120SmallDripleaf(Block block) { super(block); }
    @Override public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                                       int side, float hitX, float hitY, float hitZ) {
        if (!world.getBlock(x, y, z).isReplaceable(world, x, y, z)) ++y;
        if (y <= 0 || y + 1 >= world.getHeight() || !world.isAirBlock(x, y, z)
                || !world.isAirBlock(x, y + 1, z) || !BlockBackport120SmallDripleaf.isSoil(world.getBlock(x, y - 1, z))
                || !player.canPlayerEdit(x, y, z, side, stack)) return false;
        int facing = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        if (!world.isRemote) {
            world.setBlock(x, y, z, field_150939_a, facing, 2);
            world.setBlock(x, y + 1, z, field_150939_a, facing | BlockBackport120SmallDripleaf.UPPER, 3);
            --stack.stackSize;
        }
        return true;
    }
}
