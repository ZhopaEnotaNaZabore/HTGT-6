package com.mod.htgt6.minecraft1202.Module.Items.Combat;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.AxisAlignedBB;

public final class ItemEndCrystal extends Item {
    public ItemEndCrystal() { setUnlocalizedName("end_crystal"); setTextureName("htgt6:combat_update/end_crystal"); setCreativeTab(CreativeTabs.tabDecorations); }
    @Override public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
            int side, float hitX, float hitY, float hitZ) {
        net.minecraft.block.Block block = world.getBlock(x, y, z);
        if (block != net.minecraft.init.Blocks.obsidian && block != net.minecraft.init.Blocks.bedrock) return false;
        if (!player.canPlayerEdit(x,y+1,z,side,stack)||!world.isAirBlock(x, y + 1, z)) return false;
        if (!world.getEntitiesWithinAABBExcludingEntity(null,
                AxisAlignedBB.getBoundingBox(x, y + 1, z, x + 1, y + 3, z + 1)).isEmpty()) return false;
        if (!world.isRemote) {
            EntityEnderCrystal crystal = new EntityEnderCrystal(world);
            crystal.setPosition(x + 0.5D, y + 1.0D, z + 0.5D);
            if(!world.spawnEntityInWorld(crystal))return false;
            com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.EndMechanics.tryRespawnDragon(world);
        }
        if (!player.capabilities.isCreativeMode) --stack.stackSize;
        return true;
    }
}
