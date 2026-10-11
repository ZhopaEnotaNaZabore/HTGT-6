package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.AquiferSampler120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.CaveDensityRuntime120;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.NoiseChunk120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.CaveNoiseChunk120;

/** Headless fixed-seed checks callable from tests or a development command. */
public final class CaveRuntimeFixtures120 {
    public static final long FIXTURE_SEED = 0x1202CAFE5EEDL;
    /** NoiseRouterData 1.20.2 rangeChoice + overworld slide/squeeze contract. */
    // Rebaselined after the independent vanilla positional RNG vectors passed.
    public static final long EXPECTED_FINGERPRINT = 0x7B04985185F2D4FDL;

    /** Standalone entry point for CI/dev use; requires no Minecraft classes. */
    public static void main(String[] arguments) {
        if (arguments.length > 0 && "startup".equals(arguments[0])) {
            assertStartupContract();
            return;
        }
        System.out.println(Long.toUnsignedString(fixedSeedFingerprint(), 16));
        assertInvariants();
    }

    public static long fixedSeedFingerprint() {
        CaveDensityRuntime120 caves = new CaveDensityRuntime120(FIXTURE_SEED);
        AquiferSampler120 aquifer = new AquiferSampler120(FIXTURE_SEED);
        long hash = 0xCBF29CE484222325L;
        for (int x = -192; x <= 192; x += 48) {
            for (int z = -160; z <= 160; z += 40) {
                for (int y = 0; y <= 375; y += 25) {
                    CaveDensityRuntime120.Sample sample = caves.sample(x, y, z, 0.75);
                    AquiferSampler120.Result result = aquifer.sample(x, y, z, sample);
                    hash = append(hash, Double.doubleToLongBits(sample.cheese));
                    hash = append(hash, Double.doubleToLongBits(sample.spaghetti));
                    hash = append(hash, Double.doubleToLongBits(sample.entrance));
                    hash = append(hash, Double.doubleToLongBits(sample.noodle));
                    hash = append(hash, Double.doubleToLongBits(sample.pillar));
                    hash = append(hash, Double.doubleToLongBits(sample.routedDensity));
                    hash = append(hash, Double.doubleToLongBits(sample.carveDensity));
                    hash = append(hash, result.decision.ordinal());
                    hash = append(hash, result.fluidLevel);
                }
            }
        }
        return hash;
    }

    /** Fast fail-fast checks safe to execute during normal mod startup. */
    public static void assertStartupContract() {
        if (CaveDensityRuntime120.modernToFuturaY(-64) != 0
                || CaveDensityRuntime120.modernToFuturaY(319) != 383
                || CaveDensityRuntime120.futuraToModernY(383) != 319)
            throw new IllegalStateException("Modern/Futura Y mapping changed");

        assertReferenceFormulaContract();

        CaveDensityRuntime120 first = new CaveDensityRuntime120(FIXTURE_SEED);
        CaveDensityRuntime120 second = new CaveDensityRuntime120(FIXTURE_SEED);
        int[][] points = {{0, 64, 0}, {31, 117, -47}, {-128, 208, 96}, {17, 375, 33}};
        for (int[] point : points) {
            SamplePair pair = new SamplePair(
                    first.sample(point[0], point[1], point[2], 0.75D),
                    second.sample(point[0], point[1], point[2], 0.75D));
            if (Double.doubleToLongBits(pair.first.routedDensity)
                    != Double.doubleToLongBits(pair.second.routedDensity))
                throw new IllegalStateException("Cave runtime is not deterministic");
        }
    }

