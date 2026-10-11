package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.JsonObject;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/** LakeFeature 1.20.2: the 16x8x16 volume starts at the placed X/Z, without legacy -8. */
public final class FuturaLakeFeature120 {
    private FuturaLakeFeature120(){}
    public static boolean generate(World w,Random r,JsonObject c,int x,int y,int z){
        if(y<=4)return false;y-=4;boolean[] mask=new boolean[2048];int count=r.nextInt(4)+4;
        for(int i=0;i<count;i++){
            double sx=r.nextDouble()*6+3,sy=r.nextDouble()*4+2,sz=r.nextDouble()*6+3;
            double cx=r.nextDouble()*(16-sx-2)+1+sx/2,cy=r.nextDouble()*(8-sy-4)+2+sy/2,cz=r.nextDouble()*(16-sz-2)+1+sz/2;
            for(int dx=1;dx<15;dx++)for(int dz=1;dz<15;dz++)for(int dy=1;dy<7;dy++){
                double ax=(dx-cx)/(sx/2),ay=(dy-cy)/(sy/2),az=(dz-cz)/(sz/2);
                if(ax*ax+ay*ay+az*az<1)mask[index(dx,dy,dz)]=true;
            }
        }
        FuturaBlockState120 fluid=FuturaBlockState120.of(FuturaFlowerProvider120.state(r,c.getAsJsonObject("fluid"),x,y,z));
        for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++)for(int dy=0;dy<8;dy++)if(edge(mask,dx,dy,dz)){
            Block b=w.getBlock(x+dx,y+dy,z+dz);
            if(dy>=4&&b.getMaterial().isLiquid()||dy<4&&!b.getMaterial().isSolid()&&(b!=fluid.block||w.getBlockMetadata(x+dx,y+dy,z+dz)!=fluid.metadata))return false;
        }
        for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++)for(int dy=0;dy<8;dy++){
            if(!mask[index(dx,dy,dz)]||protectedBlock(w.getBlock(x+dx,y+dy,z+dz)))continue;
            w.setBlock(x+dx,y+dy,z+dz,dy>=4?Blocks.air:fluid.block,dy>=4?0:fluid.metadata,2);
        }
        FuturaBlockState120 barrier=FuturaBlockState120.of(FuturaFlowerProvider120.state(r,c.getAsJsonObject("barrier"),x,y,z));
        if(barrier.block!=Blocks.air)for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++)for(int dy=0;dy<8;dy++){
            if(!edge(mask,dx,dy,dz)||dy>=4&&r.nextInt(2)==0)continue;
            Block b=w.getBlock(x+dx,y+dy,z+dz);
            if(!b.getMaterial().isSolid()||protectedBlock(b)||b==Blocks.log||b==Blocks.log2||b==Blocks.leaves||b==Blocks.leaves2)continue;
            w.setBlock(x+dx,y+dy,z+dz,barrier.block,barrier.metadata,2);
        }
        if(fluid.block.getMaterial()==net.minecraft.block.material.Material.water)
            for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++)if(w.isBlockFreezable(x+dx,y+4,z+dz)&&!protectedBlock(w.getBlock(x+dx,y+4,z+dz)))w.setBlock(x+dx,y+4,z+dz,Blocks.ice,0,2);
        return true;
    }
    private static int index(int x,int y,int z){return (x*16+z)*8+y;}
    private static boolean edge(boolean[] mask,int x,int y,int z){
        return !mask[index(x,y,z)]&&(x<15&&mask[index(x+1,y,z)]||x>0&&mask[index(x-1,y,z)]||z<15&&mask[index(x,y,z+1)]||z>0&&mask[index(x,y,z-1)]||y<7&&mask[index(x,y+1,z)]||y>0&&mask[index(x,y-1,z)]);
    }
    private static boolean protectedBlock(Block b){return b==Blocks.bedrock||b==Blocks.mob_spawner||b==Blocks.chest||b==Blocks.end_portal_frame||b==com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.reinforcedDeepslate;}
}
