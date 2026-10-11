package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.Noise120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.NoiseParameters120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.VanillaNormalNoiseFactory120;

/** Direct scalar port of the Overworld cave graph assembled by NoiseRouterData in 1.20.2. */
public final class CaveDensityRuntime120 {
    public static final int FUTURA_MIN_Y = 0;
    public static final int FUTURA_MAX_Y = 383;
    public static final int MODERN_TO_FUTURA_OFFSET = 64;

    private final Noise120 spaghettiRoughness;
    private final Noise120 spaghettiRoughnessModulator;
    private final Noise120 spaghetti3dRarity;
    private final Noise120 spaghetti3dThickness;
    private final Noise120 spaghetti3dA;
    private final Noise120 spaghetti3dB;
    private final Noise120 entrance;
    private final Noise120 noodle;
    private final Noise120 noodleThickness;
    private final Noise120 noodleRidgeA;
    private final Noise120 noodleRidgeB;
    private final Noise120 pillar;
    private final Noise120 pillarRareness;
    private final Noise120 pillarThickness;
    private final Noise120 spaghetti2dModulator;
    private final Noise120 spaghetti2d;
    private final Noise120 spaghetti2dElevation;
    private final Noise120 spaghetti2dThickness;
    private final Noise120 caveLayer;
    private final Noise120 caveCheese;

    public CaveDensityRuntime120(long worldSeed) {
        VanillaNormalNoiseFactory120 noises = new VanillaNormalNoiseFactory120(worldSeed);
        spaghettiRoughness = noises.create(NoiseParameters120.SPAGHETTI_ROUGHNESS);
        spaghettiRoughnessModulator = noises.create(NoiseParameters120.SPAGHETTI_ROUGHNESS_MODULATOR);
        spaghetti3dRarity = noises.create(NoiseParameters120.SPAGHETTI_3D_RARITY);
        spaghetti3dThickness = noises.create(NoiseParameters120.SPAGHETTI_3D_THICKNESS);
        spaghetti3dA = noises.create(NoiseParameters120.SPAGHETTI_3D_1);
        spaghetti3dB = noises.create(NoiseParameters120.SPAGHETTI_3D_2);
        entrance = noises.create(NoiseParameters120.CAVE_ENTRANCE);
        noodle = noises.create(NoiseParameters120.NOODLE);
        noodleThickness = noises.create(NoiseParameters120.NOODLE_THICKNESS);
        noodleRidgeA = noises.create(NoiseParameters120.NOODLE_RIDGE_A);
        noodleRidgeB = noises.create(NoiseParameters120.NOODLE_RIDGE_B);
        pillar = noises.create(NoiseParameters120.PILLAR);
        pillarRareness = noises.create(NoiseParameters120.PILLAR_RARENESS);
        pillarThickness = noises.create(NoiseParameters120.PILLAR_THICKNESS);
        spaghetti2dModulator = noises.create(NoiseParameters120.SPAGHETTI_2D_MODULATOR);
        spaghetti2d = noises.create(NoiseParameters120.SPAGHETTI_2D);
        spaghetti2dElevation = noises.create(NoiseParameters120.SPAGHETTI_2D_ELEVATION);
        spaghetti2dThickness = noises.create(NoiseParameters120.SPAGHETTI_2D_THICKNESS);
        caveLayer = noises.create(NoiseParameters120.CAVE_LAYER);
        caveCheese = noises.create(NoiseParameters120.CAVE_CHEESE);
    }

    /** Samples the vanilla cave branch against preliminary terrain density. */
    public Sample sample(int blockX, int futuraY, int blockZ, double terrainDensity) {
        requireY(futuraY);
        double modernY = futuraY - MODERN_TO_FUTURA_OFFSET;
        double noodleDensity = noodleDensity(blockX, modernY, blockZ);
        EntranceSample entrance = entrance(blockX, modernY, blockZ);
        // The diagnostic API deliberately evaluates every component. Production
        // generation uses sampleRoutedDensity below and retains vanilla laziness.
        return sampleCaves(blockX, modernY, blockZ, terrainDensity, noodleDensity, entrance);
    }

    /** Allocation-free production path when only the routed density is required. */
    public double sampleRoutedDensity(int blockX, int futuraY, int blockZ, double terrainDensity) {
        requireY(futuraY);
        return sampleRoutedDensityUnchecked(blockX, futuraY, blockZ, terrainDensity);
    }

