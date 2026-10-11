package com.mod.htgt6.minecraft1202.Module.World.Modern;

import java.util.Random;

/** 1.20.2 WorldgenRandom(XoroshiroRandomSource) through the legacy Random API.
 * WorldgenRandom consumes the HIGH bits via BitRandomSource, not the source's
 * Lemire nextInt implementation. Inherited Random.nextLong consumes two draws.
 */
public final class WorldgenRandom120 extends Random {
    private Xoroshiro128PlusPlus120 source;

    public WorldgenRandom120(long seed) { super(0L); setSeed(seed); }

    @Override public synchronized void setSeed(long seed) {
        super.setSeed(seed); // also clears Random's cached Gaussian
        source = new Xoroshiro128PlusPlus120(seed);
    }

    @Override protected int next(int bits) { return (int)(source.nextLong() >>> (64-bits)); }

    public static long decorationSeed(long worldSeed,int blockX,int blockZ) {
        Random random=new WorldgenRandom120(worldSeed);
        long x=random.nextLong()|1L,z=random.nextLong()|1L;
        return ((long)blockX*x+(long)blockZ*z)^worldSeed;
    }

    public static long featureSeed(long decorationSeed,int step,int index) {
        return decorationSeed+index+(long)(10000*step);
    }

    /** Carvers use WorldgenRandom(LegacyRandomSource), unlike decorators. */
    public static Random carver(long worldSeed,int index,int chunkX,int chunkZ) {
        long seed=worldSeed+index;
        Random random=new Random(seed);
        long x=random.nextLong(),z=random.nextLong();
        random.setSeed((long)chunkX*x^(long)chunkZ*z^seed);
        return random;
    }
}
