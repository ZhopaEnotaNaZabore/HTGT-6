package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.*;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/** Pinned cactus/reed RandomPatch + BlockColumn feature, with vanilla probe/truncation order. */
public final class FuturaBlockColumn120 {
    private FuturaBlockColumn120() {}
    public static boolean generate(World w,Random r,String configured,JsonObject patch,int x,int y,int z) {
        String name=configured.replace("minecraft:","");
        if(!name.equals("patch_cactus")&&!name.equals("patch_sugar_cane"))return false;
        JsonObject placed=patch.getAsJsonObject("feature");
        int spread=patch.get("xz_spread").getAsInt()+1,vertical=patch.get("y_spread").getAsInt()+1;
        for(int attempt=0;attempt<patch.get("tries").getAsInt();attempt++) {
            int px=x+r.nextInt(spread)-r.nextInt(spread),py=y+r.nextInt(vertical)-r.nextInt(vertical),pz=z+r.nextInt(spread)-r.nextInt(spread);
            if(py<0||py>=w.getHeight())continue;
            boolean accepted=true;
            for(JsonElement e:placed.getAsJsonArray("placement")) {
                JsonObject modifier=e.getAsJsonObject();
                if(!modifier.get("type").getAsString().equals("minecraft:block_predicate_filter"))throw new IllegalArgumentException("Unsupported column modifier");
                if(!FuturaSimplePatch120.matches(w,modifier.getAsJsonObject("predicate"),px,py,pz)){accepted=false;break;}
            }
            if(accepted)place(w,r,placed.getAsJsonObject("feature").getAsJsonObject("config"),px,py,pz);
        }
        return true;
    }
    public static boolean place(World w,Random r,JsonObject config,int x,int y,int z) {
        JsonArray layers=config.getAsJsonArray("layers");int[] heights=new int[layers.size()];int total=0;
        for(int i=0;i<heights.length;i++) {
            heights[i]=FuturaPlacementData120.count(r,layers.get(i).getAsJsonObject().get("height"));
            if(heights[i]<0)throw new IllegalArgumentException("Negative column height");
            total=Math.addExact(total,heights[i]);
        }
        if(total==0)return false;
        String direction=config.get("direction").getAsString();int dx=0,dy=0,dz=0;
        if(direction.equals("up"))dy=1;else if(direction.equals("down"))dy=-1;
        else if(direction.equals("east"))dx=1;else if(direction.equals("west"))dx=-1;
        else if(direction.equals("south"))dz=1;else if(direction.equals("north"))dz=-1;
        else throw new IllegalArgumentException("Unsupported direction: "+direction);
        // Vanilla probes one position ahead of the write cursor, including headroom beyond the tip.
        int px=x+dx,py=y+dy,pz=z+dz;
        for(int allowed=0;allowed<total;allowed++,px+=dx,py+=dy,pz+=dz) {
            if(py<0||py>=w.getHeight()||!FuturaSimplePatch120.matches(w,config.getAsJsonObject("allowed_placement"),px,py,pz)) {
                truncate(heights,total,allowed,config.get("prioritize_tip").getAsBoolean());break;
            }
        }
        for(int i=0;i<heights.length;i++)for(int j=0;j<heights[i];j++) {
            JsonObject state=FuturaFlowerProvider120.state(r,layers.get(i).getAsJsonObject().getAsJsonObject("provider"),x,y,z);
            String name=state.get("Name").getAsString();Block block;
            if(name.equals("minecraft:cactus"))block=Blocks.cactus;
            else if(name.equals("minecraft:sugar_cane"))block=Blocks.reeds;
            else throw new IllegalArgumentException("Unsupported column block: "+name);
            int age=state.has("Properties")?state.getAsJsonObject("Properties").get("age").getAsInt():0;
            if(y>=0&&y<w.getHeight())w.setBlock(x,y,z,block,age,2);
            x+=dx;y+=dy;z+=dz;
        }
        return true;
    }
    public static void truncate(int[] heights,int total,int allowed,boolean prioritizeTip) {
        int remove=total-allowed,step=prioritizeTip?1:-1;
        for(int i=prioritizeTip?0:heights.length-1;i>=0&&i<heights.length&&remove>0;i+=step) {
            int cut=Math.min(heights[i],remove);heights[i]-=cut;remove-=cut;
        }
    }
}
