package com.mod.htgt6.minecraft1202.Module.World.Modern;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Coordinate/name-derived random streams without shared mutable state. */
public final class PositionalRandomFactory120 {
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private final long seedLo;
    private final long seedHi;

    public PositionalRandomFactory120(long worldSeed) {
        this(new Xoroshiro128PlusPlus120(worldSeed));
    }

    public PositionalRandomFactory120(Xoroshiro128PlusPlus120 source) {
        this.seedLo = source.nextLong();
        this.seedHi = source.nextLong();
    }

    public Xoroshiro128PlusPlus120 at(int x, int y, int z) {
        long positionSeed = coordinateSeed(x, y, z);
        return new Xoroshiro128PlusPlus120(seedLo ^ positionSeed, seedHi);
    }

    public Xoroshiro128PlusPlus120 fromHashOf(String name) {
        try {
            byte[] hash = MessageDigest.getInstance("MD5").digest(name.getBytes(UTF_8));
            return new Xoroshiro128PlusPlus120(seedLo ^ readLong(hash, 0), seedHi ^ readLong(hash, 8));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("MD5 is required by the Java runtime", impossible);
        }
    }

    /** RandomState.getOrCreateRandomFactory: fromHashOf(key).forkPositional(). */
    public PositionalRandomFactory120 fork(String name) {
        return new PositionalRandomFactory120(fromHashOf(name));
    }

    public static long coordinateSeed(int x, int y, int z) {
        // Mth.getSeed deliberately overflows the X product as a 32-bit int.
        long value = (long)(x * 3129871) ^ (long) z * 116129781L ^ (long) y;
        value = value * value * 42317861L + value * 11L;
        return value >> 16;
    }

    private static long readLong(byte[] bytes, int offset) {
        long value = 0L;
        for (int i = 0; i < 8; i++) value = value << 8 | bytes[offset + i] & 255L;
        return value;
    }
}
