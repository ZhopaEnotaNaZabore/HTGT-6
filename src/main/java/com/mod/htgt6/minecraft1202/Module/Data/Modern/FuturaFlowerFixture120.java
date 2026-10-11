package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.google.gson.*;
import com.mod.htgt6.minecraft1202.Module.World.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import java.io.*;
import java.lang.reflect.*;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

final class FuturaFlowerFixture120 {
    static void validate()throws Exception {
        for(JsonElement e:load("futura_flower_noise_oracle_1202.json")) {
            JsonObject v=e.getAsJsonObject();JsonArray array=v.getAsJsonArray("amplitudes");double[] amps=new double[array.size()];
            for(int i=0;i<amps.length;i++)amps[i]=array.get(i).getAsDouble();
            NormalNoise120 noise=new NormalNoise120(new Random(v.get("seed").getAsLong()),v.get("first").getAsInt(),amps);
            require(Math.abs(noise.sample(v.get("x").getAsDouble(),v.get("y").getAsDouble(),v.get("z").getAsDouble())-v.get("value").getAsDouble())<1e-10,"independent legacy normal noise");
        }
        for(JsonElement e:load("futura_flower_state_oracle_1202.json")) {
            JsonObject v=e.getAsJsonObject();Random r=new Random(7890);
            JsonObject actual=FuturaFlowerProvider120.state(r,provider(v.get("configured").getAsString()),v.get("x").getAsInt(),v.get("y").getAsInt(),v.get("z").getAsInt());
            require(actual.equals(v.getAsJsonObject("state")),"independent flower provider: "+v);
            require(r.nextLong()==v.get("nextLong").getAsLong(),"provider RNG continuation");
        }
        String[] small={"poppy","blue_orchid","allium","azure_bluet","red_tulip","orange_tulip","white_tulip","pink_tulip","oxeye_daisy","dandelion","cornflower","lily_of_the_valley"};
        for(int i=0;i<small.length;i++) {
            FuturaVegetationFixture120.Grid w=single(state(small[i]),101);
            Block expected=i<9?Blocks.red_flower:i==9?Blocks.yellow_flower:i==10?Backport120Content.cornflower:Backport120Content.lilyOfTheValley;
            require(w.getBlock(0,101,0)==expected && w.getBlockMetadata(0,101,0)==(i<9?i:0),"flower palette "+small[i]);
        }
        for(String key:new String[]{"lilac","rose_bush","peony"}) {
            JsonObject state=state(key);int expected=key.equals("lilac")?1:key.equals("rose_bush")?4:5;
            FuturaVegetationFixture120.Grid w=single(state,101);
            require(w.getBlock(0,101,0)==Blocks.double_plant && w.getBlockMetadata(0,101,0)==expected && w.getBlockMetadata(0,102,0)==8,"double flower "+key);
            require(single(state,383).cells.isEmpty(),"double flower respects ceiling");
        }
        String[] faces={"north","east","south","west"};
        for(int amount=1;amount<=4;amount++)for(int face=0;face<4;face++) {
            JsonObject state=state("pink_petals"),props=new JsonObject();props.addProperty("facing",faces[face]);props.addProperty("flower_amount",Integer.toString(amount));state.add("Properties",props);
            FuturaVegetationFixture120.Grid w=single(state,101);
            require(w.getBlock(0,101,0)==Backport120Content.pinkPetals && w.getBlockMetadata(0,101,0)==(((face+2)&3)|((amount-1)<<2)),"petal state");
        }
        Random actual=new Random(77),expected=new Random(77);
        for(int i=0;i<256;i++)require(FuturaFlowerProvider120.state(actual,provider("flower_default"),0,100,0).get("Name").getAsString()
                .equals(expected.nextInt(3)<2?"minecraft:poppy":"minecraft:dandelion"),"default 2:1 weights");
        require(actual.nextLong()==expected.nextLong(),"weighted continuation");
        Method generate=FuturaVegetationPlacement120.class.getDeclaredMethod("generate",World.class,Random.class,String.class,String.class,int.class,int.class,int.class);generate.setAccessible(true);
        Method supported=FuturaVegetationPlacement120.class.getDeclaredMethod("supported",String.class);supported.setAccessible(true);
        for(String key:new String[]{"flower_default","flower_swamp","forest_flowers","flower_flower_forest","flower_plain","flower_meadow","flower_cherry"}) {
            require((Boolean)supported.invoke(null,key),"dispatch "+key);
            FuturaVegetationFixture120.Grid w=FuturaVegetationFixture120.world();
            generate.invoke(null,w,new Random(1),key,"minecraft:"+key,0,101,0);
            require(!w.cells.isEmpty(),"actual configured generator placed no flowers: "+key);
        }
        System.out.println("Flower 45 OK: 84 noise + 63 provider/RNG vectors, seven configured generators, small/double flowers and all 16 petal states");
    }
    private static JsonObject state(String name){JsonObject s=new JsonObject();s.addProperty("Name","minecraft:"+name);return s;}
    private static JsonObject provider(String name){return FuturaPlacementData120.INSTANCE.configured(name).getAsJsonObject("config").getAsJsonObject("feature").getAsJsonObject("feature").getAsJsonObject("config").getAsJsonObject("to_place");}
    private static FuturaVegetationFixture120.Grid single(JsonObject state,int y)throws Exception {
        JsonObject config=new JsonParser().parse(FuturaPlacementData120.INSTANCE.configured("flower_default").get("config").toString()).getAsJsonObject();
        config.addProperty("tries",1);config.addProperty("xz_spread",0);config.addProperty("y_spread",0);
        JsonObject provider=new JsonObject();provider.addProperty("type","minecraft:simple_state_provider");provider.add("state",state);
        config.getAsJsonObject("feature").getAsJsonObject("feature").getAsJsonObject("config").add("to_place",provider);
        Class<?> c=Class.forName("com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaSimplePatch120");
        Method method=c.getDeclaredMethod("generate",World.class,Random.class,String.class,JsonObject.class,int.class,int.class,int.class);method.setAccessible(true);
        FuturaVegetationFixture120.Grid w=FuturaVegetationFixture120.world();w.floor=y-1;
        require((Boolean)method.invoke(null,w,new Random(1),"flower_test",config,0,y,0),"flower adapter dispatch");return w;
    }
    private static JsonArray load(String name)throws Exception {
        try(Reader r=new InputStreamReader(FuturaFlowerFixture120.class.getResourceAsStream("/assets/htgt6/worldgen/"+name),"UTF-8")){return new JsonParser().parse(r).getAsJsonArray();}
    }
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
}
