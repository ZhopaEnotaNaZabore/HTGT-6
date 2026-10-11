package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Java 8 implementation of the xoroshiro128++ generator used by modern Minecraft. */
public final class Xoroshiro128PlusPlus120 {
    private static final long SILVER_RATIO_64 = 7640891576956012809L;
    private static final long GOLDEN_RATIO_64 = -7046029254386353131L;

    private long seedLo;
    private long seedHi;

    public Xoroshiro128PlusPlus120(long seed) {
        this.seedLo = seedLo(seed);
        this.seedHi = seedHi(seed);
        ensureNonZeroState();
    }

    public Xoroshiro128PlusPlus120(long seedLo, long seedHi) {
        this.seedLo = seedLo;
        this.seedHi = seedHi;
        ensureNonZeroState();
    }

    private void ensureNonZeroState() {
        if ((seedLo | seedHi) == 0L) {
            seedLo = GOLDEN_RATIO_64;
            seedHi = SILVER_RATIO_64;
        }
    }

    public long nextLong() {
        long lo = seedLo;
        long hi = seedHi;
        long result = Long.rotateLeft(lo + hi, 17) + lo;
        hi ^= lo;
        seedLo = Long.rotateLeft(lo, 49) ^ hi ^ (hi << 21);
        seedHi = Long.rotateLeft(hi, 28);
        return result;
    }

    public int nextInt() {
        return (int) nextLong();
    }

    /** Lemire's unbiased bounded integer, as used by the 1.20 xoroshiro source. */
    public int nextInt(int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        long product = Integer.toUnsignedLong(nextInt()) * (long) bound;
        long low = product & 0xFFFFFFFFL;
        if (low < bound) {
            long threshold = (0x100000000L - bound) % bound;
            while (low < threshold) {
                product = Integer.toUnsignedLong(nextInt()) * (long) bound;
                low = product & 0xFFFFFFFFL;
            }
        }
        return (int) (product >>> 32);
    }

    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    public float nextFloat() { return (nextLong() >>> 40) * 0x1.0p-24F; }

    public static long mixStafford13(long value) {
        value = (value ^ value >>> 30) * -4658895280553007687L;
        value = (value ^ value >>> 27) * -7723592293110705685L;
        return value ^ value >>> 31;
    }

    static long seedLo(long seed) {
        return mixStafford13(seed ^ SILVER_RATIO_64);
    }

    static long seedHi(long seed) {
        long lo = seed ^ SILVER_RATIO_64;
        return mixStafford13(lo + GOLDEN_RATIO_64);
    }
}
