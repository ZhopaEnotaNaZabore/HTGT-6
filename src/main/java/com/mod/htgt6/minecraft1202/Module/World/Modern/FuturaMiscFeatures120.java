package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import java.util.Random;

/** Small configured features that share the common placed-feature executor. */
public final class FuturaMiscFeatures120 {
    private static final int[][] DIR={{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
    private FuturaMiscFeatures120(){}
    public static void populate(World w,int cx,int cz,int step,String key,FuturaUndergroundFeatures120 biomes){
        JsonObject c=FuturaPlacementData120.INSTANCE.configured(FuturaPlacementData120.INSTANCE.placed(key).get("feature").getAsString());
        FuturaPlacementExecutor120.execute(w,cx,cz,step,key,biomes,(world,r,x,y,z)->generate(world,r,c.get("type").getAsString(),c.getAsJsonObject("config"),x,y,z));
    }
    public static void generate(World w,Random r,String type,JsonObject c,int x,int y,int z){
        if(type.equals("minecraft:disk")){disk(w,r,c,x,y,z);return;}
        if(type.equals("minecraft:forest_rock")){rock(w,r,c,x,y,z);return;}
        if(type.equals("minecraft:blue_ice")){blueIce(w,r,x,y,z);return;}
        if(type.equals("minecraft:underwater_magma")){magma(w,r,c,x,y,z);return;}
        if(type.equals("minecraft:vines")){vine(w,x,y,z);return;}
        if(type.equals("minecraft:ice_spike")){iceSpike(w,r,x,y,z);return;}
        if(type.equals("minecraft:desert_well")){well(w,r,x,y,z);return;}
        if(type.equals("minecraft:iceberg")){FuturaIcebergFeature120.generate(w,r,x,z,FuturaBlockState120.of(c.getAsJsonObject("state")).block);return;}
        if(type.equals("minecraft:fossil")){com.mod.htgt6.minecraft1202.Module.World.Frostburn.FrostburnFossilGenerator.generateConfigured(w,r,c,x,y,z);return;}
        throw new IllegalArgumentException("Unimplemented configured feature: "+type);
    }
    private static void disk(World w,Random r,JsonObject c,int x,int y,int z){
        int radius=FuturaPlacementData120.count(r,c.get("radius")),half=c.get("half_height").getAsInt();
        JsonObject provider=c.getAsJsonObject("state_provider");
        for(int pz=z-radius;pz<=z+radius;pz++)for(int px=x-radius;px<=x+radius;px++){
            if((px-x)*(px-x)+(pz-z)*(pz-z)>radius*radius)continue;
            for(int py=y+half;py>=y-half;py--){
                if(py<0||py>=w.getHeight()||!FuturaBlockState120.predicate(w,c.getAsJsonObject("target"),px,py,pz))continue;
                JsonObject state=provider.getAsJsonObject("fallback");
                for(JsonElement entry:provider.getAsJsonArray("rules")){JsonObject rule=entry.getAsJsonObject();
                    if(FuturaBlockState120.predicate(w,rule.getAsJsonObject("if_true"),px,py,pz)){state=rule.getAsJsonObject("then");break;}}
                FuturaBlockState120 selected=FuturaBlockState120.of(FuturaFlowerProvider120.state(r,state,px,py,pz));
                w.setBlock(px,py,pz,selected.block,selected.metadata,2);
            }
        }
    }
    private static void rock(World w,Random r,JsonObject c,int x,int y,int z){
        while(y>3){Block b=w.getBlock(x,y-1,z);if(b==Blocks.dirt||b==Blocks.grass||b==Blocks.stone||b==Backport120Content.deepslate)break;y--;}
        if(y<=3)return;FuturaBlockState120 state=FuturaBlockState120.of(c.getAsJsonObject("state"));
        for(int i=0;i<3;i++){
            int rx=r.nextInt(2),ry=r.nextInt(2),rz=r.nextInt(2);float radius=(rx+ry+rz)*.333F+.5F;
            for(int pz=z-rz;pz<=z+rz;pz++)for(int py=y-ry;py<=y+ry;py++)for(int px=x-rx;px<=x+rx;px++)
                if((px-x)*(px-x)+(py-y)*(py-y)+(pz-z)*(pz-z)<=radius*radius)w.setBlock(px,py,pz,state.block,state.metadata,2);
            x+=-1+r.nextInt(2);y-=r.nextInt(2);z+=-1+r.nextInt(2);
        }
    }
    private static void blueIce(World w,Random r,int x,int y,int z){
        if(y>126||!water(w,x,y,z)&&!water(w,x,y-1,z))return;
        boolean packed=false;for(int d=1;d<6;d++)if(w.getBlock(x+DIR[d][0],y+DIR[d][1],z+DIR[d][2])==Blocks.packed_ice){packed=true;break;}
        if(!packed)return;w.setBlock(x,y,z,Backport120Content.blueIce,0,2);
        for(int i=0;i<200;i++){
            int dy=r.nextInt(5)-r.nextInt(6),radius=3;if(dy<2)radius+=dy/2;if(radius<1)continue;
            int px=x+r.nextInt(radius)-r.nextInt(radius),py=y+dy,pz=z+r.nextInt(radius)-r.nextInt(radius);Block b=w.getBlock(px,py,pz);
            if(b!=Blocks.air&&b!=Blocks.packed_ice&&b!=Blocks.ice&&!water(w,px,py,pz))continue;
            for(int[] d:DIR)if(w.getBlock(px+d[0],py+d[1],pz+d[2])==Backport120Content.blueIce){w.setBlock(px,py,pz,Backport120Content.blueIce,0,2);break;}
        }
    }
    private static void magma(World w,Random r,JsonObject c,int x,int y,int z){
        if(!water(w,x,y,z))return;int range=c.get("floor_search_range").getAsInt(),floor=y;boolean found=false;
        for(int i=0;i<range&&floor>0;i++){floor--;if(!water(w,x,floor,z)){found=true;break;}}
        if(!found)return;int radius=c.get("placement_radius_around_floor").getAsInt();
        for(int pz=z-radius;pz<=z+radius;pz++)for(int py=floor-radius;py<=floor+radius;py++)for(int px=x-radius;px<=x+radius;px++){
            if(r.nextFloat()>=c.get("placement_probability_per_valid_position").getAsFloat())continue;
            if(hole(w,px,py,pz)||hole(w,px,py-1,pz))continue;boolean valid=true;
            for(int d=2;d<6;d++)if(hole(w,px+DIR[d][0],py,pz+DIR[d][2])){valid=false;break;}
            if(valid)w.setBlock(px,py,pz,com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.magmaBlock,0,2);
        }
    }
    private static void vine(World w,int x,int y,int z){
        if(!w.isAirBlock(x,y,z))return;
        int[] metadata={0,0,4,1,8,2};
        for(int d=1;d<6;d++)if(w.getBlock(x+DIR[d][0],y+DIR[d][1],z+DIR[d][2]).getMaterial().blocksMovement()){
            w.setBlock(x,y,z,Blocks.vine,metadata[d],2);return;}
    }
    private static boolean water(World w,int x,int y,int z){return w.getBlock(x,y,z).getMaterial()==net.minecraft.block.material.Material.water;}
    private static boolean hole(World w,int x,int y,int z){return w.isAirBlock(x,y,z)||water(w,x,y,z);}
    private static boolean iceReplaceable(Block b){return b==Blocks.air||b==Blocks.dirt||b==Blocks.grass||b==Blocks.snow||b==Blocks.ice;}
    private static void iceSpike(World w,Random r,int x,int y,int z){
        while(w.isAirBlock(x,y,z)&&y>2)y--;if(w.getBlock(x,y,z)!=Blocks.snow)return;
        y+=r.nextInt(4);int height=7+r.nextInt(4),radius=height/4+r.nextInt(2);
        if(radius>1&&r.nextInt(60)==0)y+=10+r.nextInt(30);
        for(int dy=0;dy<height;dy++){
            float width=(1F-(float)dy/height)*radius;int bound=(int)Math.ceil(width);
            for(int dx=-bound;dx<=bound;dx++)for(int dz=-bound;dz<=bound;dz++){
                float ax=Math.abs(dx)-.25F,az=Math.abs(dz)-.25F;
                if((dx!=0||dz!=0)&&ax*ax+az*az>width*width||(Math.abs(dx)==bound||Math.abs(dz)==bound)&&r.nextFloat()>.75F)continue;
                if(iceReplaceable(w.getBlock(x+dx,y+dy,z+dz)))w.setBlock(x+dx,y+dy,z+dz,Blocks.packed_ice,0,2);
                if(dy!=0&&bound>1&&iceReplaceable(w.getBlock(x+dx,y-dy,z+dz)))w.setBlock(x+dx,y-dy,z+dz,Blocks.packed_ice,0,2);
            }
        }
        int root=Math.max(0,Math.min(1,radius-1));
        for(int dx=-root;dx<=root;dx++)for(int dz=-root;dz<=root;dz++){
            int py=y-1,left=Math.abs(dx)==1&&Math.abs(dz)==1?r.nextInt(5):50;
            while(py>114){Block b=w.getBlock(x+dx,py,z+dz);if(!iceReplaceable(b)&&b!=Blocks.packed_ice)break;
                w.setBlock(x+dx,py,z+dz,Blocks.packed_ice,0,2);py--;
                if(--left<=0){py-=r.nextInt(5)+1;left=r.nextInt(5);}}
        }
    }
    private static net.minecraft.item.ItemStack wellLoot(Random random){
        int entry=random.nextInt(8);
        if(entry<4)return new net.minecraft.item.ItemStack(Backport120Content.potterySherds[entry<2?2:4]);
        if(entry==4)return new net.minecraft.item.ItemStack(net.minecraft.init.Items.brick);
        if(entry==5)return new net.minecraft.item.ItemStack(net.minecraft.init.Items.emerald);
        if(entry==6)return new net.minecraft.item.ItemStack(net.minecraft.init.Items.stick);
        return com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemSuspiciousStew120.create(random);
    }
    private static void well(World w,Random r,int x,int y,int z){
        y++;while(w.isAirBlock(x,y,z)&&y>2)y--;if(w.getBlock(x,y,z)!=Blocks.sand)return;
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)if(w.isAirBlock(x+dx,y-1,z+dz)&&w.isAirBlock(x+dx,y-2,z+dz))return;
        for(int dy=-2;dy<=0;dy++)for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)w.setBlock(x+dx,y+dy,z+dz,Blocks.sandstone,0,2);
        int[][] cross={{0,0},{1,0},{0,1},{-1,0},{0,-1}};
        for(int[] p:cross){w.setBlock(x+p[0],y,z+p[1],Blocks.water,0,2);w.setBlock(x+p[0],y-1,z+p[1],Blocks.sand,0,2);}
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)if(Math.abs(dx)==2||Math.abs(dz)==2)w.setBlock(x+dx,y+1,z+dz,Blocks.sandstone,0,2);
        for(int d=2;d<6;d++)w.setBlock(x+DIR[d][0]*2,y+1,z+DIR[d][2]*2,Blocks.stone_slab,1,2);
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)w.setBlock(x+dx,y+4,z+dz,dx==0&&dz==0?Blocks.sandstone:Blocks.stone_slab,dx==0&&dz==0?0:1,2);
        for(int dy=1;dy<=3;dy++)for(int dx:new int[]{-1,1})for(int dz:new int[]{-1,1})w.setBlock(x+dx,y+dy,z+dz,Blocks.sandstone,0,2);
        for(int depth=1;depth<=2;depth++){
            int[] p=cross[r.nextInt(5)];int px=x+p[0],py=y-depth,pz=z+p[1];
            if(!w.setBlock(px,py,pz,Backport120Content.suspiciousSand,0,2))continue;
            net.minecraft.tileentity.TileEntity tile=w.getTileEntity(px,py,pz);
            if(tile instanceof com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Brushable){
                long lootSeed=((long)px&0x3ffffffL)<<38|((long)pz&0x3ffffffL)<<12|((py-64)&0xfffL);
                ((com.mod.htgt6.minecraft1202.Module.Blocks.Modern.TileEntityBackport120Brushable)tile).setLoot(wellLoot(new Random(lootSeed)));
            }
        }
    }
}
