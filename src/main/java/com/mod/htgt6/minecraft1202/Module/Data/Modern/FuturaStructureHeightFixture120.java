package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;

import com.mod.htgt6.minecraft1202.Module.World.Bountiful.OceanMonumentGenerator;
import com.mod.htgt6.minecraft1202.Module.World.Frostburn.FrostburnFossilGenerator;
import com.mod.htgt6.minecraft1202.Module.World.VillageAndPillage.PillagerOutpostGenerator;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120;

/** Pure regression for translating absolute structure Y into Futura's 0..383 space. */
public final class FuturaStructureHeightFixture120 {
    public static void assertContract(){
        int shift=FuturaHeight120.Y_TRANSLATION;
        require(OceanMonumentGenerator.baseY(false)==39,"legacy monument Y");
        require(OceanMonumentGenerator.baseY(true)==39+shift,"Futura monument Y");
        require(PillagerOutpostGenerator.validSurfaceHeight(55,false)
                &&PillagerOutpostGenerator.validSurfaceHeight(110,false),"legacy outpost range");
        require(PillagerOutpostGenerator.validSurfaceHeight(110,true)
                &&PillagerOutpostGenerator.validSurfaceHeight(127,true)
                &&PillagerOutpostGenerator.validSurfaceHeight(300,true)
                &&!PillagerOutpostGenerator.validSurfaceHeight(384,true),"Futura outpost range");
        require(FrostburnFossilGenerator.fossilBaseY(384,8,30,9)==10,
                "Futura fossil min-build clamp");
        require(FrostburnFossilGenerator.fossilBaseY(384,8,350,0)==335,
                "Futura fossil high terrain");
        Object first=FuturaAncientCityGenerator120.layout(0x1202AC1L,136,-248);
        Object second=FuturaAncientCityGenerator120.layout(0x1202AC1L,136,-248);
        require(first==second&&FuturaAncientCityGenerator120.cachedLayoutCount()>=1,
                "Ancient City StructureStart layout cache");
    }
    public static void main(String[] ignored){assertContract();System.out.println("Futura structure heights OK");}
    private static void require(boolean value,String name){if(!value)throw new IllegalStateException(name);}
    private FuturaStructureHeightFixture120(){ }
}
