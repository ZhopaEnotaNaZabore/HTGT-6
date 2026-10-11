package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.google.gson.*;
import com.mod.htgt6.minecraft1202.Module.World.Modern.*;
import java.util.Random;

final class FuturaSelectorFixture120 {
    static void validate(){
        FuturaPlacementData120 data=FuturaPlacementData120.INSTANCE;
        JsonObject config=data.configured("dark_forest_vegetation").getAsJsonObject("config");
        Random actual=new WorldgenRandom120(123),expected=new WorldgenRandom120(123);
        float[] chance={.025F,.05F,.6666667F,.2F,.1F};int[] counts=new int[6];
        for(int i=0;i<2048;i++){
            int selected=5;for(int j=0;j<chance.length;j++)if(expected.nextFloat()<chance[j]){selected=j;break;}
            JsonElement wanted=selected==5?config.get("default"):config.getAsJsonArray("features").get(selected).getAsJsonObject().get("feature");
            JsonElement found=FuturaFeatureSelector120.select("minecraft:random_selector",config,actual);
            require(found.equals(wanted),"dark forest sequential selector");counts[selected]++;
            require(actual.nextLong()==expected.nextLong(),"selector RNG continuation");
        }
        for(int count:counts)require(count>0,"lost selector alternative");
        require(data.placed("super_birch_bees")!=null&&data.configured("super_birch_bees")!=null,"nested selector dependencies not extracted");
        require(data.has(OverworldBiomeParameterMap120.Biome.JUNGLE,9,"bamboo_light"),"light bamboo omitted from jungle");
        require(!data.has(OverworldBiomeParameterMap120.Biome.BAMBOO_JUNGLE,9,"bamboo_light"),"light bamboo duplicated in bamboo jungle");
        require(data.index(9,"bamboo")!=data.index(9,"bamboo_light"),"bamboo feature seeds coupled");
        System.out.println("Futura selectors OK: 2048 dark forest decisions/RNG continuations, nested data and independent bamboo placement");
    }
    private static void require(boolean ok,String why){if(!ok)throw new AssertionError(why);}
}
