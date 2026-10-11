package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

/** trees_cherry plus flower_cherry placed-feature adapter. */
public final class WorldGenBackport120CherryGrove extends WorldGenerator {
    public static final int TREE_COUNT = 10;
    public static final int TREE_EXTRA_WEIGHT = 10;
    public static final int FLOWER_TRIES = 96;
    private final int radius;
    public WorldGenBackport120CherryGrove(int radius) { this.radius = Math.max(4, radius); }
    @Override public boolean generate(World world, Random random, int x, int y, int z) {
        boolean placed = false;
        int trees = TREE_COUNT + (random.nextInt(TREE_EXTRA_WEIGHT) == 0 ? 1 : 0);
        for (int i = 0; i < trees; ++i) {
            int px = x + random.nextInt(radius * 2) - radius;
            int pz = z + random.nextInt(radius * 2) - radius;
            if(world.getBiomeGenForCoords(px,pz).biomeID!=FuturaBiomeIds120.CHERRY_GROVE)continue;
            int py = world.getTopSolidOrLiquidBlock(px, pz);
            if (world.getBlock(px, py - 1, pz) == net.minecraft.init.Blocks.grass)
                placed |= new WorldGenBackport120CherryTree().generate(world, random, px, py, pz);
        }
        int flowerPatches = FuturaTreeFeatureRuntime120.cherryFlowerPatchCount(x-radius,z-radius);
        for (int patch = 0; patch < flowerPatches; patch++) for (int i = 0; i < FLOWER_TRIES; ++i) {
            int px = x + random.nextInt(13) - 6;
            int pz = z + random.nextInt(13) - 6;
            if(world.getBiomeGenForCoords(px,pz).biomeID!=FuturaBiomeIds120.CHERRY_GROVE)continue;
            int py = world.getTopSolidOrLiquidBlock(px, pz) + random.nextInt(5) - 2;
            if (world.isAirBlock(px, py, pz) && world.getBlock(px, py - 1, pz) == net.minecraft.init.Blocks.grass)
                placed |= world.setBlock(px, py, pz, Backport120Content.pinkPetals,
                        random.nextInt(4) | (random.nextInt(4) << 2), 2);
        }
        return placed;
    }
}
