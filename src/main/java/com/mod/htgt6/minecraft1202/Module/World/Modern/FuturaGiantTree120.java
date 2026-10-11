package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.*;
import java.util.*;
import net.minecraft.world.World;

/** 1.20.2 GiantTrunkPlacer, MegaPineFoliagePlacer and AlterGroundDecorator. */
public final class FuturaGiantTree120 {
    private FuturaGiantTree120(){}
    public static boolean supports(JsonObject c){
        if(!c.getAsJsonObject("trunk_placer").get("type").getAsString().equals("minecraft:giant_trunk_placer")
                ||!c.getAsJsonObject("foliage_placer").get("type").getAsString().equals("minecraft:mega_pine_foliage_placer"))return false;
        for(JsonElement e:c.getAsJsonArray("decorators"))if(!e.getAsJsonObject().get("type").getAsString().equals("minecraft:alter_ground"))return false;
        return true;
    }
    public static boolean generate(World w,Random r,JsonObject c,int x,int y,int z){
        JsonObject trunk=c.getAsJsonObject("trunk_placer"),foliage=c.getAsJsonObject("foliage_placer");
        int height=trunk.get("base_height").getAsInt()+r.nextInt(trunk.get("height_rand_a").getAsInt()+1)+r.nextInt(trunk.get("height_rand_b").getAsInt()+1);
        int crown=FuturaPlacementData120.count(r,foliage.get("crown_height")),radius=FuturaPlacementData120.count(r,foliage.get("radius"));
        int free=FuturaTreePlacement120.freeHeight(w,c,x,y,z,height);if(free<0)return false;
        Set<Position> logs=new HashSet<Position>();
        // TreeFeature sends soil writes through the trunk consumer too. They affect decorator ordering.
        for(int[] p:new int[][]{{0,0},{1,0},{0,1},{1,1}})
            if(FuturaTreePlacement120.dirt(w,r,c,x+p[0],y-1,z+p[1]))logs.add(new Position(x+p[0],y-1,z+p[1]));
        for(int dy=0;dy<free;dy++){
            log(w,r,c,logs,x,y+dy,z);
            if(dy<free-1){log(w,r,c,logs,x+1,y+dy,z);log(w,r,c,logs,x+1,y+dy,z+1);log(w,r,c,logs,x,y+dy,z+1);}
        }
        int offset=FuturaPlacementData120.count(r,foliage.get("offset")),previous=0;boolean leaves=false;
        for(int yy=y+free-crown+offset;yy<=y+free+offset;yy++){
            int distance=y+free-yy,base=radius+(int)Math.floor((float)distance/(float)crown*3.5F);
            int width=distance>0&&base==previous&&(yy&1)==0?base+1:base;
            for(int dx=-width;dx<=width+1;dx++)for(int dz=-width;dz<=width+1;dz++){
                int ax=Math.min(Math.abs(dx),Math.abs(dx-1)),az=Math.min(Math.abs(dz),Math.abs(dz-1));
                if(ax+az>=7||ax*ax+az*az>width*width)continue;
                if(FuturaTreePlacement120.replaceable(w,x+dx,yy,z+dz))leaves|=FuturaTreePlacement120.state(w,r,c.getAsJsonObject("foliage_provider"),x+dx,yy,z+dz);
            }
            previous=base;
        }
        if(logs.isEmpty()&&!leaves)return false;
        List<Position> ordered=new ArrayList<Position>(logs);
        Collections.sort(ordered,new Comparator<Position>(){public int compare(Position a,Position b){return Integer.compare(a.y,b.y);}});
        for(JsonElement decorator:c.getAsJsonArray("decorators")){
            JsonObject provider=decorator.getAsJsonObject().getAsJsonObject("provider");
            for(Position p:ordered){
                if(p.y!=ordered.get(0).y)break;
                circle(w,r,provider,p.x-1,p.y,p.z-1);circle(w,r,provider,p.x+2,p.y,p.z-1);
                circle(w,r,provider,p.x-1,p.y,p.z+2);circle(w,r,provider,p.x+2,p.y,p.z+2);
                for(int i=0;i<5;i++){int n=r.nextInt(64),px=n%8,pz=n/8;
                    if(px==0||px==7||pz==0||pz==7)circle(w,r,provider,p.x-3+px,p.y,p.z-3+pz);
                }
            }
        }
        return true;
    }
    private static void log(World w,Random r,JsonObject c,Set<Position> logs,int x,int y,int z){
        if(FuturaTreePlacement120.replaceable(w,x,y,z)&&FuturaTreePlacement120.state(w,r,c.getAsJsonObject("trunk_provider"),x,y,z))logs.add(new Position(x,y,z));
    }
    private static void circle(World w,Random r,JsonObject provider,int x,int y,int z){
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++){
            if(Math.abs(dx)==2&&Math.abs(dz)==2)continue;
            for(int dy=2;dy>=-3;dy--){
                if(FuturaTreePlacement120.soil(w.getBlock(x+dx,y+dy,z+dz))){
                    FuturaBlockState120 state=FuturaBlockState120.of(FuturaFlowerProvider120.state(r,provider,x+dx,y,z+dz));
                    w.setBlock(x+dx,y+dy,z+dz,state.block,state.metadata,2);break;
                }
                if(dy<0&&!w.isAirBlock(x+dx,y+dy,z+dz))break;
            }
        }
    }
    private static final class Position implements Comparable<Position>{
        final int x,y,z;
        Position(int x,int y,int z){this.x=x;this.y=y;this.z=z;}
        // Retain BlockPos HashSet iteration in modern coordinates despite the +64 storage shift.
        public int hashCode(){return (y-64+z*31)*31+x;}
        public boolean equals(Object o){if(!(o instanceof Position))return false;Position p=(Position)o;return x==p.x&&y==p.y&&z==p.z;}
        public int compareTo(Position p){int d=Integer.compare(y,p.y);if(d==0)d=Integer.compare(z,p.z);return d==0?Integer.compare(x,p.x):d;}
    }
}
