package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import com.mod.htgt6.minecraft1202.Module.World.Modern.WorldGenBackport120AzaleaTree;
import com.mod.htgt6.minecraft1202.Module.World.Modern.WorldGenBackport120CherryTree;
import com.mod.htgt6.minecraft1202.Module.World.Modern.WorldGenBackport120MangroveTree;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaPlacementData120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.WorldGenBackport120CherryGrove;
import com.mod.htgt6.minecraft1202.Module.World.Modern.WorldGenBackport120MangroveSwamp;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaTreeFeatureRuntime120;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;

/** Startup contract for sapling registration and the 256/384 build-height seams. */
public final class Backport120TreeGrowthFixture {
    public static void assertContract() {
        assertIntrinsicContract();
        registered("backport_1_20_cherry_sapling", Backport120Content.cherrySapling);
        registered("backport_1_20_mangrove_propagule", Backport120Content.mangrovePropagule);
        registered("backport_1_20_azalea", Backport120Content.azalea);
        registered("backport_1_20_flowering_azalea", Backport120Content.floweringAzalea);
        registered("backport_1_20_bamboo_sapling", Backport120Content.bambooSapling);
    }

    private static void assertIntrinsicContract() {
        growable("cherry sapling", Backport120Content.cherrySapling);
        growable("mangrove propagule", Backport120Content.mangrovePropagule);
        growable("azalea", Backport120Content.azalea);
        growable("flowering azalea", Backport120Content.floweringAzalea);
        growable("bamboo sapling", Backport120Content.bambooSapling);
        growable("bamboo", Backport120Content.bamboo);

        heightSeam("cherry", WorldGenBackport120CherryTree.MAX_TREE_ABOVE);
        heightSeam("mangrove", WorldGenBackport120MangroveTree.MAX_TREE_ABOVE);
        heightSeam("azalea", WorldGenBackport120AzaleaTree.MAX_TREE_ABOVE);
        if (!Backport120Content.cherryLog.isWood(null, 0, 0, 0)
                || !Backport120Content.mangroveLog.isWood(null, 0, 0, 0)
                || !Backport120Content.cherryLog.canSustainLeaves(null, 0, 0, 0)
                || !Backport120Content.mangroveLog.canSustainLeaves(null, 0, 0, 0)) {
            throw new IllegalStateException("Backport tree logs do not expose Forge wood/leaf support");
        }
        referenceParameters();
    }

    public static void main(String[] args) {
        assertIntrinsicContract();
        System.out.println("backport-tree-growth: IGrowable=6, heights=256/384, official-configs=4, wiring=5, roots="
                + Long.toHexString(FuturaTreeFeatureRuntime120.mangroveRootFixtureFingerprint())+", placement-noise="
                +Long.toHexString(FuturaTreeFeatureRuntime120.placementNoiseFixtureFingerprint()));
    }

