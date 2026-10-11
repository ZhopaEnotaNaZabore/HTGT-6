package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Allocation-conscious building blocks used by the Futura density graph. */
public final class DensityFunctions120 {
    private DensityFunctions120() { }

    public static DensityFunction120 constant(double value) { return new Constant(value); }
    public static DensityFunction120 add(DensityFunction120 a, DensityFunction120 b) { return new Binary(a, b, ADD); }
    public static DensityFunction120 mul(DensityFunction120 a, DensityFunction120 b) { return new Binary(a, b, MUL); }
    public static DensityFunction120 min(DensityFunction120 a, DensityFunction120 b) { return new Binary(a, b, MIN); }
    public static DensityFunction120 max(DensityFunction120 a, DensityFunction120 b) { return new Binary(a, b, MAX); }
    public static DensityFunction120 clamp(DensityFunction120 input, double min, double max) {
        if (min > max) throw new IllegalArgumentException("min > max");
        return new Clamp(input, min, max);
    }
    public static DensityFunction120 abs(DensityFunction120 input) {
        if (input == null) throw new NullPointerException("input");
        return new Abs(input);
    }
    public static DensityFunction120 halfNegative(DensityFunction120 input){return new NegativeScale(input,0.5D);}
    public static DensityFunction120 quarterNegative(DensityFunction120 input){return new NegativeScale(input,0.25D);}
    public static DensityFunction120 yGradient(int fromY, int toY, double fromValue, double toValue) {
        if (fromY >= toY) throw new IllegalArgumentException("fromY must be below toY");
        return new YGradient(fromY, toY, fromValue, toValue);
    }
    public static DensityFunction120 noise(Noise120 noise, double xzScale, double yScale) {
        if (noise == null) throw new NullPointerException("noise");
        return new NoiseFunction(noise, xzScale, yScale);
    }
    public static DensityFunction120 cacheOnce(DensityFunction120 input) { return new CacheOnce(input); }
    public static DensityFunction120 cache2d(DensityFunction120 input) { return new Cache2D(input); }
    public static DensityFunction120 interpolated(DensityFunction120 input, int horizontalCell, int verticalCell) {
        if (horizontalCell <= 0 || verticalCell <= 0) throw new IllegalArgumentException("cell sizes must be positive");
        return new Interpolated(input, horizontalCell, verticalCell);
    }

    private static final int ADD = 0, MUL = 1, MIN = 2, MAX = 3;

    private static final class Constant implements DensityFunction120 {
        private final double value;
        private Constant(double value) { this.value = value; }
        @Override public double sample(DensityContext120 context) { return value; }
    }

    private static final class Binary implements DensityFunction120 {
        private final DensityFunction120 a, b;
        private final int operation;
        private Binary(DensityFunction120 a, DensityFunction120 b, int operation) {
            if (a == null || b == null) throw new NullPointerException("density operand");
            this.a = a; this.b = b; this.operation = operation;
        }
        @Override public double sample(DensityContext120 context) {
            double av = a.sample(context), bv = b.sample(context);
            switch (operation) {
                case ADD: return av + bv;
                case MUL: return av * bv;
                case MIN: return Math.min(av, bv);
                default: return Math.max(av, bv);
            }
        }
    }

    private static final class Clamp implements DensityFunction120 {
        private final DensityFunction120 input;
        private final double min, max;
        private Clamp(DensityFunction120 input, double min, double max) {
            if (input == null) throw new NullPointerException("input");
            this.input = input; this.min = min; this.max = max;
        }
        @Override public double sample(DensityContext120 context) {
            return Math.max(min, Math.min(max, input.sample(context)));
        }
    }

    private static final class Abs implements DensityFunction120 {
        private final DensityFunction120 input;
        private Abs(DensityFunction120 input) { this.input = input; }
        @Override public double sample(DensityContext120 context) { return Math.abs(input.sample(context)); }
    }

    private static final class NegativeScale implements DensityFunction120 {
        private final DensityFunction120 input;private final double scale;
        private NegativeScale(DensityFunction120 input,double scale){
            if(input==null)throw new NullPointerException("input");this.input=input;this.scale=scale;
        }
        @Override public double sample(DensityContext120 context){double value=input.sample(context);return value<0.0D?value*scale:value;}
    }

