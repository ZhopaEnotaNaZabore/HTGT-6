package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.google.gson.JsonObject;
import com.mod.htgt6.minecraft1202.Module.World.Modern.*;
import java.util.Random;
import net.minecraft.init.Blocks;

final class FuturaStraightTreeFixture120 {
    static void validate() throws Exception {
        for(String key:new String[]{"oak","birch","super_birch_bees_0002"})for(int seed=0;seed<64;seed++){
            JsonObject c=FuturaPlacementData120.INSTANCE.configured(key).getAsJsonObject("config");
            FuturaVegetationFixture120.Grid w=FuturaVegetationFixture120.world();
            Random actual=new Random(seed),oracle=new Random(seed);
            int height=c.getAsJsonObject("trunk_placer").get("base_height").getAsInt()+oracle.nextInt(3)+oracle.nextInt(key.startsWith("super_")?7:1);
            require(WorldGenFuturaVanillaTree120.generateConfigured(w,actual,key,c,0,101,0),"straight tree failed");
            for(int y=101;y<101+height;y++)require(w.getBlock(0,y,0)==Blocks.log,"trunk gap");
            require(w.getBlock(0,101+height,0)==Blocks.leaves&&w.getBlock(0,102+height,0)==Blocks.air,"blob crown height");
            // Four corner decisions on each of the four BlobFoliagePlacer rows.
            for(int i=0;i<16;i++)oracle.nextInt(2);
            if(!key.startsWith("super_"))require(actual.nextLong()==oracle.nextLong(),"blob foliage random continuation");
            require(w.getBlock(0,100,0)==Blocks.dirt,"trunk did not replace grass with dirt");
        }
        JsonObject oak=FuturaPlacementData120.INSTANCE.configured("oak").getAsJsonObject("config");
        FuturaVegetationFixture120.Grid blocked=FuturaVegetationFixture120.world();blocked.cells.put("0,103,0",Blocks.stone);
        require(!WorldGenFuturaVanillaTree120.generateConfigured(blocked,new Random(0),"oak",oak,0,101,0),"obstructed trunk accepted");
        require(blocked.cells.size()==1,"failed tree partially wrote blocks");
        for(String key:new String[]{"pine","spruce","jungle_bush"})for(int seed=0;seed<32;seed++){
            FuturaVegetationFixture120.Grid w=FuturaVegetationFixture120.world();
            require(WorldGenFuturaVanillaTree120.generateConfigured(w,new Random(seed),key,FuturaPlacementData120.INSTANCE.configured(key).getAsJsonObject("config"),0,101,0),key+" failed");
            require(w.cells.containsValue(Blocks.leaves)&&w.getBlock(0,101,0)==Blocks.log,key+" missing trunk or foliage");
        }
        System.out.println("Futura straight trees OK: 288 configured trees, blob crown/RNG, pine, spruce, bush, obstruction before writes");
    }
    private static void require(boolean ok,String why){if(!ok)throw new AssertionError(why);}
}