    public static void assertInvariants() {
        if (CaveDensityRuntime120.modernToFuturaY(-64) != 0
                || CaveDensityRuntime120.modernToFuturaY(319) != 383
                || CaveDensityRuntime120.futuraToModernY(383) != 319)
            throw new IllegalStateException("Modern/Futura Y mapping changed");

        assertReferenceFormulaContract();

        CaveDensityRuntime120 first = new CaveDensityRuntime120(FIXTURE_SEED);
        CaveDensityRuntime120 second = new CaveDensityRuntime120(FIXTURE_SEED);
        FuturaMountainDensityRouter120 routedTerrain=new FuturaMountainDensityRouter120(FIXTURE_SEED);
        CaveNoiseChunk120 routedChunk=routedTerrain.createNoiseChunk(0,0,384,first);
        double[] routedColumn=new double[384];
        routedChunk.fillColumn(7,11,1,routedColumn);
        CaveNoiseChunk120.Statistics routedStats=routedChunk.statistics();
        if(routedStats.evaluatedNodes!=2450||routedStats.interpolatedSamples!=766)
            throw new IllegalStateException("Cave NoiseChunk cache contract changed: nodes="
                    +routedStats.evaluatedNodes+", samples="+routedStats.interpolatedSamples);
        for(int y=1;y<384;y++)if(!Double.isFinite(routedColumn[y]))
            throw new IllegalStateException("Non-finite interpolated cave density at Y="+y);
        AquiferSampler120 aquifer = new AquiferSampler120(FIXTURE_SEED);
        AquiferSampler120 physicalAquifer = new AquiferSampler120(FIXTURE_SEED,
                AquiferSampler120.DEFAULT_SEA_LEVEL, AquiferSampler120.DEFAULT_LAVA_LEVEL,
                new FuturaMountainDensityRouter120(FIXTURE_SEED), 255);
        for (int x = -64; x <= 64; x += 32) for (int z = -64; z <= 64; z += 32) {
            int preliminary = physicalAquifer.preliminarySurfaceModern(x, z);
            if (preliminary > 191)
                throw new IllegalStateException("256-height aquifer leaked a surface above Y=255: "
                        + preliminary);
        }
        double[] terrainDensities = {-0.75D, 0.75D, 1.562499D, 1.5625D, 2.0D};
        int[][] routedPoints = {{0, 0, 0}, {31, 117, -47}, {-128, 208, 96}, {17, 375, 33}};
        for (int[] point : routedPoints) for (double terrainDensity : terrainDensities) {
            double expected = first.sample(point[0], point[1], point[2], terrainDensity).routedDensity;
            double actual = first.sampleRoutedDensity(point[0], point[1], point[2], terrainDensity);
            if (Double.doubleToLongBits(expected) != Double.doubleToLongBits(actual))
                throw new IllegalStateException("Optimized cave route differs from diagnostic route");
        }
        int carved = 0;
        for (int x = -128; x <= 128; x += 16) {
            for (int z = -128; z <= 128; z += 16) {
                for (int y = 0; y <= 380; y += 10) {
                    CaveDensityRuntime120.Sample a = first.sample(x, y, z, 0.75);
                    CaveDensityRuntime120.Sample b = second.sample(x, y, z, 0.75);
                    if (Double.doubleToLongBits(a.carveDensity)
                            != Double.doubleToLongBits(b.carveDensity))
                        throw new IllegalStateException("Cave runtime is not deterministic");
                    if (Double.doubleToLongBits(a.routedDensity)
                            != Double.doubleToLongBits(b.routedDensity))
                        throw new IllegalStateException("Routed cave density is not deterministic");
                    if (a.shouldCarve()) carved++;
                    if (aquifer.sample(x, y, z, false).decision != AquiferSampler120.Decision.SOLID)
                        throw new IllegalStateException("Aquifer replaced an uncarved voxel");
                }
            }
        }
        if (carved == 0) throw new IllegalStateException("Fixture produced no caves");
        int layer117Carved = 0, layer117Samples = 0;
        for (int x = -256; x <= 256; x += 16) for (int z = -256; z <= 256; z += 16) {
            if (first.sample(x, 117, z, 0.75).shouldCarve()) ++layer117Carved;
            ++layer117Samples;
        }
        if (layer117Carved * 5 > layer117Samples * 3)
            throw new IllegalStateException("Cave density opened a world-wide layer near Y=117");
        if (first.sample(0, 0, 0, 2.0).shouldCarve())
            throw new IllegalStateException("Density opened the overworld bottom slide");
        if (!first.sample(0, 383, 0, 2.0).shouldCarve())
            throw new IllegalStateException("Density closed the intended overworld top air slide");
        if (aquifer.sample(0, 0, 0, true).decision != AquiferSampler120.Decision.LAVA
                || aquifer.sample(0, 383, 0, true).decision != AquiferSampler120.Decision.AIR)
            throw new IllegalStateException("Aquifer vertical material contract changed");
        int dry = 0, water = 0;
        for (int x = -192; x <= 192; x += 24) {
            for (int z = -192; z <= 192; z += 24) {
                for (int y = 24; y <= 124; y += 10) {
                    AquiferSampler120.Decision decision = aquifer.sample(x, y, z, true).decision;
                    if (decision == AquiferSampler120.Decision.AIR) dry++;
                    else if (decision == AquiferSampler120.Decision.WATER) water++;
                }
            }
        }
        if (water == 0 || dry <= water)
            throw new IllegalStateException("Aquifer fixture must contain water while keeping most caves dry");
        long fingerprint = fixedSeedFingerprint();
        if (fingerprint != EXPECTED_FINGERPRINT)
            throw new IllegalStateException("Fixed-seed cave fingerprint changed: expected 0x"
                    + Long.toHexString(EXPECTED_FINGERPRINT) + ", actual 0x"
                    + Long.toHexString(fingerprint));
    }

    public static boolean invariantsHold() {
        try {
            assertInvariants();
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    /** Fixed values transcribed from NoiseRouterData in the local 1.20.2 client reference. */
    private static void assertReferenceFormulaContract() {
        if (CaveDensityRuntime120.rarityType1(-1.0D) != 0.5D
                || CaveDensityRuntime120.rarityType1(1.0D) != 3.0D
                || CaveDensityRuntime120.rarityType2(-1.0D) != 0.75D
                || CaveDensityRuntime120.rarityType2(1.0D) != 2.0D)
            throw new IllegalStateException("Signed WeirdScaledSampler rarity contract changed");
        double vertical = CaveDensityRuntime120.spaghetti2dVerticalDensity(1.0D, 0.0D, -64.0D);
        if (Math.abs(vertical - (-0.857375D)) > 1.0E-12D)
            throw new IllegalStateException("Spaghetti 2D elevation/thickness cube changed: " + vertical);
        double scaled=CaveDensityRuntime120.finalDensityShapeForFixture(1.0D,0.0D,64.0D);
        double expected=.64D/2.0D-.64D*.64D*.64D/24.0D;
        if(Math.abs(scaled-expected)>1.0E-15D)
            throw new IllegalStateException("Final density lost official 0.64 multiplier: "+scaled);
    }

    private static long append(long hash, long value) {
        hash ^= value;
        return hash * 0x100000001B3L;
    }

    private static final class SamplePair {
        final CaveDensityRuntime120.Sample first, second;
        SamplePair(CaveDensityRuntime120.Sample first, CaveDensityRuntime120.Sample second) {
            this.first = first;
            this.second = second;
        }
    }

    private CaveRuntimeFixtures120() { }
}