    /** Value inside slideOverworld and before postProcess(interpolated(...)). */
    public double samplePreInterpolatedDensityCellBoundary(int blockX, int futuraY, int blockZ,
                                                            double terrainDensity) {
        if (futuraY < FUTURA_MIN_Y || futuraY > FUTURA_MAX_Y + 1)
            throw new IllegalArgumentException("Futura cave cell Y must be in 0..384: " + futuraY);
        double modernY=futuraY-MODERN_TO_FUTURA_OFFSET;
        EntranceSample entrance=entrance(blockX,modernY,blockZ);
        if(terrainDensity>=-1000000.0D&&terrainDensity<1.5625D)
            return slideDensity(Math.min(terrainDensity,5.0D*entrance.combined),modernY);
        return sampleCaves(blockX,modernY,blockZ,terrainDensity,64.0D,entrance).preInterpolatedDensity;
    }

    /** Noodle is a separate yLimitedInterpolatable density in NoiseRouterData. */
    public double sampleNoodleCellBoundary(int blockX,int futuraY,int blockZ){
        if(futuraY<FUTURA_MIN_Y||futuraY>FUTURA_MAX_Y+1)
            throw new IllegalArgumentException("Futura noodle cell Y must be in 0..384: "+futuraY);
        return noodleDensity(blockX,futuraY-MODERN_TO_FUTURA_OFFSET,blockZ);
    }

    /** postProcess: mul(interpolated(blendDensity), .64).squeeze(), then min(noodle). */
    public static double finishInterpolatedDensity(double interpolatedDensity,double interpolatedNoodle){
        double clamped=clamp(interpolatedDensity*.64D,-1.0D,1.0D);
        double squeezed=clamped/2.0D-clamped*clamped*clamped/24.0D;
        return Math.min(squeezed,interpolatedNoodle);
    }

    private double sampleRoutedDensityUnchecked(int blockX, int futuraY, int blockZ,
                                                 double terrainDensity) {
        double modernY = futuraY - MODERN_TO_FUTURA_OFFSET;
        double noodleDensity = noodleDensity(blockX, modernY, blockZ);
        EntranceSample entrance = entrance(blockX, modernY, blockZ);
        // NoiseRouterData: rangeChoice(sloped_cheese, -1e6, 1.5625,
        // min(sloped_cheese, 5 * entrances), underground(sloped_cheese)).
        if (terrainDensity >= -1000000.0D && terrainDensity < 1.5625D) {
            return shapeAndNoodle(Math.min(terrainDensity, 5.0D * entrance.combined),
                    modernY, noodleDensity);
        }
        return sampleCaves(blockX, modernY, blockZ, terrainDensity,
                noodleDensity, entrance).routedDensity;
    }

