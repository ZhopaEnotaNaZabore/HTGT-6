package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.JsonObject;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/** Shared TreeFeature clearance and state-provider operations for configured placers. */
final class FuturaTreePlacement120 {
    private FuturaTreePlacement120(){}
    static int freeHeight(World w,JsonObject c,int x,int y,int z,int height){
        if(y<1||y+height+1>w.getHeight())return -1;
        JsonObject minimum=c.getAsJsonObject("minimum_size");int free=height;
        search:for(int dy=0;dy<=height+1;dy++){
            int size=dy<minimum.get("limit").getAsInt()?minimum.get("lower_size").getAsInt():minimum.get("upper_size").getAsInt();
            for(int dx=-size;dx<=size;dx++)for(int dz=-size;dz<=size;dz++){
                Block b=w.getBlock(x+dx,y+dy,z+dz);
                if((!replaceable(w,x+dx,y+dy,z+dz)&&!b.isWood(w,x+dx,y+dy,z+dz))
                        ||!c.get("ignore_vines").getAsBoolean()&&b==Blocks.vine){free=dy-2;break search;}
            }
        }
        return free<height&&(!minimum.has("min_clipped_height")||free<minimum.get("min_clipped_height").getAsInt())?-1:free;
    }
    static boolean dirt(World w,Random r,JsonObject c,int x,int y,int z){
        Block b=w.getBlock(x,y,z);
        if(c.get("force_dirt").getAsBoolean()||!soil(b)||b==Blocks.grass||b==Blocks.mycelium)
            return state(w,r,c.getAsJsonObject("dirt_provider"),x,y,z);
        return false;
    }
    static boolean soil(Block b){
        return b==Blocks.grass||b==Blocks.mycelium||b==Blocks.dirt||b==Backport120Content.rootedDirt
                ||b==Backport120Content.mossBlock||b==Backport120Content.mud||b==Backport120Content.muddyMangroveRoots;
    }
    static boolean replaceable(World w,int x,int y,int z){
        Block b=w.getBlock(x,y,z);
        return b==Blocks.air||b.isLeaves(w,x,y,z)||b==Blocks.vine||b==Blocks.tallgrass||b==Blocks.deadbush||b==Blocks.double_plant
                ||b==Backport120Content.glowLichen||b==Backport120Content.hangingRoots
                ||b==OceanContent.marinePlant&&w.getBlockMetadata(x,y,z)<=2
                ||b.getMaterial()==net.minecraft.block.material.Material.water;
    }
    static boolean state(World w,Random r,JsonObject provider,int x,int y,int z){
        FuturaBlockState120 state=FuturaBlockState120.of(FuturaFlowerProvider120.state(r,provider,x,y,z));
        return y>=0&&y<w.getHeight()&&w.setBlock(x,y,z,state.block,state.metadata,2);
    }
}
