package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Standalone fixed-seed/invariant gate; this class has no Minecraft dependency. */
public final class FuturaDensityValidation120 {
    private static final long SEED = 0x1202F07A5EEDL;
    /** Updated only when the local 1.20.2 NoiseRouterData/TerrainProvider reference changes. */
    private static final long EXPECTED_HASH = 0x2399BD6880AEDD94L;

    private FuturaDensityValidation120() { }

    public static long validateOrThrow() {
        require(FuturaMountainDensityRouter120.modernToFuturaY(-64) == 0, "modern -64 mapping");
        require(FuturaMountainDensityRouter120.modernToFuturaY(319) == 383, "modern 319 mapping");
        require(FuturaMountainDensityRouter120.modernToFuturaY(320) == 384, "exclusive upper mapping");
        require(FuturaMountainDensityRouter120.futuraToModernY(0) == -64, "inverse Y mapping");
        require(FuturaMountainDensityRouter120.isLogicalY(0), "logical lower bound");
        require(FuturaMountainDensityRouter120.isLogicalY(383), "logical upper bound");
        require(!FuturaMountainDensityRouter120.isLogicalY(-1), "negative Y rejected");
        require(!FuturaMountainDensityRouter120.isLogicalY(384), "Y above 383 rejected");

        validatePrimitiveFunctions();
        validateNoiseChunkReuse();
        FuturaMountainDensityRouter120 first = new FuturaMountainDensityRouter120(SEED);
        FuturaMountainDensityRouter120 duplicate = new FuturaMountainDensityRouter120(SEED);
        FuturaMountainDensityRouter120 other = new FuturaMountainDensityRouter120(SEED + 1L);
        MutableDensityContext120 context = new MutableDensityContext120();
        long hash = 0xCBF29CE484222325L;
        boolean differentSeedObserved = false;
        int[] xs = {-4097, -513, -1, 0, 17, 511, 4096};
        int[] ys = {0, 1, 63, 127, 192, 255, 320, 382, 383};
        int[] zs = {-2049, -17, 0, 31, 1025};
        for (int x : xs) for (int z : zs) for (int y : ys) {
            context.set(x, y, z);
            checkRange(first.continents().sample(context), -1.0, 1.0, "continents");
            checkRange(first.erosion().sample(context), -1.0, 1.0, "erosion");
            checkRange(first.ridges().sample(context), -1.0, 1.0, "ridges");
            checkRange(first.weirdness().sample(context), -1.0, 1.0, "weirdness");
            checkRange(first.factor().sample(context), 0.0, 8.0, "vanilla spline factor");
            checkRange(first.jaggedness().sample(context), 0.0, 2.0, "vanilla spline jaggedness");
            double density = first.finalDensity().sample(context);
            double repeated = duplicate.finalDensity().sample(context);
            require(Double.doubleToLongBits(density) == Double.doubleToLongBits(repeated), "same seed determinism");
            require(Double.isFinite(density), "finite final density");
            differentSeedObserved |= Double.doubleToLongBits(density)
                    != Double.doubleToLongBits(other.finalDensity().sample(context));
            hash ^= Double.doubleToLongBits(density);
            hash *= 0x100000001B3L;
        }
        require(differentSeedObserved, "seed must affect density");
        context.set(73, 0, -91);
        require(first.depth().sample(context) > 0.0, "bottom depth must be solid-biased");
        context.set(73, 383, -91);
        require(first.depth().sample(context) < 0.0, "top depth must be air-biased");
        require(hash == EXPECTED_HASH, "fixed-seed density hash changed: expected 0x"
                + Long.toHexString(EXPECTED_HASH) + ", actual 0x" + Long.toHexString(hash));
        return hash;
    }

