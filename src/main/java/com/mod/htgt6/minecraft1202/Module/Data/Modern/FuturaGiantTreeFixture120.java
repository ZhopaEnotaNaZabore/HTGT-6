package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.google.gson.JsonObject;
import com.mod.htgt6.minecraft1202.Module.World.Modern.*;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

final class FuturaGiantTreeFixture120 {
    static void validate()throws Exception{
        for(String key:new String[]{"mega_pine","mega_spruce"})for(int seed=0;seed<64;seed++){
            JsonObject c=config(key);Random actual=new Random(seed),oracle=new Random(seed);
            int height=13+oracle.nextInt(3)+oracle.nextInt(15),crown=(key.equals("mega_pine")?3:13)+oracle.nextInt(5);
            FuturaVegetationFixture120.Grid w=FuturaVegetationFixture120.world();
            int x=seed%2==0?-17:15,z=seed%3==0?-1:31,y=seed%2==0?270:101;w.floor=y-1;
            require(WorldGenFuturaVanillaTree120.generateConfigured(w,actual,key,c,x,y,z),key+" failed");
            int logs=0;for(Block block:w.cells.values())if(block==Blocks.log)logs++;
            require(logs==4*height-3,"giant trunk block count");
            for(int yy=y;yy<y+height-1;yy++)for(int dx=0;dx<2;dx++)for(int dz=0;dz<2;dz++)
                require(w.getBlock(x+dx,yy,z+dz)==Blocks.log,"2x2 trunk gap");
            require(w.getBlock(x,y+height-1,z)==Blocks.log&&w.getBlock(x+1,y+height-1,z)==Blocks.leaves,"single log tip");
            for(int dx=0;dx<2;dx++)for(int dz=0;dz<2;dz++)require(w.getBlock(x+dx,y+height,z+dz)==Blocks.leaves,"2x2 crown tip");
            require(w.getBlock(x-1,y+height,z)==Blocks.air,"tip radius");
            int bottom=y+height-crown;
            if(bottom!=w.floor)require(w.getBlock(x-3,bottom,z)==Blocks.leaves,"bottom crown radius: "+key+" seed "+seed);
            if(bottom-1!=w.floor)require(w.getBlock(x-3,bottom-1,z)==Blocks.air,"crown starts too low");
            require(w.getBlock(x-3,y-1,z-1)==Blocks.dirt&&w.getBlockMetadata(x-3,y-1,z-1)==2,"podzol fixed circle");
            for(int i=0;i<20;i++)oracle.nextInt(64);
            require(actual.nextLong()==oracle.nextLong(),"giant tree RNG continuation");
        }
        for(Block obstacle:new Block[]{Blocks.stone,Blocks.snow_layer,Blocks.vine}){
            FuturaVegetationFixture120.Grid w=FuturaVegetationFixture120.world();w.cells.put("1,104,1",obstacle);
            Random actual=new Random(72),oracle=new Random(72);oracle.nextInt(3);oracle.nextInt(15);oracle.nextInt(5);
            require(!WorldGenFuturaVanillaTree120.generateConfigured(w,actual,"mega_pine",config("mega_pine"),0,101,0),"obstructed giant accepted");
            require(w.cells.size()==1&&actual.nextLong()==oracle.nextLong(),"failed giant mutated world or RNG");
        }
        for(Block plant:new Block[]{Blocks.double_plant,Blocks.water}){
            FuturaVegetationFixture120.Grid w=FuturaVegetationFixture120.world();w.cells.put("1,104,1",plant);
            require(WorldGenFuturaVanillaTree120.generateConfigured(w,new Random(72),"mega_pine",config("mega_pine"),0,101,0),"replaceable tree position rejected");
            require(w.getBlock(1,104,1)==Blocks.log,"replaceable block not replaced");
        }
        // Only one dirt-provider write: it is the lowest trunk-context entry, so only five patch draws.
        FuturaVegetationFixture120.Grid mixed=FuturaVegetationFixture120.world();mixed.soil=Blocks.dirt;mixed.cells.put("0,100,0",Blocks.grass);
        Random actual=new Random(4),oracle=new Random(4);oracle.nextInt(3);oracle.nextInt(15);oracle.nextInt(5);
        require(WorldGenFuturaVanillaTree120.generateConfigured(mixed,actual,"mega_pine",config("mega_pine"),0,101,0),"mixed substrate giant failed");
        for(int i=0;i<5;i++)oracle.nextInt(64);
        require(actual.nextLong()==oracle.nextLong(),"dirt-provider writes missing from decorator context");
        FuturaVegetationFixture120.Grid high=FuturaVegetationFixture120.world();high.floor=380;
        require(!WorldGenFuturaVanillaTree120.generateConfigured(high,new Random(0),"mega_spruce",config("mega_spruce"),0,381,0)&&high.cells.isEmpty(),"build height guard");
        for(int seed=0;seed<16;seed++){
            FuturaVegetationFixture120.Grid low=FuturaVegetationFixture120.world();low.floor=0;
            require(WorldGenFuturaVanillaTree120.generateConfigured(low,new Random(seed),"mega_spruce",config("mega_spruce"),0,1,0),"minimum-height giant failed");
        }
        System.out.println("Futura giant trees OK: 128 trees, shifted/negative coordinates, trunk/crown/podzol, RNG, soil context, obstruction and height guards");
    }
    private static JsonObject config(String key){return FuturaPlacementData120.INSTANCE.configured(key).getAsJsonObject("config");}
    private static void require(boolean ok,String why){if(!ok)throw new AssertionError(why);}
}