    private static void referenceParameters() {
        FuturaTreeFeatureRuntime120.Config cherry = FuturaTreeFeatureRuntime120.CHERRY;
        require(cherry.baseHeight == 7 && cherry.heightRandA == 1 && cherry.heightRandB == 0
                && cherry.foliageRadius == 4 && cherry.foliageHeight == 5, "cherry configured_feature");
        require(FuturaTreeFeatureRuntime120.CHERRY_BRANCH_MIN == 2
                && FuturaTreeFeatureRuntime120.CHERRY_BRANCH_MAX == 4
                && close(FuturaTreeFeatureRuntime120.CHERRY_WIDE_HOLE, .25F)
                && close(FuturaTreeFeatureRuntime120.CHERRY_HANGING, 1F / 6F), "cherry placers");
        FuturaTreeFeatureRuntime120.Config azalea = FuturaTreeFeatureRuntime120.AZALEA;
        require(azalea.baseHeight == 4 && azalea.heightRandA == 2 && azalea.leafAttempts == 50,
                "azalea configured_feature");
        FuturaTreeFeatureRuntime120.Config mangrove = FuturaTreeFeatureRuntime120.MANGROVE;
        FuturaTreeFeatureRuntime120.Config tall = FuturaTreeFeatureRuntime120.TALL_MANGROVE;
        require(mangrove.baseHeight == 2 && mangrove.heightRandB == 4 && mangrove.leafAttempts == 70
                && tall.baseHeight == 4 && tall.heightRandB == 9
                && FuturaTreeFeatureRuntime120.MANGROVE_ROOT_WIDTH == 8
                && FuturaTreeFeatureRuntime120.MANGROVE_ROOT_LENGTH == 15
                && WorldGenBackport120MangroveTree.MAX_TREE_ABOVE == 18
                && WorldGenBackport120MangroveTree.TALL_MAX_TREE_ABOVE == 36, "mangrove configured_feature");
        require(FuturaTreeFeatureRuntime120.BAMBOO_MIN_HEIGHT == 5
                && FuturaTreeFeatureRuntime120.BAMBOO_HEIGHT_RANGE == 12
                && close(FuturaPlacementData120.INSTANCE.configured("bamboo_some_podzol").getAsJsonObject("config").get("probability").getAsFloat(), .2F)
                && FuturaPlacementData120.INSTANCE.placed("bamboo_light").getAsJsonArray("placement").get(0).getAsJsonObject().get("chance").getAsInt() == 4, "bamboo configured/placed_feature");
        require(WorldGenBackport120CherryTree.CONFIG == cherry
                && WorldGenBackport120AzaleaTree.CONFIG == azalea
                && WorldGenBackport120MangroveTree.NORMAL_CONFIG == mangrove
                && WorldGenBackport120MangroveTree.TALL_CONFIG == tall, "sapling generator wiring");
        require(WorldGenBackport120CherryGrove.TREE_COUNT == 10
                && WorldGenBackport120CherryGrove.TREE_EXTRA_WEIGHT == 10
                && WorldGenBackport120CherryGrove.FLOWER_TRIES == 96, "trees_cherry placed_feature");
        require(WorldGenBackport120MangroveSwamp.TREE_COUNT == 25
                && close(WorldGenBackport120MangroveSwamp.TALL_CHANCE, .85F), "trees_mangrove placed_feature");
        require(FuturaTreeFeatureRuntime120.BEEHIVE_DECORATOR_SUPPORTED
                && close(FuturaTreeFeatureRuntime120.CHERRY_BEEHIVE_CHANCE,.05F)
                && close(FuturaTreeFeatureRuntime120.MANGROVE_BEEHIVE_CHANCE,.01F), "beehive decorator gate");
        require(FuturaTreeFeatureRuntime120.mangroveRootFixtureFingerprint() == 0x009579df722e4085L,
                "mangrove root seed fingerprint");
        require(FuturaTreeFeatureRuntime120.placementNoiseFixtureFingerprint() == 0xe60e7c453df3b2bcL,
                "Biome.BIOME_INFO_NOISE placement fingerprint");
    }

    private static boolean close(float a, float b) { return Math.abs(a - b) < 0.000001F; }
    private static void require(boolean condition, String name) {
        if (!condition) throw new IllegalStateException("Reference tree contract mismatch: " + name);
    }

    private static void growable(String name, Block block) {
        if (!(block instanceof IGrowable)) throw new IllegalStateException(name + " is not IGrowable");
    }

    private static void registered(String path, Block expected) {
        Object key = Block.blockRegistry.getNameForObject(expected);
        if (key == null || !key.toString().endsWith(":" + path)) {
            throw new IllegalStateException(path + " registration missing: " + key);
        }
    }

    private static void heightSeam(String name, int above) {
        if (!fits(255 - above, above, 256) || fits(256 - above, above, 256)
                || !fits(383 - above, above, 384) || fits(384 - above, above, 384)) {
            throw new IllegalStateException(name + " build-height seam is inconsistent");
        }
    }

    static boolean fits(int baseY, int above, int height) {
        return baseY >= 1 && baseY + above < height;
    }

    private Backport120TreeGrowthFixture() { }
}
