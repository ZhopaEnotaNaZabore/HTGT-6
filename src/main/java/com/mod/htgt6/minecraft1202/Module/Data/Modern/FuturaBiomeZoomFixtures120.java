package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeZoomer120;

/** Fixed block-boundary cases transcribed from the local 1.20.2 BiomeManager reference. */
public final class FuturaBiomeZoomFixtures120 {
    private static final long SEED=0x1234ABCD5678EF90L;
    private static final long EXPECTED_OBFUSCATED_SEED=0xCA05E92301A66D32L;
    private static final int[][] CASES={
            // block x, block y, block z, expected quart x, quart y, quart z
            {0,0,0,-1,-1,-1},
            {1,0,1,0,0,0},
            {2,0,2,0,0,0},
            {3,0,3,0,0,0},
            {4,0,4,0,-1,1},
            {5,0,5,1,0,1},
            {6,0,6,1,-1,1},
            {7,0,7,1,-1,1},
            {8,0,8,1,0,2},
            {3,0,4,0,-1,1},
            {4,0,3,0,-1,0},
            {7,0,8,1,0,2},
            {8,0,7,1,-1,1},
            {-1,0,-1,-1,-1,-1},
            {-2,0,-2,-1,-1,-1},
            {-3,0,-3,-2,-1,-1},
            {-4,0,-4,-2,-1,-1},
            {-5,0,-5,-2,-1,-1},
            {-7,0,-8,-2,-1,-2},
            {-8,0,-7,-2,-1,-2},
            {-8,0,-8,-3,0,-2},
            {-9,0,-9,-3,0,-3},
            {-1,0,4,-1,0,1},
            {4,0,-1,0,-1,0}
    };
    private FuturaBiomeZoomFixtures120(){}
    public static void main(String[] arguments){
        FuturaBiomeZoomer120 zoomer=new FuturaBiomeZoomer120(SEED);
        if(FuturaBiomeZoomer120.obfuscateSeed(SEED)!=EXPECTED_OBFUSCATED_SEED)
            throw new IllegalStateException("BiomeManager SHA-256 seed obfuscation changed");
        for(int[] test:CASES){
            FuturaBiomeZoomer120.QuartPosition actual=zoomer.select(test[0],test[1],test[2]);
            if(actual.x!=test[3]||actual.y!=test[4]||actual.z!=test[5])throw new IllegalStateException(
                    "BiomeManager mismatch at "+test[0]+","+test[1]+","+test[2]+": expected "
                            +test[3]+","+test[4]+","+test[5]+", got "+actual.x+","+actual.y+","+actual.z);
        }
        System.out.println("Biome zoom fixture OK; cases="+CASES.length+", obfuscatedSeed=0x"
                +Long.toHexString(FuturaBiomeZoomer120.obfuscateSeed(SEED)));
    }
}
