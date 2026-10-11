package com.mod.htgt6.minecraft1202.Module.Data.Combat;

import com.mod.htgt6.minecraft1202.Module.World.Combat.IglooNbtGenerator;

/** Regression checks for deterministic and persistent igloo starts. */
public final class IglooStructureFixture120 {
    public static void main(String[] ignored){
        IglooNbtGenerator.validateTemplates();
        IglooStructureData120.assertRoundTripFixture();
        long[] seeds={0L,1L,-1L,9876543212345L};
        for(long seed:seeds)for(int x=-70;x<=70;x+=7)for(int z=-70;z<=70;z+=11){
            int first=IglooNbtGenerator.planFingerprint(seed,x,z);
            if(first!=IglooNbtGenerator.planFingerprint(seed,x,z))
                throw new IllegalStateException("igloo plan consumes shared population RNG");
            int rotation=first&3,depth=first>>>3;
            if(rotation<0||rotation>3||depth<0||depth>11)
                throw new IllegalStateException("invalid igloo plan");
        }
        System.out.println("Igloo starts OK; all three NBT templates have unique positions/entities; deterministic plan and persistent one-start claim");
    }
    private IglooStructureFixture120(){}
}
