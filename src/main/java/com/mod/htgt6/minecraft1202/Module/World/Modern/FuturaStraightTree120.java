package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.*;
import java.util.Random;
import net.minecraft.world.World;

/** StraightTrunkPlacer with the vanilla blob, bush, pine and spruce foliage placers. */
public final class FuturaStraightTree120 {
    private FuturaStraightTree120(){}
    public static boolean supports(JsonObject c){
        if(!c.getAsJsonObject("trunk_placer").get("type").getAsString().equals("minecraft:straight_trunk_placer"))return false;
        for(JsonElement e:c.getAsJsonArray("decorators"))if(!e.getAsJsonObject().get("type").getAsString().equals("minecraft:beehive"))return false;
        String foliage=c.getAsJsonObject("foliage_placer").get("type").getAsString();
        return foliage.equals("minecraft:blob_foliage_placer")||foliage.equals("minecraft:bush_foliage_placer")
                ||foliage.equals("minecraft:pine_foliage_placer")||foliage.equals("minecraft:spruce_foliage_placer");
    }
    public static boolean generate(World w,Random r,JsonObject c,int x,int y,int z){
        java.util.List<int[]> logs=new java.util.ArrayList<int[]>(),leaves=new java.util.ArrayList<int[]>();
        JsonObject trunk=c.getAsJsonObject("trunk_placer"),foliage=c.getAsJsonObject("foliage_placer");
        String type=foliage.get("type").getAsString();
        int height=trunk.get("base_height").getAsInt()+r.nextInt(trunk.get("height_rand_a").getAsInt()+1)+r.nextInt(trunk.get("height_rand_b").getAsInt()+1);
        int leavesHeight=type.equals("minecraft:spruce_foliage_placer")?Math.max(4,height-FuturaPlacementData120.count(r,foliage.get("trunk_height"))):FuturaPlacementData120.count(r,foliage.get("height"));
        int radius=FuturaPlacementData120.count(r,foliage.get("radius"));
        if(type.equals("minecraft:pine_foliage_placer"))radius+=r.nextInt(Math.max(height-leavesHeight+1,1));
        int free=FuturaTreePlacement120.freeHeight(w,c,x,y,z,height);if(free<0)return false;
        FuturaTreePlacement120.dirt(w,r,c,x,y-1,z);
        for(int dy=0;dy<free;dy++)if(FuturaTreePlacement120.replaceable(w,x,y+dy,z)){FuturaTreePlacement120.state(w,r,c.getAsJsonObject("trunk_provider"),x,y+dy,z);logs.add(new int[]{x,y+dy,z});}
        int offset=FuturaPlacementData120.count(r,foliage.get("offset")),current=type.equals("minecraft:spruce_foliage_placer")?r.nextInt(2):0,limit=1,reset=0;
        int bottom=type.equals("minecraft:spruce_foliage_placer")?-leavesHeight:offset-leavesHeight;
        for(int dy=offset;dy>=bottom;dy--){
            int width=type.equals("minecraft:blob_foliage_placer")?Math.max(radius-1-dy/2,0)
                    :type.equals("minecraft:bush_foliage_placer")?radius-1-dy:current;
            for(int dx=-width;dx<=width;dx++)for(int dz=-width;dz<=width;dz++){
                if(Math.abs(dx)==width&&Math.abs(dz)==width){
                    if(type.equals("minecraft:blob_foliage_placer")?(r.nextInt(2)==0||dy==0)
                            :type.equals("minecraft:bush_foliage_placer")?r.nextInt(2)==0:width>0)continue;
                }
                if(FuturaTreePlacement120.replaceable(w,x+dx,y+free+dy,z+dz)){FuturaTreePlacement120.state(w,r,c.getAsJsonObject("foliage_provider"),x+dx,y+free+dy,z+dz);leaves.add(new int[]{x+dx,y+free+dy,z+dz});}
            }
            if(type.equals("minecraft:spruce_foliage_placer")){
                if(current>=limit){current=reset;reset=1;limit=Math.min(limit+1,radius);}else current++;
            }else if(type.equals("minecraft:pine_foliage_placer")){
                if(current>=1&&dy==bottom+1)current--;else if(current<radius)current++;
            }
        }
        BeeNestDecorator115.configured(w,r,c,logs,leaves);
        return true;
    }
}
