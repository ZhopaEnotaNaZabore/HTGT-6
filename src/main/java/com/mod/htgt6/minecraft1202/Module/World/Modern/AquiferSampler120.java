package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.MutableDensityContext120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.Noise120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.NoiseParameters120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.VanillaNormalNoiseFactory120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.PositionalRandomFactory120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.Xoroshiro128PlusPlus120;

import java.util.HashMap;
import java.util.Map;

/** Scalar Java-8 port of Minecraft 1.20.2 Aquifer.NoiseBasedAquifer. */
public final class AquiferSampler120 {
    public static final int DEFAULT_SEA_LEVEL = CaveDensityRuntime120.modernToFuturaY(63);
    public static final int DEFAULT_LAVA_LEVEL = CaveDensityRuntime120.modernToFuturaY(-54);

    private static final int[][] SURFACE_SAMPLING_OFFSETS = {
            {0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1},
            {-3, 0}, {-2, 0}, {-1, 0}, {1, 0},
            {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
    };

    public enum Decision { SOLID, AIR, WATER, LAVA }

    private final Noise120 barrierNoise;
    private final Noise120 floodednessNoise;
    private final Noise120 spreadNoise;
    private final Noise120 lavaNoise;
    private final PositionalRandomFactory120 positionalRandom;
    private final FuturaMountainDensityRouter120 terrain;
    private final MutableDensityContext120 terrainContext = new MutableDensityContext120();
    private final Map<CellKey, CellCenter> centers = new HashMap<CellKey, CellCenter>();
    private final Map<CellKey, FluidStatus> statuses = new HashMap<CellKey, FluidStatus>();
    private final Map<ColumnKey, Integer> surfaces = new HashMap<ColumnKey, Integer>();
    private final int seaLevelModern;
    private final int lavaLevelModern;
    private final int maxFuturaY;

    public AquiferSampler120(long worldSeed) {
        this(worldSeed, DEFAULT_SEA_LEVEL, DEFAULT_LAVA_LEVEL,
                new FuturaMountainDensityRouter120(worldSeed), CaveDensityRuntime120.FUTURA_MAX_Y);
    }

    public AquiferSampler120(long worldSeed, int seaLevel, int lavaLevel) {
        this(worldSeed, seaLevel, lavaLevel, new FuturaMountainDensityRouter120(worldSeed),
                CaveDensityRuntime120.FUTURA_MAX_Y);
    }

    public AquiferSampler120(long worldSeed, int seaLevel, int lavaLevel,
                             FuturaMountainDensityRouter120 terrain) {
        this(worldSeed, seaLevel, lavaLevel, terrain, CaveDensityRuntime120.FUTURA_MAX_Y);
    }

    public AquiferSampler120(long worldSeed, int seaLevel, int lavaLevel,
                             FuturaMountainDensityRouter120 terrain, int maxFuturaY) {
        if (!CaveDensityRuntime120.isLogicalY(seaLevel)
                || !CaveDensityRuntime120.isLogicalY(lavaLevel) || lavaLevel > seaLevel)
            throw new IllegalArgumentException("Aquifer levels must satisfy 0 <= lava <= sea <= 383");
        if (terrain == null) throw new NullPointerException("terrain");
        if (!CaveDensityRuntime120.isLogicalY(maxFuturaY))
            throw new IllegalArgumentException("Aquifer maximum Y must be in 0..383");
        this.seaLevelModern = CaveDensityRuntime120.futuraToModernY(seaLevel);
        this.lavaLevelModern = CaveDensityRuntime120.futuraToModernY(lavaLevel);
        this.terrain = terrain;
        this.maxFuturaY = maxFuturaY;
        VanillaNormalNoiseFactory120 noises = new VanillaNormalNoiseFactory120(worldSeed);
        barrierNoise = noises.create(NoiseParameters120.AQUIFER_BARRIER);
        floodednessNoise = noises.create(NoiseParameters120.AQUIFER_FLUID_LEVEL_FLOODEDNESS);
        spreadNoise = noises.create(NoiseParameters120.AQUIFER_FLUID_LEVEL_SPREAD);
        lavaNoise = noises.create(NoiseParameters120.AQUIFER_LAVA);
        positionalRandom = new PositionalRandomFactory120(worldSeed).fork("minecraft:aquifer");
    }

    public Result sample(int blockX, int futuraY, int blockZ, boolean carved) {
        if (!CaveDensityRuntime120.isLogicalY(futuraY))
            throw new IllegalArgumentException("Futura Y must be in 0..383: " + futuraY);
        if (!carved) return new Result(Decision.SOLID, Integer.MIN_VALUE, 0.0);
        return sampleDensity(blockX, CaveDensityRuntime120.futuraToModernY(futuraY), blockZ, -1.0);
    }

    public Result sample(int blockX, int futuraY, int blockZ,
                         CaveDensityRuntime120.Sample caveSample) {
        if (caveSample == null) throw new IllegalArgumentException("caveSample must not be null");
        if (!caveSample.shouldCarve()) return new Result(Decision.SOLID, Integer.MIN_VALUE, 0.0);
        return sampleDensity(blockX, CaveDensityRuntime120.futuraToModernY(futuraY), blockZ,
                -caveSample.carveDensity);
    }

    /** Mirrors Aquifer.computeSubstance: positive density remains solid. */
    public Result sampleDensity(int blockX, int modernY, int blockZ, double density) {
        if (density > 0.0) return new Result(Decision.SOLID, Integer.MIN_VALUE, 0.0);

        FluidStatus global = globalFluid(modernY);
        if (global.at(modernY) == Decision.LAVA)
            return result(global, modernY, 0.0);

        int gridX = Math.floorDiv(blockX - 5, 16);
        int gridY = Math.floorDiv(modernY + 1, 12);
        int gridZ = Math.floorDiv(blockZ - 5, 16);
        int firstDistance = Integer.MAX_VALUE;
        int secondDistance = Integer.MAX_VALUE;
        int thirdDistance = Integer.MAX_VALUE;
        CellCenter first = null, second = null, third = null;

        for (int dx = 0; dx <= 1; ++dx) {
            for (int dy = -1; dy <= 1; ++dy) {
                for (int dz = 0; dz <= 1; ++dz) {
                    CellCenter center = center(gridX + dx, gridY + dy, gridZ + dz);
                    int ox = center.x - blockX, oy = center.y - modernY, oz = center.z - blockZ;
                    int distance = ox * ox + oy * oy + oz * oz;
                    if (firstDistance >= distance) {
                        third = second; second = first; first = center;
                        thirdDistance = secondDistance; secondDistance = firstDistance; firstDistance = distance;
                    } else if (secondDistance >= distance) {
                        third = second; second = center;
                        thirdDistance = secondDistance; secondDistance = distance;
                    } else if (thirdDistance >= distance) {
                        third = center; thirdDistance = distance;
                    }
                }
            }
        }

        FluidStatus firstStatus = status(first);
        double firstSecond = similarity(firstDistance, secondDistance);
        Decision nearest = firstStatus.at(modernY);
        if (firstSecond <= 0.0) return result(firstStatus, modernY, 0.0);

        FluidStatus secondStatus = status(second);
        double barrier = Double.NaN;
        double pressure = firstSecond * pressure(blockX, modernY, blockZ,
                firstStatus, secondStatus, barrier);
        if (density + pressure > 0.0)
            return new Result(Decision.SOLID, Integer.MIN_VALUE, pressure);

        FluidStatus thirdStatus = status(third);
        double firstThird = similarity(firstDistance, thirdDistance);
        if (firstThird > 0.0) {
            double value = firstSecond * firstThird * pressure(blockX, modernY, blockZ,
                    firstStatus, thirdStatus, barrier);
            if (density + value > 0.0)
                return new Result(Decision.SOLID, Integer.MIN_VALUE, value);
        }

        double secondThird = similarity(secondDistance, thirdDistance);
        if (secondThird > 0.0) {
            double value = firstSecond * secondThird * pressure(blockX, modernY, blockZ,
                    secondStatus, thirdStatus, barrier);
            if (density + value > 0.0)
                return new Result(Decision.SOLID, Integer.MIN_VALUE, value);
        }
        return new Result(nearest, toFuturaLevel(firstStatus.fluidLevel), pressure);
    }

    private double pressure(int x, int y, int z, FluidStatus first, FluidStatus second,
                            double unusedBarrierCache) {
        Decision firstFluid = first.at(y), secondFluid = second.at(y);
        if ((firstFluid == Decision.LAVA && secondFluid == Decision.WATER)
                || (firstFluid == Decision.WATER && secondFluid == Decision.LAVA)) return 2.0;
        int levelDifference = Math.abs(first.fluidLevel - second.fluidLevel);
        if (levelDifference == 0) return 0.0;

        double midpoint = 0.5 * (first.fluidLevel + second.fluidLevel);
        double distanceFromMidpoint = y + 0.5 - midpoint;
        double halfDifference = levelDifference / 2.0;
        double remaining = halfDifference - Math.abs(distanceFromMidpoint);
        double shaped;
        if (distanceFromMidpoint > 0.0) {
            shaped = remaining > 0.0 ? remaining / 1.5 : remaining / 2.5;
        } else {
            double lower = 3.0 + remaining;
            shaped = lower > 0.0 ? lower / 3.0 : lower / 10.0;
        }
        double barrier = shaped >= -2.0 && shaped <= 2.0
                ? barrierNoise.sample(x, y * 0.5, z) : 0.0;
        return 2.0 * (barrier + shaped);
    }

    private CellCenter center(int gridX, int gridY, int gridZ) {
        CellKey key = new CellKey(gridX, gridY, gridZ);
        CellCenter cached = centers.get(key);
        if (cached != null) return cached;
        Xoroshiro128PlusPlus120 random = positionalRandom.at(gridX, gridY, gridZ);
        CellCenter created = new CellCenter(gridX * 16 + random.nextInt(10),
                gridY * 12 + random.nextInt(9), gridZ * 16 + random.nextInt(10), key);
        trimCaches();
        centers.put(key, created);
        return created;
    }

    private FluidStatus status(CellCenter center) {
        FluidStatus cached = statuses.get(center.key);
        if (cached != null) return cached;
        FluidStatus created = computeStatus(center.x, center.y, center.z);
        statuses.put(center.key, created);
        return created;
    }

    private FluidStatus computeStatus(int x, int y, int z) {
        FluidStatus global = globalFluid(y);
        int minimumSurface = Integer.MAX_VALUE;
        int upper = y + 12, lower = y - 12;
        boolean surfaceFluid = false;
        for (int[] offset : SURFACE_SAMPLING_OFFSETS) {
            int sampleX = x + offset[0] * 4;
            int sampleZ = z + offset[1] * 4;
            int surface = preliminarySurfaceModern(sampleX, sampleZ);
            int surfacePlusEight = surface + 8;
            boolean origin = offset[0] == 0 && offset[1] == 0;
            if (origin && lower > surfacePlusEight) return global;
            boolean reachesSurface = upper > surfacePlusEight;
            if (reachesSurface || origin) {
                FluidStatus surfaceStatus = globalFluid(surfacePlusEight);
                if (surfaceStatus.at(surfacePlusEight) != Decision.AIR) {
                    if (origin) surfaceFluid = true;
                    if (reachesSurface) return surfaceStatus;
                }
            }
            minimumSurface = Math.min(minimumSurface, surface);
        }

        int fluidLevel;
        terrainContext.set(x, y + MODERN_TO_FUTURA_OFFSET, z);
        boolean deepDark = isDeepDarkRegion(terrain.erosion().sample(terrainContext),
                terrain.depth().sample(terrainContext));
        if (deepDark) {
            fluidLevel = Integer.MIN_VALUE;
        } else {
            int distanceToSurface = minimumSurface + 8 - y;
            double surfaceInfluence = surfaceFluid
                    ? clampedMap(distanceToSurface, 0.0, 64.0, 1.0, 0.0) : 0.0;
            double floodedness = clamp(floodednessNoise.sample(x, y * 0.67, z), -1.0, 1.0);
            double upperThreshold = clampedMap(surfaceInfluence, 1.0, 0.0, -0.3, 0.8);
            double lowerThreshold = clampedMap(surfaceInfluence, 1.0, 0.0, -0.8, 0.4);
            if (floodedness - upperThreshold > 0.0) fluidLevel = global.fluidLevel;
            else if (floodedness - lowerThreshold > 0.0)
                fluidLevel = randomizedFluidLevel(x, y, z, minimumSurface);
            else fluidLevel = Integer.MIN_VALUE;
        }
        return new FluidStatus(fluidLevel, fluidType(x, y, z, global, fluidLevel));
    }

    public static boolean isDeepDarkRegion(double erosion,double depth){
        return erosion < -0.22499999403953552D && depth > 0.8999999761581421D;
    }

    private int randomizedFluidLevel(int x, int y, int z, int maximumSurface) {
        int gridX = Math.floorDiv(x, 16), gridY = Math.floorDiv(y, 40), gridZ = Math.floorDiv(z, 16);
        int middle = gridY * 40 + 20;
        double spread = spreadNoise.sample(gridX, gridY * 0.7142857142857143, gridZ) * 10.0;
        int quantized = (int) Math.floor(spread / 3.0) * 3;
        return Math.min(maximumSurface, middle + quantized);
    }

    private Decision fluidType(int x, int y, int z, FluidStatus global, int fluidLevel) {
        Decision type = global.type;
        if (fluidLevel <= -10 && fluidLevel != Integer.MIN_VALUE && global.type != Decision.LAVA) {
            double lava = lavaNoise.sample(Math.floorDiv(x, 64), Math.floorDiv(y, 40), Math.floorDiv(z, 64));
            if (Math.abs(lava) > 0.3) type = Decision.LAVA;
        }
        return type;
    }

    private FluidStatus globalFluid(int modernY) {
        return modernY < Math.min(lavaLevelModern, seaLevelModern)
                ? new FluidStatus(lavaLevelModern, Decision.LAVA)
                : new FluidStatus(seaLevelModern, Decision.WATER);
    }

    public int preliminarySurfaceModern(int x, int z) {
        ColumnKey key = new ColumnKey(x, z);
        Integer cached = surfaces.get(key);
        if (cached != null) return cached.intValue();
        int surface = terrain.preliminarySurfaceLevel(x, z, maxFuturaY);
        // A clipped 1.7 column has no representable surface above maxFuturaY.
        // Treat its ceiling as the preliminary surface instead of leaking the
        // logical 384-block result (or Integer.MAX_VALUE) into aquifer pressure.
        if (surface == Integer.MAX_VALUE || surface > maxFuturaY) surface = maxFuturaY;
        int value = surface - MODERN_TO_FUTURA_OFFSET;
        surfaces.put(key, Integer.valueOf(value));
        return value;
    }

    private void trimCaches() {
        if (centers.size() < 16384) return;
        centers.clear(); statuses.clear(); surfaces.clear();
    }

    private static Result result(FluidStatus status, int y, double pressure) {
        return new Result(status.at(y), toFuturaLevel(status.fluidLevel), pressure);
    }

    private static int toFuturaLevel(int modernLevel) {
        return modernLevel == Integer.MIN_VALUE ? Integer.MIN_VALUE : modernLevel + MODERN_TO_FUTURA_OFFSET;
    }

    private static double similarity(int firstDistance, int secondDistance) {
        return 1.0 - Math.abs(secondDistance - firstDistance) / 25.0;
    }

    private static double clampedMap(double value, double fromA, double fromB, double toA, double toB) {
        if (fromA == fromB) return value < fromA ? toA : toB;
        double t = clamp((value - fromA) / (fromB - fromA), 0.0, 1.0);
        return toA + t * (toB - toA);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final int MODERN_TO_FUTURA_OFFSET = CaveDensityRuntime120.MODERN_TO_FUTURA_OFFSET;

    private static final class FluidStatus {
        private final int fluidLevel;
        private final Decision type;
        private FluidStatus(int fluidLevel, Decision type) { this.fluidLevel = fluidLevel; this.type = type; }
        private Decision at(int y) { return y < fluidLevel ? type : Decision.AIR; }
    }

    private static final class CellCenter {
        private final int x, y, z;
        private final CellKey key;
        private CellCenter(int x, int y, int z, CellKey key) {
            this.x = x; this.y = y; this.z = z; this.key = key;
        }
    }

    private static final class CellKey {
        private final int x, y, z;
        private CellKey(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
        @Override public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof CellKey)) return false;
            CellKey key = (CellKey) other;
            return x == key.x && y == key.y && z == key.z;
        }
        @Override public int hashCode() { return (x * 31 + y) * 31 + z; }
    }

    private static final class ColumnKey {
        private final int x, z;
        private ColumnKey(int x, int z) { this.x = x; this.z = z; }
        @Override public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof ColumnKey)) return false;
            ColumnKey key = (ColumnKey) other;
            return x == key.x && z == key.z;
        }
        @Override public int hashCode() { return x * 31 + z; }
    }

    public static final class Result {
        public final Decision decision;
        public final int fluidLevel;
        public final double pressure;
        private Result(Decision decision, int fluidLevel, double pressure) {
            this.decision = decision; this.fluidLevel = fluidLevel; this.pressure = pressure;
        }
    }
}
