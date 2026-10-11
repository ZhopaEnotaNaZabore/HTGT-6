package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.google.gson.*;
import com.mod.htgt6.minecraft1202.Module.World.Modern.*;
import java.lang.reflect.Method;
import java.util.*;
import net.minecraft.world.World;
import net.minecraft.init.Blocks;

final class FuturaPlacementFixture120 {
    static void validate() throws Exception {
        Method visit=FuturaPlacementExecutor120.class.getDeclaredMethod("visit",World.class,Random.class,int.class,String.class,FuturaUndergroundFeatures120.class,JsonArray.class,int.class,int.class,int.class,int.class,FuturaPlacementExecutor120.Feature.class);
        visit.setAccessible(true);FuturaVegetationFixture120.Grid w=FuturaVegetationFixture120.world();
        JsonArray modifiers=new JsonArray();
        for(JsonElement e:FuturaPlacementData120.INSTANCE.placed("lush_caves_vegetation").getAsJsonArray("placement")){
            String type=e.getAsJsonObject().get("type").getAsString();
            if(type.equals("minecraft:environment_scan")||type.equals("minecraft:random_offset"))modifiers.add(e);
        }
        List<Integer> heights=new ArrayList<Integer>();FuturaPlacementExecutor120.Feature callback=(world,r,x,y,z)->heights.add(y);
        visit.invoke(null,w,new Random(0),9,"fixture",null,modifiers,0,0,106,0,callback);
        require(heights.equals(Arrays.asList(101)),"scan rejected solid target reached through air");
        heights.clear();visit.invoke(null,w,new Random(0),9,"fixture",null,modifiers,0,0,114,0,callback);
        require(heights.isEmpty(),"scan exceeded max_steps");
        visit.invoke(null,w,new Random(0),9,"fixture",null,modifiers,0,0,100,0,callback);
        require(heights.isEmpty(),"scan ignored initial allowed-search condition");
        JsonArray order=new JsonParser().parse("[{\"type\":\"minecraft:count\",\"count\":2},{\"type\":\"minecraft:in_square\"},{\"type\":\"minecraft:height_range\",\"height\":{\"type\":\"minecraft:uniform\",\"min_inclusive\":{\"absolute\":0},\"max_inclusive\":{\"absolute\":0}}}]").getAsJsonArray();
        List<String> actual=new ArrayList<String>(),expected=new ArrayList<String>();Random rng=new Random(91),oracle=new Random(91);
        visit.invoke(null,w,rng,9,"fixture",null,order,0,-16,0,32,(FuturaPlacementExecutor120.Feature)(world,r,x,y,z)->actual.add(x+","+y+","+z+":"+r.nextInt(17)));
        for(int i=0;i<2;i++){int x=-16+oracle.nextInt(16),z=32+oracle.nextInt(16);oracle.nextInt(1);expected.add(x+",64,"+z+":"+oracle.nextInt(17));}
        require(actual.equals(expected)&&rng.nextLong()==oracle.nextLong(),"modifier order, +64 height or lazy feature RNG changed");
        sturdyFaces();
        caveVines(visit);
        System.out.println("Futura placements OK: real environment scan, max_steps, initial predicate, lazy callback RNG and +64 absolute height");
    }
    private static void sturdyFaces()throws Exception{
        FuturaVegetationFixture120.Grid w=FuturaVegetationFixture120.world();
        JsonObject predicate=new JsonObject();predicate.addProperty("type","minecraft:has_sturdy_face");
        for(String direction:new String[]{"down","up","north","south","west","east"}){
            predicate.addProperty("direction",direction);w.cells.put("0,300,0",Blocks.stone);
            require(FuturaBlockState120.predicate(w,predicate,0,300,0),"stone sturdy face "+direction);
            w.cells.put("0,300,0",Blocks.air);
            require(!FuturaBlockState120.predicate(w,predicate,0,300,0),"air sturdy face "+direction);
            w.cells.put("0,300,0",Blocks.water);
            require(!FuturaBlockState120.predicate(w,predicate,0,300,0),"water sturdy face "+direction);
        }
        w.cells.put("0,300,0",Blocks.stone_slab);predicate.addProperty("direction","down");
        require(FuturaBlockState120.predicate(w,predicate,0,300,0),"lower slab underside");
        w.metadata.put("0,300,0",8);
        require(!FuturaBlockState120.predicate(w,predicate,0,300,0),"upper slab underside is not a full face");
        predicate.addProperty("direction","up");
        require(FuturaBlockState120.predicate(w,predicate,0,300,0),"upper slab top");
        JsonArray offset=new JsonArray();offset.add(new JsonPrimitive(0));offset.add(new JsonPrimitive(1));offset.add(new JsonPrimitive(0));predicate.add("offset",offset);
        require(FuturaBlockState120.predicate(w,predicate,0,299,0),"sturdy face offset");
    }
    private static void caveVines(Method visit)throws Exception{
        FuturaVegetationFixture120.Grid w=FuturaVegetationFixture120.world();
        w.soil=Blocks.air;
        for(int x=-16;x<0;x++)for(int z=32;z<48;z++)w.cells.put(x+",300,"+z,Blocks.stone);
        JsonArray modifiers=new JsonArray();
        // Execute the real crash-producing modifier chain; the biome decision is tested separately.
        for(JsonElement e:FuturaPlacementData120.INSTANCE.placed("cave_vines").getAsJsonArray("placement"))
            if(!e.getAsJsonObject().get("type").getAsString().equals("minecraft:biome"))modifiers.add(e);
        List<String> actual=new ArrayList<String>(),expected=new ArrayList<String>();
        Random random=new Random(91),oracle=new Random(91);
        visit.invoke(null,w,random,9,"minecraft:cave_vines",null,modifiers,0,-16,0,32,
                (FuturaPlacementExecutor120.Feature)(world,r,x,y,z)->actual.add(x+","+y+","+z));
        for(int i=0;i<188;i++){
            int x=-16+oracle.nextInt(16),z=32+oracle.nextInt(16),y=oracle.nextInt(321);
            if(y>=288&&y<300)expected.add(x+",299,"+z);
        }
        require(!expected.isEmpty()&&actual.equals(expected),"cave vines ceiling scan / downward offset");
        require(random.nextLong()==oracle.nextLong(),"cave vines predicate consumed RNG");
        w.cells.clear();actual.clear();
        visit.invoke(null,w,new Random(91),9,"minecraft:cave_vines",null,modifiers,0,-16,0,32,
                (FuturaPlacementExecutor120.Feature)(world,r,x,y,z)->actual.add(x+","+y+","+z));
        require(actual.isEmpty(),"cave vines placed without ceiling");
        System.out.println("Cave vines crash regression OK: official 188-attempt modifiers, Y=300 ceiling, negative chunks, absent ceiling, face directions, slabs, offset and RNG");
    }
    private static void require(boolean ok,String why){if(!ok)throw new AssertionError(why);}
}
