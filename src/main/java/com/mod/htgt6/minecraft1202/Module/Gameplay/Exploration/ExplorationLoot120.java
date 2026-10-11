package com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration;
import java.util.Random;
/** Uniform base count plus the 1.20 looting_enchant round(uniform(0,1)*level). */
public final class ExplorationLoot120 {
    private ExplorationLoot120(){}
    public static int count(Random random,int maximum,int looting){
        int count=random.nextInt(maximum+1);
        return looting>0?count+Math.round(random.nextFloat()*looting):count;
    }
}
