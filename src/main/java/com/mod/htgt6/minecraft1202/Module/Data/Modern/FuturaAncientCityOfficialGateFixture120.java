package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;

import com.mod.htgt6.minecraft1202.Module.Data.Modern.AncientCityJigsawFixture120;

/** Fail-fast production gate for official pools and their configured feature. */
public final class FuturaAncientCityOfficialGateFixture120 {
    public static long assertContract(){
        if(!FuturaAncientCityGenerator120.usesOfficialJigsawProduction())
            throw new IllegalStateException("Ancient City production reverted to a non-jigsaw layout");
        if(!FuturaAncientCityGenerator120.supportsOfficialFeature("minecraft:sculk_patch_ancient_city"))
            throw new IllegalStateException("Ancient City sculk configured feature is not connected");
        return AncientCityJigsawFixture120.assertContract();
    }
    public static void main(String[] arguments){System.out.println(Long.toHexString(assertContract()));}
    private FuturaAncientCityOfficialGateFixture120(){ }
}
