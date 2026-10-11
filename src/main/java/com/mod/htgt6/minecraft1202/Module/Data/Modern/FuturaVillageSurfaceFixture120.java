package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaSurfaceEstimator120;

/** Regression coordinates from the DEV 1 village reported below the real terrain surface. */
public final class FuturaVillageSurfaceFixture120 {
    private static final long REPORTED_SEED=-8092085661795312214L;
    // Bell cluster in the save is centred near 3044,53; the owning random-
    // spread start is chunk 191,3 and starts at the chunk minimum.
    private static final int START_X=191<<4;
    private static final int START_Z=3<<4;

    public static void assertContract(){
        FuturaSurfaceEstimator120.assertContract();
        int[] heights=FuturaSurfaceEstimator120.compareWithPreliminaryForFixture(REPORTED_SEED,START_X,START_Z);
        if(heights[0]!=130||heights[1]!=112||heights[0]<=heights[1])throw new IllegalStateException(
                "Reported village surface regression: terrain="+heights[0]+", preliminary="+heights[1]);
        System.out.println("Reported village surface: terrain="+heights[0]+", aquiferPreliminary="+heights[1]);
    }

    public static void main(String[] ignored){assertContract();}
    private FuturaVillageSurfaceFixture120(){ }
}
