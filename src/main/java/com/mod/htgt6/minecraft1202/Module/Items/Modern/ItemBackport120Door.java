package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Door;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Stack-64 door item using modern placement choices and legacy two-block metadata. */
public final class ItemBackport120Door extends Item {
    private final BlockBackport120Door door;

    public ItemBackport120Door(String name, BlockBackport120Door door) {
        this.door = door;
        setUnlocalizedName("backport_1_20." + name);
        setTextureName("htgt6:1_20_backport/" + name);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                                       int x, int y, int z, int side,
                                       float hitX, float hitY, float hitZ) {
        return DoorPlacement120.use(door,stack,player,world,x,y,z,side,hitX,hitY,hitZ);
    }
}
