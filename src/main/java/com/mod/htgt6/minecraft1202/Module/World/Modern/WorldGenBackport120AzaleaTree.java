package com.mod.htgt6.minecraft1202.Module.World.Modern;

import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import java.util.Random;

/** Adapter for azalea_tree; Lush Caves rooted_azalea_tree is deliberately separate. */
public final class WorldGenBackport120AzaleaTree extends WorldGenerator {
    public static final FuturaTreeFeatureRuntime120.Config CONFIG = FuturaTreeFeatureRuntime120.AZALEA;
    public static final int MAX_TREE_ABOVE = FuturaTreeFeatureRuntime120.AZALEA_MAX_ABOVE;
    @Override public boolean generate(World world, Random random, int x, int y, int z) {
        return FuturaTreeFeatureRuntime120.azalea(world, random, x, y, z);
    }
}