    private Sample sampleCaves(int blockX, double modernY, int blockZ,
                               double terrainDensity, double noodleDensity,
                               EntranceSample entranceSample) {

        // WeirdScaledSampler consumes the signed rarity input.  Taking abs here
        // changes both rarity type piecewise functions from the vanilla graph.
        double modulator2dInput = sample(spaghetti2dModulator,
                blockX, modernY, blockZ, 2.0, 1.0);
        double rarity2d = rarityType2(modulator2dInput);
        double spaghetti2dValue = rarity2d * Math.abs(spaghetti2d.sample(
                blockX / rarity2d, modernY / rarity2d, blockZ / rarity2d));
        // NoiseRouterData maps spaghetti_2d_elevation from 0 to
        // floorDiv(minY, 8) (-8 for the overworld).
        double elevationNoise = sample(spaghetti2dElevation,
                blockX, modernY, blockZ, 1.0, 0.0);
        double thicknessNoise = sample(spaghetti2dThickness,
                blockX, modernY, blockZ, 2.0, 1.0);
        double thickness2d = clamp(mapped(thicknessNoise, -0.6, -1.3), -1.0, 1.0);
        double verticalDensity = spaghetti2dVerticalDensity(elevationNoise, thicknessNoise, modernY);
        double spaghetti2dDensity = clamp(Math.max(spaghetti2dValue + 0.083 * thickness2d,
                verticalDensity), -1.0, 1.0);

        double layerNoise = sample(caveLayer, blockX, modernY, blockZ, 1.0, 8.0);
        double layer = 4.0 * layerNoise * layerNoise;
        double cheeseNoise = sample(caveCheese, blockX, modernY, blockZ, 1.0, 0.6666666666666666);
        double cheese = clamp(0.27 + cheeseNoise, -1.0, 1.0)
                + clamp(1.5 - 0.64 * terrainDensity, 0.0, 0.5);
        double caveBody = layer + cheese;

        double pillarNoise = sample(pillar, blockX, modernY, blockZ, 25.0, 0.3);
        double pillarRarity = mapped(sample(pillarRareness,
                blockX, modernY, blockZ, 1.0, 1.0), 0.0, -2.0);
        double pillarThicknessValue = mapped(sample(pillarThickness,
                blockX, modernY, blockZ, 1.0, 1.0), 0.0, 1.1);
        double pillarDensity = (pillarNoise * 2.0 + pillarRarity)
                * pillarThicknessValue * pillarThicknessValue * pillarThicknessValue;
        double pillarGate = pillarDensity < 0.03 ? -1000000.0 : pillarDensity;

        double spaghettiAndRoughness = Math.max(spaghetti2dDensity, entranceSample.roughness);
        double caves = Math.max(Math.min(Math.min(caveBody, entranceSample.combined),
                spaghettiAndRoughness), pillarGate);

        double routedCaves = terrainDensity >= -1000000.0 && terrainDensity < 1.5625
                ? Math.min(terrainDensity, 5.0D * entranceSample.combined) : caves;
        double preInterpolatedDensity=slideDensity(routedCaves,modernY);
        double finalDensity=finishInterpolatedDensity(preInterpolatedDensity,noodleDensity);
        return new Sample(cheese, Math.min(spaghetti2dDensity, entranceSample.spaghetti3d),
                entranceSample.raw, noodleDensity, pillarDensity, preInterpolatedDensity,finalDensity);
    }

    private EntranceSample entrance(int blockX, double modernY, int blockZ) {
        double roughnessNoise = sample(spaghettiRoughness, blockX, modernY, blockZ, 1.0, 1.0);
        double roughnessModulator = mapped(sample(spaghettiRoughnessModulator,
                blockX, modernY, blockZ, 1.0, 1.0), 0.0, -0.1);
        double roughness = roughnessModulator * (Math.abs(roughnessNoise) - 0.4);

        double rarityInput = sample(spaghetti3dRarity, blockX, modernY, blockZ, 2.0, 1.0);
        double rarity = rarityType1(rarityInput);
        double spaghetti3dAValue = rarity * Math.abs(spaghetti3dA.sample(
                blockX / rarity, modernY / rarity, blockZ / rarity));
        double spaghetti3dBValue = rarity * Math.abs(spaghetti3dB.sample(
                blockX / rarity, modernY / rarity, blockZ / rarity));
        double thickness3d = mapped(sample(spaghetti3dThickness,
                blockX, modernY, blockZ, 1.0, 1.0), -0.065, -0.088);
        double spaghetti3dDensity = clamp(Math.max(spaghetti3dAValue, spaghetti3dBValue)
                + thickness3d, -1.0, 1.0);

        double raw = sample(entrance, blockX, modernY, blockZ, 0.75, 0.5) + 0.37
                + yGradient(modernY, -10.0, 30.0, 0.3, 0.0);
        return new EntranceSample(raw, roughness, spaghetti3dDensity,
                Math.min(raw, roughness + spaghetti3dDensity));
    }

    /** Normal-overworld slide followed by DensityFunction.squeeze and noodle min. */
    private static double shapeAndNoodle(double density, double modernY, double noodleDensity) {
        return finishInterpolatedDensity(slideDensity(density,modernY),noodleDensity);
    }

    private static double slideDensity(double density,double modernY){
        double top = clampedMap(modernY, 240.0D, 256.0D, 1.0D, 0.0D);
        density = lerp(top, -0.078125D, density);
        double bottom = clampedMap(modernY, -64.0D, -40.0D, 0.0D, 1.0D);
        return lerp(bottom, 0.1171875D, density);
    }

    public static double finalDensityShapeForFixture(double density,double modernY,double noodleDensity){
        return shapeAndNoodle(density,modernY,noodleDensity);
    }

