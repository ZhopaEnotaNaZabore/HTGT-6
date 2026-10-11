package com.mod.htgt6.common.util;

import java.util.Random;

/** Pure, headless implementation of Minecraft 1.20.2 structure-set placement. */
public final class StructurePlacement120 {
    private static final long REGION_X_MULTIPLIER = 341873128712L;
    private static final long REGION_Z_MULTIPLIER = 132897987541L;
    private static final int LEGACY_BURIED_TREASURE_SALT = 10387320;
    private static final long EXPECTED_FINGERPRINT = 0x1e7437873b5592a3L;

    public static final Placement VILLAGE = randomSpread(34, 8, 10387312);
    public static final Placement ANCIENT_CITY = randomSpread(24, 8, 20083232);
    public static final Placement SHIPWRECK = randomSpread(24, 4, 165745295);
    /** pillager_outposts structure set in Minecraft 1.20.2. */
    public static final Placement PILLAGER_OUTPOST = randomSpread(32, 8, 165745296);
    public static final Placement TRAIL_RUINS = randomSpread(34, 8, 83469867);
    public static final Placement NETHER_FOSSIL = randomSpread(2, 1, 14357921);
    public static final Placement OCEAN_RUIN = randomSpread(20, 8, 14357621);
    public static final Placement IGLOO = randomSpread(32, 8, 14357618);
    public static final Placement WOODLAND_MANSION = triangularSpread(80, 20, 10387319);

    public static int[] candidate(long worldSeed, int regionX, int regionZ, Placement placement) {
        Random random = new Random(worldSeed + (long)regionX * REGION_X_MULTIPLIER
                + (long)regionZ * REGION_Z_MULTIPLIER + placement.salt);
        int range = placement.spacing - placement.separation;
        int offsetX = placement.triangular
                ? (random.nextInt(range) + random.nextInt(range)) / 2 : random.nextInt(range);
        int offsetZ = placement.triangular
                ? (random.nextInt(range) + random.nextInt(range)) / 2 : random.nextInt(range);
        return new int[] {regionX * placement.spacing + offsetX,
                regionZ * placement.spacing + offsetZ};
    }

    public static boolean isCandidate(long worldSeed, int chunkX, int chunkZ, Placement placement) {
        int regionX = floorDiv(chunkX, placement.spacing);
        int regionZ = floorDiv(chunkZ, placement.spacing);
        int[] candidate = candidate(worldSeed, regionX, regionZ, placement);
        return candidate[0] == chunkX && candidate[1] == chunkZ;
    }

    /** Random stream immediately after the two spread offsets were selected. */
    public static Random randomAfterCandidate(long worldSeed,int regionX,int regionZ,Placement placement) {
        Random random = new Random(worldSeed + (long)regionX * REGION_X_MULTIPLIER
                + (long)regionZ * REGION_Z_MULTIPLIER + placement.salt);
        int range=placement.spacing-placement.separation;
        random.nextInt(range);
        if(placement.triangular)random.nextInt(range);
        random.nextInt(range);
        if(placement.triangular)random.nextInt(range);
        return random;
    }

    /** StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_2 from 1.20.2. */
    public static boolean buriedTreasurePasses(long worldSeed, int chunkX, int chunkZ) {
        Random random = new Random(worldSeed + (long)chunkX * REGION_X_MULTIPLIER
                + (long)chunkZ * REGION_Z_MULTIPLIER + LEGACY_BURIED_TREASURE_SALT);
        return random.nextFloat() < 0.01F;
    }

    /** StructurePlacement.FrequencyReductionMethod.LEGACY_TYPE_1 used by outposts. */
    public static boolean pillagerOutpostFrequencyPasses(long worldSeed,int chunkX,int chunkZ){
        int regionX=chunkX>>4,regionZ=chunkZ>>4;
        Random random=new Random((long)(regionX^(regionZ<<4))^worldSeed);
        random.nextInt();
        return random.nextInt(5)==0;
    }

    /** Fast deterministic regression contract; has no Minecraft or Forge dependencies. */
    public static void assertContract() {
        long actual = fingerprint();
        if (EXPECTED_FINGERPRINT != 0L && actual != EXPECTED_FINGERPRINT) {
            throw new IllegalStateException("1.20.2 structure placement contract changed: expected "
                    + Long.toHexString(EXPECTED_FINGERPRINT) + ", got " + Long.toHexString(actual));
        }
    }

    public static long fingerprint() {
        Placement[] placements = {VILLAGE, ANCIENT_CITY, SHIPWRECK, OCEAN_RUIN,
                IGLOO, WOODLAND_MANSION};
        long hash = 0xcbf29ce484222325L;
        long[] seeds = {0L, 1L, -1L, 1234567890123456789L};
        for (Placement placement : placements) for (long seed : seeds) {
            for (int regionX = -4; regionX <= 4; regionX++) for (int regionZ = -4; regionZ <= 4; regionZ++) {
                int[] point = candidate(seed, regionX, regionZ, placement);
                hash = fnv(hash, point[0]);
                hash = fnv(hash, point[1]);
                long continuation=randomAfterCandidate(seed,regionX,regionZ,placement).nextLong();
                hash=fnv(hash,(int)continuation);
                hash=fnv(hash,(int)(continuation>>>32));
            }
        }
        for (long seed : seeds) for (int x = -64; x <= 64; x++) for (int z = -64; z <= 64; z++) {
            if (buriedTreasurePasses(seed, x, z)) {
                hash = fnv(hash, x);
                hash = fnv(hash, z);
            }
        }
        return hash;
    }

    public static void main(String[] ignored) {
        assertContract();
        System.out.println(Long.toHexString(fingerprint()));
    }

    public static int floorDiv(int value, int divisor) {
        int result = value / divisor;
        return value < 0 && value % divisor != 0 ? result - 1 : result;
    }

    /** Shared ownership boundary for structure slices written during chunk population. */
    public static boolean isOwnedByChunk(int blockX,int blockZ,int chunkX,int chunkZ){
        return (blockX>>4)==chunkX&&(blockZ>>4)==chunkZ;
    }

    private static long fnv(long hash, int value) {
        hash ^= value;
        return hash * 0x100000001b3L;
    }

    private static Placement randomSpread(int spacing, int separation, int salt) {
        return new Placement(spacing, separation, salt, false);
    }

    private static Placement triangularSpread(int spacing, int separation, int salt) {
        return new Placement(spacing, separation, salt, true);
    }

    public static final class Placement {
        public final int spacing;
        public final int separation;
        public final int salt;
        public final boolean triangular;

        private Placement(int spacing, int separation, int salt, boolean triangular) {
            if (spacing <= separation) throw new IllegalArgumentException("spacing must exceed separation");
            this.spacing = spacing;
            this.separation = separation;
            this.salt = salt;
            this.triangular = triangular;
        }
    }

    private StructurePlacement120() { }
}
