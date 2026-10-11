package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import java.util.*;

/** GeodeFeature 1.20.2; block coordinates are physical, noise coordinates are modern. */
public final class FuturaGeodeFeature120 {
    private final NormalNoise120 noise;
    private static final int[][] DIRECTIONS={{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
    public FuturaGeodeFeature120(long seed){noise=new NormalNoise120(new Random(seed),-4,1D);}
    public boolean generate(World w,Random r,JsonObject c,int x,int y,int z){
        int points=FuturaPlacementData120.count(r,c.get("distribution_points"));
        JsonObject wall=c.getAsJsonObject("outer_wall_distance");
        JsonObject wallValue=wall.has("value")?wall.getAsJsonObject("value"):wall;
        double offset=(double)points/wallValue.get("max_inclusive").getAsInt();
        JsonObject layers=c.getAsJsonObject("layers"),crack=c.getAsJsonObject("crack");
        double filling=1/Math.sqrt(layers.get("filling").getAsDouble());
        double inner=1/Math.sqrt(layers.get("inner_layer").getAsDouble()+offset);
        double middle=1/Math.sqrt(layers.get("middle_layer").getAsDouble()+offset);
        double outer=1/Math.sqrt(layers.get("outer_layer").getAsDouble()+offset);
        double crackThreshold=1/Math.sqrt(crack.get("base_crack_size").getAsDouble()+r.nextDouble()/2+(points>3?offset:0));
        boolean cracked=r.nextFloat()<crack.get("generate_crack_chance").getAsDouble();
        List<int[]> centers=new ArrayList<int[]>(),cracks=new ArrayList<int[]>(),buds=new ArrayList<int[]>();
        int invalid=0;
        for(int i=0;i<points;i++){
            int px=x+FuturaPlacementData120.count(r,wall),py=y+FuturaPlacementData120.count(r,wall),pz=z+FuturaPlacementData120.count(r,wall);
            if(invalid(w.getBlock(px,py,pz))&&++invalid>c.get("invalid_blocks_threshold").getAsInt())return false;
            centers.add(new int[]{px,py,pz,FuturaPlacementData120.count(r,c.get("point_offset"))});
        }
        if(cracked){
            int direction=r.nextInt(4),distance=points*2+1;
            int dx=direction==0||direction==2?distance:0,dz=direction==1||direction==2?distance:0;
            for(int dy:new int[]{7,5,1})cracks.add(new int[]{x+dx,y+dy,z+dz,crack.get("crack_point_offset").getAsInt()});
        }
        int min=c.get("min_gen_offset").getAsInt(),max=c.get("max_gen_offset").getAsInt();
        double multiplier=c.get("noise_multiplier").getAsDouble();
        for(int pz=z+min;pz<=z+max;pz++)for(int py=y+min;py<=y+max;py++)for(int px=x+min;px<=x+max;px++){
            double perturbation=noise.sample(px,py-64,pz)*multiplier;
            double field=field(centers,px,py,pz,perturbation),crackField=field(cracks,px,py,pz,perturbation);
            if(field<outer)continue;
            if(cracked&&crackField>=crackThreshold&&field<filling){
                set(w,px,py,pz,Blocks.air,0);
                for(int[] direction:DIRECTIONS){int ax=px+direction[0],ay=py+direction[1],az=pz+direction[2];
                    Block fluid=w.getBlock(ax,ay,az);if(fluid.getMaterial().isLiquid())w.scheduleBlockUpdate(ax,ay,az,fluid,0);}
            }else if(field>=filling)set(w,px,py,pz,Blocks.air,0);
            else if(field>=inner){
                boolean alternate=r.nextFloat()<c.get("use_alternate_layer0_chance").getAsDouble();
                set(w,px,py,pz,alternate?Backport120Content.buddingAmethyst:Backport120Content.amethystBlock,0);
                if((!c.get("placements_require_layer0_alternate").getAsBoolean()||alternate)
                        &&r.nextFloat()<c.get("use_potential_placements_chance").getAsDouble())buds.add(new int[]{px,py,pz});
            }else if(field>=middle)set(w,px,py,pz,Backport120Content.calcite,0);
            else set(w,px,py,pz,Backport120Content.smoothBasalt,0);
        }
        Block[] states={Backport120Content.smallAmethystBud,Backport120Content.mediumAmethystBud,Backport120Content.largeAmethystBud,Backport120Content.amethystCluster};
        for(int[] p:buds){Block bud=states[r.nextInt(states.length)];
            for(int direction=0;direction<DIRECTIONS.length;direction++){
                int px=p[0]+DIRECTIONS[direction][0],py=p[1]+DIRECTIONS[direction][1],pz=p[2]+DIRECTIONS[direction][2];
                Block existing=w.getBlock(px,py,pz);
                if(existing!=Blocks.air&&!(existing.getMaterial()==net.minecraft.block.material.Material.water&&w.getBlockMetadata(px,py,pz)==0))continue;
                set(w,px,py,pz,bud,direction);break;
            }
        }
        return true;
    }
    private static double field(List<int[]> points,int x,int y,int z,double noise){
        double result=0;for(int[] p:points){double dx=x-p[0],dy=y-p[1],dz=z-p[2];result+=1/Math.sqrt(dx*dx+dy*dy+dz*dz+p[3])+noise;}return result;
    }
    private static boolean invalid(Block b){return b==Blocks.air||b==Blocks.bedrock||b.getMaterial().isLiquid()
            ||b==Blocks.ice||b==Blocks.packed_ice||b==Backport120Content.blueIce;}
    private static void set(World w,int x,int y,int z,Block b,int meta){
        if(y<0||y>=w.getHeight())return;
        Block old=w.getBlock(x,y,z);
        if(old==Blocks.bedrock||old==Blocks.mob_spawner||old==Blocks.chest||old==Blocks.end_portal_frame
                ||old==Backport120Content.reinforcedDeepslate)return;
        w.setBlock(x,y,z,b,meta,2);
    }
}