    private double noodleDensity(int blockX, double modernY, int blockZ) {
        if (modernY < -60.0 || modernY > 320.0) return 64.0D;
        double noodleToggle = sample(noodle, blockX, modernY, blockZ, 1.0, 1.0);
        if (noodleToggle < 0.0) return 64.0D;
        double noodleThicknessValue = mapped(sample(noodleThickness,
                blockX, modernY, blockZ, 1.0, 1.0), -0.05, -0.1);
        double ridgeA = Math.abs(sample(noodleRidgeA,
                blockX, modernY, blockZ, 2.6666666666666665, 2.6666666666666665));
        double ridgeB = Math.abs(sample(noodleRidgeB,
                blockX, modernY, blockZ, 2.6666666666666665, 2.6666666666666665));
        return noodleThicknessValue + 1.5 * Math.max(ridgeA, ridgeB);
    }

    private static double sample(Noise120 noise, double x, double y, double z,
                                 double xzScale, double yScale) {
        return noise.sample(x * xzScale, y * yScale, z * xzScale);
    }

    private static double mapped(double unitNoise, double min, double max) {
        return min + (unitNoise + 1.0) * 0.5 * (max - min);
    }

    private static double yGradient(double y, double fromY, double toY,
                                    double fromValue, double toValue) {
        if (y <= fromY) return fromValue;
        if (y >= toY) return toValue;
        return fromValue + (y - fromY) / (toY - fromY) * (toValue - fromValue);
    }

    private static double clampedMap(double value, double fromLow, double fromHigh,
                                     double toLow, double toHigh) {
        if (value <= fromLow) return toLow;
        if (value >= fromHigh) return toHigh;
        return lerp((value - fromLow) / (fromHigh - fromLow), toLow, toHigh);
    }

    private static double lerp(double amount, double first, double second) {
        return first + amount * (second - first);
    }

    public static double rarityType1(double value) {
        if (value < -0.75) return 0.5;
        if (value < -0.5) return 0.75;
        if (value < 0.5) return 1.0;
        return value < 0.75 ? 2.0 : 3.0;
    }

    public static double rarityType2(double value) {
        if (value < -0.5) return 0.75;
        if (value < 0.0) return 1.0;
        return value < 0.5 ? 1.5 : 2.0;
    }

    /** NoiseRouterData.f: abs(elevation + gradient) + thickness, then cube. */
    public static double spaghetti2dVerticalDensity(double elevationNoise, double thicknessNoise,
                                              double modernY) {
        double elevation = mapped(elevationNoise, 0.0, -8.0);
        double thickness = clamp(mapped(thicknessNoise, -0.6, -1.3), -1.0, 1.0);
        double vertical = Math.abs(elevation
                + yGradient(modernY, -64.0, 320.0, 8.0, -40.0)) + thickness;
        return vertical * vertical * vertical;
    }

    public static int modernToFuturaY(int modernY) { return modernY + MODERN_TO_FUTURA_OFFSET; }
    public static int futuraToModernY(int futuraY) { requireY(futuraY); return futuraY - MODERN_TO_FUTURA_OFFSET; }
    public static boolean isLogicalY(int futuraY) { return futuraY >= FUTURA_MIN_Y && futuraY <= FUTURA_MAX_Y; }

    private static void requireY(int futuraY) {
        if (!isLogicalY(futuraY))
            throw new IllegalArgumentException("Futura Y must be in 0..383: " + futuraY);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static final class Sample {
        public final double cheese;
        public final double spaghetti;
        public final double entrance;
        public final double noodle;
        public final double pillar;
        private final double preInterpolatedDensity;
        /** Density after the vanilla cave/noodle branch. Positive is solid. */
        public final double routedDensity;
        /** Positive means the routed 1.20.2 density is air. */
        public final double carveDensity;

        private Sample(double cheese, double spaghetti, double entrance, double noodle,
                       double pillar,double preInterpolatedDensity,double routedDensity) {
            this.cheese = cheese;
            this.spaghetti = spaghetti;
            this.entrance = entrance;
            this.noodle = noodle;
            this.pillar = pillar;
            this.preInterpolatedDensity=preInterpolatedDensity;
            this.routedDensity = routedDensity;
            this.carveDensity = -routedDensity;
        }

        public boolean shouldCarve() { return routedDensity < 0.0; }
    }

    private static final class EntranceSample {
        final double raw, roughness, spaghetti3d, combined;
        EntranceSample(double raw, double roughness, double spaghetti3d, double combined) {
            this.raw = raw;
            this.roughness = roughness;
            this.spaghetti3d = spaghetti3d;
            this.combined = combined;
        }
    }
}
