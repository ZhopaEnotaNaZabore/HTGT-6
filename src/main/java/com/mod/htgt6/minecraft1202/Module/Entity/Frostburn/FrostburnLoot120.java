package com.mod.htgt6.minecraft1202.Module.Entity.Frostburn;

import java.util.Random;

/** Uniform set_count followed by looting_enchant, as in the 1.20.2 entity tables. */
public final class FrostburnLoot120 {
    private FrostburnLoot120() { }
    public static int count(Random random,int maximum,int looting) {
        int count=random.nextInt(maximum+1);
        if(looting>0)count+=Math.round(random.nextFloat()*looting);
        return count;
    }
    public static boolean rareHuskDrop(Random random,int looting) {
        return random.nextFloat()<.025F+.01F*Math.max(0,looting);
    }
}
