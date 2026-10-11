package com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/** Sticky low-friction block; fall damage is cancelled unless the entity sneaks. */
public final class BlockSlime extends Block {
    public BlockSlime() {
        super(Material.clay);
        setBlockName("slime_block"); setBlockTextureName("htgt6:bountiful/slime_block");
        setHardness(0.0F); setStepSound(soundTypeCloth); setCreativeTab(CreativeTabs.tabBlock);
        slipperiness = 0.8F;
    }
    @Override public void onFallenUpon(World world, int x, int y, int z, Entity entity, float distance) {
        if (entity.isSneaking()) super.onFallenUpon(world, x, y, z, entity, distance);
        else entity.fallDistance = 0.0F;
    }
    @Override public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (Math.abs(entity.motionY) < 0.1D && !entity.isSneaking()) {
            double factor = 0.4D + Math.abs(entity.motionY) * 0.2D;
            entity.motionX *= factor; entity.motionZ *= factor;
        }
        super.onEntityCollidedWithBlock(world, x, y, z, entity);
    }
}
