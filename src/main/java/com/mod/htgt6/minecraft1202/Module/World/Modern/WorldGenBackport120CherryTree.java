package com.mod.htgt6.minecraft1202.Module.World.Modern;

import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import java.util.Random;

/** Adapter for the official 1.20.2 cherry configured tree feature. */
public final class WorldGenBackport120CherryTree extends WorldGenerator {
    public static final FuturaTreeFeatureRuntime120.Config CONFIG = FuturaTreeFeatureRuntime120.CHERRY;
    public static final int MAX_TREE_ABOVE = FuturaTreeFeatureRuntime120.CHERRY_MAX_ABOVE;
    @Override public boolean generate(World world, Random random, int x, int y, int z) {
        return FuturaTreeFeatureRuntime120.cherry(world, random, x, y, z);
    }
}
