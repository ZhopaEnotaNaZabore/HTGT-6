package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaVillageGenerator120;

/** Headless contract for retained official village block-entity NBT and loot-table routing. */
public final class VillageTemplateNbtFixture120 {
    private static final long EXPECTED=0x3856b072beb2730aL;

    public static long assertContract(){
        FuturaVillageGenerator120.loadTemplateNbtForFixture();
        String[] tables={"village_armorer","village_butcher","village_cartographer",
                "village_desert_house","village_fisher","village_fletcher","village_mason",
                "village_plains_house","village_savanna_house","village_shepherd",
                "village_snowy_house","village_taiga_house","village_tannery","village_temple",
                "village_toolsmith","village_weaponsmith"};
        for(String table:tables)if(FuturaAncientCityGenerator120.villageLootProfile(
                "minecraft:chests/village/"+table)==0)throw new IllegalStateException("Unrouted village loot "+table);
        if(FuturaAncientCityGenerator120.villageLootProfile("minecraft:chests/ancient_city")!=0)
            throw new IllegalStateException("Ancient City loot routed as village loot");
        if(!FuturaAncientCityGenerator120.shouldIgnoreTemplateState(true,"minecraft:air")
                ||!FuturaAncientCityGenerator120.shouldIgnoreTemplateState(true,"minecraft:cave_air")
                ||FuturaAncientCityGenerator120.shouldIgnoreTemplateState(true,"minecraft:stone")
                ||FuturaAncientCityGenerator120.shouldIgnoreTemplateState(false,"minecraft:air"))
            throw new IllegalStateException("legacy_single air-ignore contract failed");
        long actual=FuturaAncientCityGenerator120.villageBlockEntityFingerprint();
        if(EXPECTED!=0L&&actual!=EXPECTED)throw new IllegalStateException("Village block NBT fixture changed: expected "
                +Long.toHexString(EXPECTED)+", got "+Long.toHexString(actual));
        return actual;
    }

    public static void main(String[] ignored){System.out.println(Long.toHexString(assertContract()));}
    private VillageTemplateNbtFixture120(){ }
}
