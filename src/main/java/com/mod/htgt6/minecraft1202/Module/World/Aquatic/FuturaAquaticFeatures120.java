package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import com.google.gson.JsonObject;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockKelp;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaPlacementData120;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import java.util.Random;

/** Configured aquatic features; count, origin, biome and seed belong to the caller's placed feature. */
public final class FuturaAquaticFeatures120 {
    private FuturaAquaticFeatures120(){}
    public static boolean handles(String type){return type.equals("minecraft:kelp")||type.equals("minecraft:seagrass")
            ||type.equals("minecraft:sea_pickle")||type.startsWith("minecraft:coral_");}
    public static void generate(World w,Random r,String type,JsonObject config,int x,int y,int z){
        if(type.startsWith("minecraft:coral_")){
            int shape=type.endsWith("claw")?WorldGenCoralFeature120.CLAW:type.endsWith("mushroom")?WorldGenCoralFeature120.MUSHROOM:WorldGenCoralFeature120.TREE;
            WorldGenCoralFeature120.generate(w,r,x,y,z,r.nextInt(5),shape);return;
        }
        if(type.equals("minecraft:kelp")){kelp(w,r,x,z);return;}
        if(type.equals("minecraft:seagrass")){
            x+=r.nextInt(8)-r.nextInt(8);z+=r.nextInt(8)-r.nextInt(8);y=floor(w,x,z);
            if(!water(w,x,y,z))return;
            boolean tall=r.nextDouble()<config.get("probability").getAsFloat();
            if(!support(w,x,y-1,z))return;
            if(tall){if(water(w,x,y+1,z)){w.setBlock(x,y,z,OceanContent.marinePlant,1,2);w.setBlock(x,y+1,z,OceanContent.marinePlant,2,2);}}
            else w.setBlock(x,y,z,OceanContent.marinePlant,0,2);
            return;
        }
        if(type.equals("minecraft:sea_pickle")){
            int count=FuturaPlacementData120.count(r,config.get("count"));
            for(int i=0;i<count;i++){
                int px=x+r.nextInt(8)-r.nextInt(8),pz=z+r.nextInt(8)-r.nextInt(8),py=floor(w,px,pz);
                int amount=r.nextInt(4);
                if(water(w,px,py,pz)&&support(w,px,py-1,pz))w.setBlock(px,py,pz,OceanContent.marinePlant,8+amount,2);
            }
            return;
        }
        throw new IllegalArgumentException("Unknown aquatic feature: "+type);
    }
    private static void kelp(World w,Random r,int x,int z){
        int y=floor(w,x,z);if(!water(w,x,y,z))return;
        int height=1+r.nextInt(10);
        for(int i=0;i<=height;i++,y++){
            if(water(w,x,y,z)&&water(w,x,y+1,z)&&support(w,x,y-1,z)){
                if(i==height)w.setBlock(x,y,z,OceanContent.kelp,8+r.nextInt(4),2);
                else w.setBlock(x,y,z,OceanContent.kelpPlant,0,2);
            }else if(i>0){
                if(!support(w,x,y-2,z)||w.getBlock(x,y-2,z)==OceanContent.kelp)break;
                w.setBlock(x,y-1,z,OceanContent.kelp,8+r.nextInt(4),2);break;
            }
        }
    }
    private static boolean support(World w,int x,int y,int z){
        Block b=w.getBlock(x,y,z);return b==OceanContent.kelpPlant||b==OceanContent.kelp||b.getMaterial().blocksMovement();
    }
    private static boolean water(World w,int x,int y,int z){return y>=0&&y<w.getHeight()&&BlockKelp.isOpenWater(w.getBlock(x,y,z));}
    private static int floor(World w,int x,int z){
        int y=w.getHeightValue(x,z);while(y>0&&!w.getBlock(x,y-1,z).getMaterial().blocksMovement())y--;return y;
    }
}
