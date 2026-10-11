package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import net.minecraft.world.World;

import java.util.Random;

/** Metadata bit 0 is the short catalyst bloom animation. */
public final class BlockBackport120SculkCatalyst extends BlockBackport120Sculk {
    public BlockBackport120SculkCatalyst() { super("sculk_catalyst", 3.0F, 5); }

    public void bloom(World world, int x, int y, int z) {
        if (world.isRemote) return;
        world.setBlockMetadataWithNotify(x, y, z, 1, 3);
        world.scheduleBlockUpdate(x, y, z, this, 8);
    }

    @Override public void updateTick(World world, int x, int y, int z, Random random) {
        if (!world.isRemote && world.getBlockMetadata(x, y, z) != 0)
            world.setBlockMetadataWithNotify(x, y, z, 0, 3);
    }
}
