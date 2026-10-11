package com.mod.htgt6.minecraft1202.Module.World.Modern;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import java.util.Random;

/** IcebergFeature 1.20.2, translated to physical sea level. */
public final class FuturaIcebergFeature120 {
    private FuturaIcebergFeature120(){}
    public static void generate(World w,Random r,int x,int z,Block state){
        int y=FuturaMountainDensityRouter120.SEA_LEVEL;
        boolean snow=r.nextDouble()>.7;double angle=r.nextDouble()*2*Math.PI;
        int length=11-r.nextInt(5),narrow=3+r.nextInt(3);boolean ellipse=r.nextDouble()>.7;
        int height=ellipse?r.nextInt(6)+6:r.nextInt(15)+3;
        if(!ellipse&&r.nextDouble()>.9)height+=r.nextInt(19)+7;
        int depth=Math.min(height+r.nextInt(11),18),radius=Math.min(height+r.nextInt(7)-r.nextInt(5),11),bound=ellipse?length:11;
        for(int dx=-bound;dx<bound;dx++)for(int dz=-bound;dz<bound;dz++)for(int dy=0;dy<height;dy++){
            int width=ellipse?ellipticalWidth(dy,height,radius):aboveWidth(r,dy,height,radius);
            if(!ellipse&&dx>=width)continue;
            point(w,r,x,y,z,height,dx,dy,dz,width,bound,ellipse,narrow,angle,snow,state);
        }
        smooth(w,x,y,z,radius,height,ellipse,length);
        for(int dx=-bound;dx<bound;dx++)for(int dz=-bound;dz<bound;dz++)for(int dy=-1;dy>-depth;dy--){
            int horizontal=ellipse?(int)Math.ceil((float)bound*(1F-(float)Math.pow(dy,2)/((float)depth*8F))):bound;
            int width=belowWidth(r,-dy,depth,radius);if(dx>=width)continue;
            point(w,r,x,y,z,depth,dx,dy,dz,width,horizontal,ellipse,narrow,angle,snow,state);
        }
        if(r.nextDouble()>(ellipse?.1:.7))cut(w,r,x,y,z,radius,height,ellipse,length,angle,narrow);
    }
    private static void point(World w,Random r,int x,int y,int z,int height,int dx,int dy,int dz,int width,int bound,boolean ellipse,int narrow,double angle,boolean snow,Block state){
        int minor=narrow;if(dy>0&&height-dy<=3)minor-=4-(height-dy);
        double distance=ellipse?ellipse(dx,dz,0,0,bound,minor,angle)
                :(double)(10F*(.2F+r.nextFloat()*(.8F-.2F))/(float)width)+Math.pow(dx,2)+Math.pow(dz,2)-Math.pow(width,2);
        if(distance>=0||Double.isNaN(distance))return;
        double edge=ellipse?-.5:-6-r.nextInt(3);if(distance>edge&&r.nextDouble()>.9)return;
        Block old=w.getBlock(x+dx,y+dy,z+dz);
        if(old!=Blocks.air&&old!=Blocks.snow&&old!=Blocks.ice&&!water(old))return;
        boolean snowAllowed=!ellipse||r.nextDouble()>.05;
        if(snow&&!water(old)&&(double)(height-dy)<=r.nextInt(Math.max(1,height/(ellipse?3:2)))+(double)height*.6&&snowAllowed)
            w.setBlock(x+dx,y+dy,z+dz,Blocks.snow,0,2);
        else w.setBlock(x+dx,y+dy,z+dz,state,0,2);
    }
    private static int aboveWidth(Random r,int dy,int height,int radius){
        float scale=3.5F-r.nextFloat(),width=(1F-(float)Math.pow(dy,2)/((float)height*scale))*radius;
        if(height>15+r.nextInt(5)){int sample=dy<3+r.nextInt(6)?dy/2:dy;width=(1F-(float)sample/((float)height*scale*.4F))*radius;}
        return (int)Math.ceil(width/2F);
    }
    private static int ellipticalWidth(int dy,int height,int radius){return (int)Math.ceil(((1F-(float)Math.pow(dy,2)/(float)height)*radius)/2F);}
    private static int belowWidth(Random r,int dy,int height,int radius){float scale=1F+r.nextFloat()/2F;return (int)Math.ceil(((1F-(float)dy/((float)height*scale))*radius)/2F);}
    private static double ellipse(int x,int z,int ox,int oz,int major,int minor,double angle){
        return Math.pow(((x-ox)*Math.cos(angle)-(z-oz)*Math.sin(angle))/major,2)
                +Math.pow(((x-ox)*Math.sin(angle)+(z-oz)*Math.cos(angle))/minor,2)-1;
    }
    private static void cut(World w,Random r,int x,int y,int z,int radius,int height,boolean ellipse,int length,double angle,int narrow){
        int sx=r.nextBoolean()?-1:1,sz=r.nextBoolean()?-1:1;
        int ox=r.nextInt(Math.max(radius/2-2,1));if(r.nextBoolean())ox=radius/2+1-r.nextInt(Math.max(radius-radius/2-1,1));
        int oz=r.nextInt(Math.max(radius/2-2,1));if(r.nextBoolean())oz=radius/2+1-r.nextInt(Math.max(radius-radius/2-1,1));
        if(ellipse)ox=oz=r.nextInt(Math.max(length-5,1));
        double direction=ellipse?angle+Math.PI/2:r.nextDouble()*2*Math.PI;
        for(int dy=0;dy<height-3;dy++)cutLayer(w,x,y,z,aboveWidth(r,dy,height,radius),dy,false,direction,sx*ox,sz*oz,length,narrow);
        for(int dy=-1;dy>-height+r.nextInt(5);dy--)cutLayer(w,x,y,z,belowWidth(r,-dy,height,radius),dy,true,direction,sx*ox,sz*oz,length,narrow);
    }
    private static void cutLayer(World w,int x,int y,int z,int width,int dy,boolean underwater,double angle,int ox,int oz,int length,int narrow){
        int bound=width+1+length/3,minor=Math.min(width-3,3)+narrow/2-1;
        for(int dx=-bound;dx<bound;dx++)for(int dz=-bound;dz<bound;dz++){
            if(!(ellipse(dx,dz,ox,oz,bound,minor,angle)<0))continue;
            if(!ice(w.getBlock(x+dx,y+dy,z+dz)))continue;
            w.setBlock(x+dx,y+dy,z+dz,underwater?Blocks.water:Blocks.air,0,2);
            if(!underwater&&w.getBlock(x+dx,y+dy+1,z+dz)==Blocks.snow_layer)w.setBlock(x+dx,y+dy+1,z+dz,Blocks.air,0,2);
        }
    }
    private static void smooth(World w,int x,int y,int z,int radius,int height,boolean ellipse,int length){
        int bound=ellipse?length:radius/2;
        for(int dx=-bound;dx<=bound;dx++)for(int dz=-bound;dz<=bound;dz++)for(int dy=0;dy<=height;dy++){
            int px=x+dx,py=y+dy,pz=z+dz;Block b=w.getBlock(px,py,pz);
            if(!ice(b)&&b!=Blocks.snow_layer)continue;
            if(w.isAirBlock(px,py-1,pz)){w.setBlock(px,py,pz,Blocks.air,0,2);w.setBlock(px,py+1,pz,Blocks.air,0,2);continue;}
            if(!ice(b))continue;int exposed=0;
            for(int[] d:new int[][]{{0,1},{0,-1},{-1,0},{1,0}})if(!ice(w.getBlock(px+d[0],py,pz+d[1])))exposed++;
            if(exposed>=3)w.setBlock(px,py,pz,Blocks.air,0,2);
        }
    }
    private static boolean water(Block b){return b.getMaterial()==net.minecraft.block.material.Material.water;}
    private static boolean ice(Block b){return b==Blocks.packed_ice||b==Blocks.snow||b==com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.blueIce;}
}