    private static void validatePrimitiveFunctions() {
        MutableDensityContext120 context = new MutableDensityContext120();
        DensityFunction120 gradient = DensityFunctions120.yGradient(0, 100, 2.0, -2.0);
        require(gradient.sample(context.set(0, 0, 0)) == 2.0, "gradient lower clamp");
        require(gradient.sample(context.set(0, 50, 0)) == 0.0, "gradient midpoint");
        require(gradient.sample(context.set(0, 100, 0)) == -2.0, "gradient upper clamp");

        CountingFunction onceSource = new CountingFunction(false);
        DensityFunction120 once = DensityFunctions120.cacheOnce(onceSource);
        once.sample(context.set(1, 2, 3)); once.sample(context.set(1, 2, 3));
        require(onceSource.calls == 1, "cache-once miss");
        once.sample(context.set(1, 3, 3));
        require(onceSource.calls == 2, "cache-once invalidation");

        CountingFunction columnSource = new CountingFunction(true);
        DensityFunction120 column = DensityFunctions120.cache2d(columnSource);
        column.sample(context.set(-2, 1, 8)); column.sample(context.set(-2, 400, 8));
        require(columnSource.calls == 1, "cache-2d column reuse");
        column.sample(context.set(-1, 400, 8));
        require(columnSource.calls == 2, "cache-2d invalidation");

        DensityFunction120 linear = new DensityFunction120() {
            @Override public double sample(DensityContext120 c) {
                return c.blockX() * 2.0 + c.blockY() * 3.0 - c.blockZ() * 4.0;
            }
        };
        DensityFunction120 interpolated = DensityFunctions120.interpolated(linear, 4, 8);
        double expected = linear.sample(context.set(-3, 5, 6));
        double actual = interpolated.sample(context);
        require(Math.abs(expected - actual) < 1.0E-12, "trilinear interpolation including negative cells");

        VanillaCubicSpline120.Coordinate xCoordinate=new VanillaCubicSpline120.Coordinate(){
            @Override public float apply(DensityContext120 c){return c.blockX()*0.25F;}
        };
        VanillaCubicSpline120 spline=VanillaCubicSpline120.builder(xCoordinate)
                .add(0.0F,0.0F,1.0F).add(1.0F,1.0F,1.0F).build();
        require(spline.sample(context.set(-4,0,0))==-1.0F,"spline lower extrapolation");
        require(spline.sample(context.set(1,0,0))==0.25F,"spline Hermite interpolation");
        require(spline.sample(context.set(8,0,0))==2.0F,"spline upper extrapolation");

        DensityFunction120 signed=new DensityFunction120(){
            @Override public double sample(DensityContext120 c){return c.blockY();}
        };
        require(DensityFunctions120.halfNegative(signed).sample(context.set(0,-2,0))==-1.0D,
                "half-negative transform");
        require(DensityFunctions120.quarterNegative(signed).sample(context.set(0,2,0))==2.0D,
                "quarter-negative positive pass-through");
    }

    private static void validateNoiseChunkReuse(){
        FuturaMountainDensityRouter120 scalar=new FuturaMountainDensityRouter120(SEED);
        FuturaMountainDensityRouter120 batched=new FuturaMountainDensityRouter120(SEED);
        NoiseChunk120 chunk=batched.createNoiseChunk(-3,5,256);
        MutableDensityContext120 context=new MutableDensityContext120();
        double[] column=new double[256];
        for(int localX=0;localX<16;localX++)for(int localZ=0;localZ<16;localZ++){
            chunk.fillColumn(localX,localZ,1,column);
            int worldX=(-3<<4)+localX,worldZ=(5<<4)+localZ;
            for(int y=1;y<256;y++){
                double expected=scalar.finalDensity().sample(context.set(worldX,y,worldZ));
                require(Double.doubleToLongBits(column[y])==Double.doubleToLongBits(expected),
                        "NoiseChunk/scalar mismatch at "+worldX+","+y+","+worldZ);
            }
        }
        NoiseChunk120.Statistics statistics=chunk.statistics();
        require(statistics.evaluatedNodes==5*33*5,"NoiseChunk shared-node grid size");
        require(statistics.interpolatedSamples==16L*16L*255L,"NoiseChunk block sample count");
        require(statistics.reusedSamples()>64000L,"NoiseChunk did not reuse shared grid nodes");
    }

    private static void checkRange(double value, double min, double max, String name) {
        require(Double.isFinite(value) && value >= min - 1.0E-12 && value <= max + 1.0E-12, name + " range");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("Stage-10 validation failed: " + message);
    }

    private static final class CountingFunction implements DensityFunction120 {
        private final boolean ignoreY;
        private int calls;
        private CountingFunction(boolean ignoreY) { this.ignoreY = ignoreY; }
        @Override public double sample(DensityContext120 context) {
            calls++;
            return context.blockX() + context.blockZ() + (ignoreY ? 0 : context.blockY());
        }
    }

    public static void main(String[] args) {
        long hash = validateOrThrow();
        System.out.println("Stage 10 density validation OK; fixed-seed hash=" + hash + "L (0x" + Long.toHexString(hash) + ")");
        System.out.println(surfaceStatistics());
    }

    private static String surfaceStatistics() {
        FuturaMountainDensityRouter120 router = new FuturaMountainDensityRouter120(SEED);
        MutableDensityContext120 context = new MutableDensityContext120();
        int minimum = Integer.MAX_VALUE, maximum = Integer.MIN_VALUE;
        int oceanColumns = 0, landColumns = 0;
        for (int z = -4096; z <= 4096; z += 256) for (int x = -4096; x <= 4096; x += 256) {
            int surface = 0;
            for (int y = FuturaMountainDensityRouter120.MAX_Y; y > 0; y--) {
                if (router.finalDensity().sample(context.set(x, y, z)) > 0.0D) {
                    surface = y;
                    break;
                }
            }
            minimum = Math.min(minimum, surface);
            maximum = Math.max(maximum, surface);
            if (surface < FuturaMountainDensityRouter120.SEA_LEVEL) oceanColumns++;
            else landColumns++;
        }
        require(oceanColumns > 0 && landColumns > 0, "surface fixture needs land and ocean");
        require(maximum - minimum >= 48, "surface fixture lacks 1.18+ vertical relief: min="
                + minimum + ", max=" + maximum + ", ocean=" + oceanColumns + ", land=" + landColumns);
        return "Surface fixture OK; min=" + minimum + ", max=" + maximum
                + ", ocean=" + oceanColumns + ", land=" + landColumns;
    }
}
