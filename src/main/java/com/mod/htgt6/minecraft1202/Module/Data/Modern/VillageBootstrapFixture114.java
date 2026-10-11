package com.mod.htgt6.minecraft1202.Module.Data.Modern;

/** Structure mapping now constructs real vanilla-derived building blocks. */
public final class VillageBootstrapFixture114 {
    public static void initialize(){try{
        com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;
        com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;
        com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;
        com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
        com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.initialize();
    }catch(Exception e){throw new IllegalStateException("Vanilla fixture bootstrap",e);}}
    private VillageBootstrapFixture114(){}
}