    private static final class YGradient implements DensityFunction120 {
        private final int fromY, toY;
        private final double fromValue, toValue;
        private YGradient(int fromY, int toY, double fromValue, double toValue) {
            this.fromY = fromY; this.toY = toY; this.fromValue = fromValue; this.toValue = toValue;
        }
        @Override public double sample(DensityContext120 context) {
            int y = context.blockY();
            if (y <= fromY) return fromValue;
            if (y >= toY) return toValue;
            double t = (y - fromY) / (double) (toY - fromY);
            return fromValue + t * (toValue - fromValue);
        }
    }

    private static final class NoiseFunction implements DensityFunction120 {
        private final Noise120 noise;
        private final double xzScale, yScale;
        private NoiseFunction(Noise120 noise, double xzScale, double yScale) {
            this.noise = noise; this.xzScale = xzScale; this.yScale = yScale;
        }
        @Override public double sample(DensityContext120 context) {
            return noise.sample(context.blockX() * xzScale, context.blockY() * yScale, context.blockZ() * xzScale);
        }
    }

    /** Last-position cache. One instance must belong to one generation worker. */
    private static final class CacheOnce implements DensityFunction120 {
        private final DensityFunction120 input;
        private int x, y, z;
        private double value;
        private boolean valid;
        private CacheOnce(DensityFunction120 input) {
            if (input == null) throw new NullPointerException("input");
            this.input = input;
        }
        @Override public double sample(DensityContext120 context) {
            int cx = context.blockX(), cy = context.blockY(), cz = context.blockZ();
            if (!valid || cx != x || cy != y || cz != z) {
                x = cx; y = cy; z = cz; value = input.sample(context); valid = true;
            }
            return value;
        }
    }

    /** Last-column cache for functions whose value is independent of Y. */
    private static final class Cache2D implements DensityFunction120 {
        private final DensityFunction120 input;
        private int x, z;
        private double value;
        private boolean valid;
        private Cache2D(DensityFunction120 input) {
            if (input == null) throw new NullPointerException("input");
            this.input = input;
        }
        @Override public double sample(DensityContext120 context) {
            int cx = context.blockX(), cz = context.blockZ();
            if (!valid || cx != x || cz != z) {
                x = cx; z = cz; value = input.sample(context); valid = true;
            }
            return value;
        }
    }

    /** Trilinear cell sampler retaining only the active cell's eight corner values. */
    private static final class Interpolated implements DensityFunction120 {
        private final DensityFunction120 input;
        private final int horizontalCell, verticalCell;
        private final MutableDensityContext120 corner = new MutableDensityContext120();
        private int cellX, cellY, cellZ;
        private boolean valid;
        private double n000, n001, n010, n011, n100, n101, n110, n111;
        private Interpolated(DensityFunction120 input, int horizontalCell, int verticalCell) {
            if (input == null) throw new NullPointerException("input");
            this.input = input; this.horizontalCell = horizontalCell; this.verticalCell = verticalCell;
        }
        @Override public double sample(DensityContext120 context) {
            int x = context.blockX(), y = context.blockY(), z = context.blockZ();
            int cx = Math.floorDiv(x, horizontalCell), cy = Math.floorDiv(y, verticalCell), cz = Math.floorDiv(z, horizontalCell);
            if (!valid || cx != cellX || cy != cellY || cz != cellZ) load(cx, cy, cz);
            double tx = Math.floorMod(x, horizontalCell) / (double) horizontalCell;
            double ty = Math.floorMod(y, verticalCell) / (double) verticalCell;
            double tz = Math.floorMod(z, horizontalCell) / (double) horizontalCell;
            double x00 = lerp(tx, n000, n100), x01 = lerp(tx, n001, n101);
            double x10 = lerp(tx, n010, n110), x11 = lerp(tx, n011, n111);
            return lerp(ty, lerp(tz, x00, x01), lerp(tz, x10, x11));
        }
        private void load(int cx, int cy, int cz) {
            cellX = cx; cellY = cy; cellZ = cz; valid = true;
            int x0 = cx * horizontalCell, x1 = x0 + horizontalCell;
            int y0 = cy * verticalCell, y1 = y0 + verticalCell;
            int z0 = cz * horizontalCell, z1 = z0 + horizontalCell;
            n000 = at(x0, y0, z0); n001 = at(x0, y0, z1);
            n010 = at(x0, y1, z0); n011 = at(x0, y1, z1);
            n100 = at(x1, y0, z0); n101 = at(x1, y0, z1);
            n110 = at(x1, y1, z0); n111 = at(x1, y1, z1);
        }
        private double at(int x, int y, int z) { return input.sample(corner.set(x, y, z)); }
        private static double lerp(double t, double a, double b) { return a + t * (b - a); }
    }
}
