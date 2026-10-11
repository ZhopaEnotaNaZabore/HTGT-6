package com.mod.htgt6.minecraft1202.Module.World.Modern;

import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import java.util.Random;

/** Adapter for the official normal/tall mangrove configured tree features. */
public final class WorldGenBackport120MangroveTree extends WorldGenerator {
    public static final FuturaTreeFeatureRuntime120.Config NORMAL_CONFIG = FuturaTreeFeatureRuntime120.MANGROVE;
    public static final FuturaTreeFeatureRuntime120.Config TALL_CONFIG = FuturaTreeFeatureRuntime120.TALL_MANGROVE;
    public static final int MAX_TREE_ABOVE = FuturaTreeFeatureRuntime120.MANGROVE_MAX_ABOVE;
    public static final int TALL_MAX_TREE_ABOVE = FuturaTreeFeatureRuntime120.TALL_MANGROVE_MAX_ABOVE;
    private final boolean tall;
    public WorldGenBackport120MangroveTree() { this(false); }
    public WorldGenBackport120MangroveTree(boolean tall) { this.tall = tall; }
    @Override public boolean generate(World world, Random random, int x, int y, int z) {
        return FuturaTreeFeatureRuntime120.mangrove(world, random, x, y, z, tall);
    }
}
