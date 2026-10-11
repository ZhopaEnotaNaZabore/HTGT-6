package com.mod.htgt6.minecraft1202.Module.Items.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Facing;
import net.minecraft.world.World;

/** Places the 1.8 armor-stand entity while preserving the 1.7.10 item-use contract. */
public final class ItemBountifulArmorStand extends Item {
    public ItemBountifulArmorStand() {
        setUnlocalizedName("bountiful_armor_stand");
        setTextureName("htgt6:bountiful/wooden_armorstand");
        setCreativeTab(CreativeTabs.tabDecorations);
        setMaxStackSize(16);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
            int side, float hitX, float hitY, float hitZ) {
        if (side == 0) return false;
        Block clicked = world.getBlock(x, y, z);
        if (!clicked.isReplaceable(world, x, y, z)) {
            x += Facing.offsetsXForSide[side];
            y += Facing.offsetsYForSide[side];
            z += Facing.offsetsZForSide[side];
        }
        if (!player.canPlayerEdit(x, y, z, side, stack)
                || !isReplaceable(world, x, y, z) || !isReplaceable(world, x, y + 1, z)) return false;

        AxisAlignedBB placement = AxisAlignedBB.getBoundingBox(x, y, z, x + 1.0D, y + 2.0D, z + 1.0D);
        if (!world.getEntitiesWithinAABBExcludingEntity(null, placement).isEmpty()) return false;
        if (!world.isRemote) {
            EntityBountifulArmorStand stand = new EntityBountifulArmorStand(world);
            float yaw = (float)(Math.floor((player.rotationYaw - 180.0F) * 8.0F / 360.0F + 0.5D) / 8.0D * 360.0D);
            stand.setLocationAndAngles(x + 0.5D, y, z + 0.5D, yaw, 0.0F);
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("EntityTag", 10)) {
                stand.applyItemEntityTag(stack.getTagCompound().getCompoundTag("EntityTag"));
            }
            if(stack.hasDisplayName())stand.setCustomNameTag(stack.getDisplayName());
            if (!world.spawnEntityInWorld(stand)) return false;
        }
        if (!world.isRemote && !player.capabilities.isCreativeMode) --stack.stackSize;
        return true;
    }

    private static boolean isReplaceable(World world, int x, int y, int z) {
        return world.isAirBlock(x, y, z) || world.getBlock(x, y, z).isReplaceable(world, x, y, z);
    }
}
