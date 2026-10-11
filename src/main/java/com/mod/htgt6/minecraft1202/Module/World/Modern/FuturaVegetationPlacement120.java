package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.*;
import java.util.Random;

/** Vanilla placed-feature modifiers around the existing Java-8 vegetation primitives. */
public final class FuturaVegetationPlacement120 {
    private final FuturaUndergroundFeatures120 biomes;
    public FuturaVegetationPlacement120(FuturaUndergroundFeatures120 biomes){this.biomes=biomes;}
    public void populate(World world,int chunkX,int chunkZ){
        for(String key:FuturaPlacementData120.INSTANCE.features(9))populateFeature(world,chunkX,chunkZ,key);
    }
    public void populateFeature(World world,int chunkX,int chunkZ,String key){
        FuturaPlacementData120 data=FuturaPlacementData120.INSTANCE;
        {
            String name=key.substring(10);
            if(!supported(name))return;
            Random r=data.random(world.getSeed(),chunkX,chunkZ,9,key);
            DecorateBiomeEvent.Decorate.EventType event=name.startsWith("trees_")?DecorateBiomeEvent.Decorate.EventType.TREE
                    :(name.startsWith("flower_")||name.equals("forest_flowers"))?DecorateBiomeEvent.Decorate.EventType.FLOWERS
                    :name.contains("mushroom")?DecorateBiomeEvent.Decorate.EventType.SHROOM
                    :name.contains("cactus")?DecorateBiomeEvent.Decorate.EventType.CACTUS
                    :name.contains("sugar_cane")?DecorateBiomeEvent.Decorate.EventType.REED
                    :name.contains("dead_bush")?DecorateBiomeEvent.Decorate.EventType.DEAD_BUSH
                    :name.contains("pumpkin")?DecorateBiomeEvent.Decorate.EventType.PUMPKIN
                    :name.contains("waterlily")?DecorateBiomeEvent.Decorate.EventType.LILYPAD:DecorateBiomeEvent.Decorate.EventType.GRASS;
            if(!TerrainGen.decorate(FuturaGenerationRegion120.unwrap(world),r,chunkX<<4,chunkZ<<4,event))return;
            final String configured=data.placed(key).get("feature").getAsString();
            FuturaPlacementExecutor120.execute(world,chunkX,chunkZ,9,key,biomes,(w,random,x,y,z)->{
                generate(w,random,name,configured,x,y,z);
            });
        }
    }
    public static boolean supported(String n){
        return n.startsWith("kelp_")||n.startsWith("seagrass_")||n.equals("sea_pickle")||n.equals("warm_ocean_vegetation")||n.equals("bamboo")||n.equals("bamboo_light")||n.equals("birch_tall")||n.equals("forest_flowers")||n.equals("dark_forest_vegetation")||n.equals("mushroom_island_vegetation")||n.equals("bamboo_vegetation")||n.startsWith("trees_")||n.startsWith("flower_")||n.startsWith("brown_mushroom_")
                ||n.startsWith("red_mushroom_")||n.startsWith("patch_");
    }
    private static void generate(World w,Random r,String name,String configured,int x,int y,int z){
        if(name.equals("bamboo")||name.equals("bamboo_light")){
            JsonObject config=FuturaPlacementData120.INSTANCE.configured(configured).getAsJsonObject("config");
            FuturaTreeFeatureRuntime120.bamboo(w,r,x,y,z,config.get("probability").getAsFloat());return;
        }
        JsonObject feature=FuturaPlacementData120.INSTANCE.configured(configured);
        String type=feature.get("type").getAsString();
        if(com.mod.htgt6.minecraft1202.Module.World.Aquatic.FuturaAquaticFeatures120.handles(type)){
            com.mod.htgt6.minecraft1202.Module.World.Aquatic.FuturaAquaticFeatures120.generate(w,r,type,feature.getAsJsonObject("config"),x,y,z);return;
        }
        if(configured.equals("minecraft:forest_flowers")){
            FuturaSimplePatch120.generate(w,r,configured,feature.getAsJsonObject("config"),x,y,z);return;
        }
        if(type.endsWith("_selector")){
            selected(w,r,FuturaFeatureSelector120.select(type,feature.getAsJsonObject("config"),r),x,y,z);return;
        }
        if(type.equals("minecraft:tree")){
            WorldGenFuturaVanillaTree120.generateConfigured(w,r,configured,feature.getAsJsonObject("config"),x,y,z);return;
        }
        if(type.equals("minecraft:huge_brown_mushroom")||type.equals("minecraft:huge_red_mushroom")){
            new net.minecraft.world.gen.feature.WorldGenBigMushroom(type.contains("brown")?0:1).generate(w,r,x,y,z);return;
        }
        JsonObject config=FuturaPlacementData120.INSTANCE.configured(configured).getAsJsonObject("config");
        if(FuturaSimplePatch120.generate(w,r,configured,config,x,y,z))return;
        if(FuturaBlockColumn120.generate(w,r,configured,config,x,y,z))return;
        throw new IllegalStateException("Unsupported configured vegetation feature: "+configured);
    }
    private static void selected(World w,Random r,JsonElement selection,int x,int y,int z){
        JsonObject placed=selection.isJsonPrimitive()?FuturaPlacementData120.INSTANCE.placed(selection.getAsString()):selection.getAsJsonObject();
        for(JsonElement entry:placed.getAsJsonArray("placement")){
            JsonObject modifier=entry.getAsJsonObject();
            if(!modifier.get("type").getAsString().equals("minecraft:block_predicate_filter"))
                throw new IllegalArgumentException("Unsupported selected-tree modifier: "+modifier);
            JsonObject predicate=modifier.getAsJsonObject("predicate");
            if(!predicate.get("type").getAsString().equals("minecraft:would_survive"))
                throw new IllegalArgumentException("Unsupported selected-tree predicate: "+predicate);
            if(!WorldGenFuturaVanillaTree120.saplingSurvives(w,predicate.getAsJsonObject("state").get("Name").getAsString(),x,y,z))return;
        }
        JsonElement child=placed.get("feature");
        if(child.isJsonPrimitive())generate(w,r,"selected",child.getAsString(),x,y,z);
        else {
            String type=child.getAsJsonObject().get("type").getAsString();
            if(com.mod.htgt6.minecraft1202.Module.World.Aquatic.FuturaAquaticFeatures120.handles(type))
                com.mod.htgt6.minecraft1202.Module.World.Aquatic.FuturaAquaticFeatures120.generate(w,r,type,child.getAsJsonObject().getAsJsonObject("config"),x,y,z);
            else if(type.equals("minecraft:huge_brown_mushroom")||type.equals("minecraft:huge_red_mushroom"))
                new net.minecraft.world.gen.feature.WorldGenBigMushroom(type.contains("brown")?0:1).generate(w,r,x,y,z);
            else throw new IllegalArgumentException("Unsupported inline selected feature: "+type);
        }
    }

}
