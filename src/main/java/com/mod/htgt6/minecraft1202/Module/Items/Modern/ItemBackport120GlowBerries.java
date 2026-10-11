package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120CaveVines;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public final class ItemBackport120GlowBerries extends ItemFood {
    public ItemBackport120GlowBerries() {
        super(2, 0.1F, false);
        setUnlocalizedName("backport_1_20.glow_berries");
        setTextureName("htgt6:1_20_backport/glow_berries");
        setCreativeTab(CreativeTabs.tabFood);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                             int side, float hitX, float hitY, float hitZ) {
        if (side != 0) return false;
        Block clicked = world.getBlock(x, y, z);
        if (!clicked.isReplaceable(world, x, y, z)) --y;
        if (y < 0 || !player.canPlayerEdit(x, y, z, side, stack) || !world.isAirBlock(x, y, z)
                || !world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN)) return false;
        if (!world.isRemote && world.setBlock(x, y, z, Backport120Content.caveVines,
                BlockBackport120CaveVines.BERRIES, 3)) --stack.stackSize;
        return true;
    }
}
