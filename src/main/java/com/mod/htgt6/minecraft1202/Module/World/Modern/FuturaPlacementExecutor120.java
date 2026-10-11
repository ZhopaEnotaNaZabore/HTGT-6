package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import java.util.Random;

/** Lazy ordered modifier execution; feature callbacks share the exact same random stream. */
public final class FuturaPlacementExecutor120 {
    public interface Feature {void place(World world,Random random,int x,int y,int z);}
    private FuturaPlacementExecutor120(){}
    public static void execute(World w,int cx,int cz,int step,String key,FuturaUndergroundFeatures120 biomes,Feature feature){
        Random r=FuturaPlacementData120.INSTANCE.random(w.getSeed(),cx,cz,step,key);
        visit(w,r,step,key,biomes,FuturaPlacementData120.INSTANCE.placed(key).getAsJsonArray("placement"),0,cx<<4,0,cz<<4,feature);
    }
    private static void visit(World w,Random r,int step,String key,FuturaUndergroundFeatures120 biomes,JsonArray modifiers,int index,int x,int y,int z,Feature feature){
        if(index==modifiers.size()){feature.place(w,r,x,y,z);return;}
        JsonObject m=modifiers.get(index).getAsJsonObject();String type=m.get("type").getAsString();int count=1;
        if(type.equals("minecraft:count"))count=FuturaPlacementData120.count(r,m.get("count"));
        else if(type.equals("minecraft:rarity_filter")){if(r.nextFloat()>=1F/m.get("chance").getAsInt())return;}
        else if(type.equals("minecraft:in_square")){x+=r.nextInt(16);z+=r.nextInt(16);}
        else if(type.equals("minecraft:height_range"))y=FuturaPlacementData120.sampleHeight(r,m.getAsJsonObject("height"),w.getHeight());
        else if(type.equals("minecraft:heightmap")){y=height(w,m.get("heightmap").getAsString(),x,z);if(y<=0)return;}
        else if(type.equals("minecraft:biome")){if(!biomes.allows(w,x,y,z,step,key))return;}
        else if(type.equals("minecraft:block_predicate_filter")){if(!FuturaBlockState120.predicate(w,m.getAsJsonObject("predicate"),x,y,z))return;}
        else if(type.equals("minecraft:random_offset")){x+=FuturaPlacementData120.count(r,m.get("xz_spread"));y+=FuturaPlacementData120.count(r,m.get("y_spread"));z+=FuturaPlacementData120.count(r,m.get("xz_spread"));}
        else if(type.equals("minecraft:noise_based_count"))count=FuturaTreeFeatureRuntime120.noiseBasedCount(x,z,m.get("noise_factor").getAsDouble(),m.get("noise_offset").getAsDouble(),m.get("noise_to_count_ratio").getAsInt());
        else if(type.equals("minecraft:noise_threshold_count"))count=FuturaTreeFeatureRuntime120.thresholdCount(x,z,m.get("noise_level").getAsDouble(),m.get("below_noise").getAsInt(),m.get("above_noise").getAsInt());
        else if(type.equals("minecraft:surface_water_depth_filter")){if(height(w,"WORLD_SURFACE_WG",x,z)-height(w,"OCEAN_FLOOR_WG",x,z)>m.get("max_water_depth").getAsInt())return;}
        else if(type.equals("minecraft:surface_relative_threshold_filter")){
            int relative=y-height(w,m.get("heightmap").getAsString(),x,z);
            if(m.has("min_inclusive")&&relative<m.get("min_inclusive").getAsInt()||m.has("max_inclusive")&&relative>m.get("max_inclusive").getAsInt())return;
        }else if(type.equals("minecraft:environment_scan")){
            int dy=m.get("direction_of_search").getAsString().equals("up")?1:-1,limit=m.get("max_steps").getAsInt();
            if(m.has("allowed_search_condition")&&!FuturaBlockState120.predicate(w,m.getAsJsonObject("allowed_search_condition"),x,y,z))return;
            for(int i=0;i<limit;i++){
                if(FuturaBlockState120.predicate(w,m.getAsJsonObject("target_condition"),x,y,z))break;
                y+=dy;if(y<0||y>=w.getHeight())return;
                if(m.has("allowed_search_condition")&&!FuturaBlockState120.predicate(w,m.getAsJsonObject("allowed_search_condition"),x,y,z))break;
            }
            if(!FuturaBlockState120.predicate(w,m.getAsJsonObject("target_condition"),x,y,z))return;
        }else if(type.equals("minecraft:carving_mask")&&m.get("step").getAsString().equals("liquid"))return;
        else throw new IllegalArgumentException("Unsupported placement modifier for "+key+": "+m);
        for(int i=0;i<count;i++)visit(w,r,step,key,biomes,modifiers,index+1,x,y,z,feature);
    }
    public static int height(World w,String type,int x,int z){
        if(w instanceof FuturaGenerationRegion120)
            return ((FuturaGenerationRegion120)w).placementHeight(type,x,z);
        boolean floor=type.startsWith("OCEAN_FLOOR"),motion=type.startsWith("MOTION_BLOCKING");
        for(int y=w.getHeight()-1;y>=0;y--){Block b=w.getBlock(x,y,z);
            if(floor?b.getMaterial().blocksMovement()
                    :motion?b.getMaterial().blocksMovement()||b.getMaterial().isLiquid():b!=Blocks.air)return y+1;}
        return 0;
    }
}
