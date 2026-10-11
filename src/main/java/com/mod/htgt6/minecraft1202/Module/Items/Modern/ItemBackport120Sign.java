package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Sign;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Places a backported standing/wall pair without substituting the vanilla oak sign. */
public final class ItemBackport120Sign extends Item {
    private final Block standing;
    private final Block wall;

    public ItemBackport120Sign(Block standing, Block wall) {
        this("cherry_sign", standing, wall);
    }

    public ItemBackport120Sign(String name, Block standing, Block wall) {
        this.standing = standing;
        this.wall = wall;
        setMaxStackSize(16);
        setCreativeTab(CreativeTabs.tabDecorations);
        setUnlocalizedName("backport_1_20." + name);
        setTextureName("htgt6:1_20_backport/" + name);
    }

    @Override public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                                       int x, int y, int z, int side,
                                       float hitX, float hitY, float hitZ) {
        if (side < 1 || side > 5 || stack.stackSize < 1 || !world.getBlock(x, y, z).getMaterial().isSolid()) return false;
        int px=x, py=y, pz=z;
        if(side==1)py++; else if(side==2)pz--; else if(side==3)pz++; else if(side==4)px--; else if(side==5)px++;
        if (!player.canPlayerEdit(px, py, pz, side, stack) || !standing.canPlaceBlockAt(world, px, py, pz)) return false;
        if (world.isRemote) return true;
        if (side == 1) {
            int rotation = MathHelper.floor_double((player.rotationYaw + 180.0F) * 16.0F / 360.0F + 0.5D) & 15;
            if(!world.setBlock(px, py, pz, standing, rotation, 3))return false;
        } else if(!world.setBlock(px, py, pz, wall, side, 3))return false;
        if(!player.capabilities.isCreativeMode)stack.stackSize--;
        TileEntity tile = world.getTileEntity(px, py, pz);
        if (tile instanceof TileEntityBackport120Sign) player.func_146100_a((TileEntityBackport120Sign) tile);
        return true;
    }
}
