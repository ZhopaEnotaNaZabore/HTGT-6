package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import java.util.*;

/** OreFeature 1.20.2: unshifted X/Z origin, sphere pruning, visited mask and air exposure. */
public final class WorldGenBackport120Ore extends WorldGenerator {
    private final int size;
    private final float discard;
    private final List<Target> targets=new ArrayList<Target>();
    /** BlockMatchTest used by Nether ores; shares the modern sphere algorithm. */
    public WorldGenBackport120Ore(Block ore,int size,Block host){
        this.size=size;discard=0;
        targets.add(new Target(host,ore));
    }
    public WorldGenBackport120Ore(Block stoneOre,Block deepslateOre,int size){
        this.size=size;discard=0;
        targets.add(new Target("minecraft:stone_ore_replaceables",stoneOre,0));
        targets.add(new Target("minecraft:deepslate_ore_replaceables",deepslateOre,0));
    }
    public WorldGenBackport120Ore(JsonObject config){
        size=config.get("size").getAsInt();discard=config.get("discard_chance_on_air_exposure").getAsFloat();
        for(JsonElement entry:config.getAsJsonArray("targets")){
            JsonObject t=entry.getAsJsonObject();
            String name=t.getAsJsonObject("state").get("Name").getAsString().substring(10);
            int meta=name.equals("granite")?1:name.equals("diorite")?3:name.equals("andesite")?5:0;
            targets.add(new Target(t.getAsJsonObject("target").get("tag").getAsString(),block(name),meta));
        }
    }
    private static Block block(String name){
        if(name.equals("granite")||name.equals("diorite")||name.equals("andesite"))return Blocks.stone;
        // 1.7 has a single infested-stone block. Preserve silverfish behaviour.
        if(name.startsWith("infested_"))return Blocks.monster_egg;
        Object vanilla=Block.blockRegistry.getObject("minecraft:"+name);
        if(vanilla instanceof Block&&vanilla!=Blocks.air)return (Block)vanilla;
        StringBuilder field=new StringBuilder();boolean upper=false;
        for(char c:name.toCharArray()){if(c=='_'){upper=true;continue;}field.append(upper?Character.toUpperCase(c):c);upper=false;}
        try{return (Block)Backport120Content.class.getField(field.toString()).get(null);}
        catch(Exception e){throw new IllegalStateException("Unmapped vanilla ore state: "+name,e);}
    }
    @Override public boolean generate(World world,Random random,int x,int y,int z){
        float angle=random.nextFloat()*(float)Math.PI,spread=size/8.0F;
        int padding=(int)Math.ceil((size/16.0F*2.0F+1.0F)/2.0F);
        double x0=x+Math.sin(angle)*spread,x1=x-Math.sin(angle)*spread;
        double z0=z+Math.cos(angle)*spread,z1=z-Math.cos(angle)*spread;
        double y0=y+random.nextInt(3)-2,y1=y+random.nextInt(3)-2;
        int bx=x-(int)Math.ceil(spread)-padding,by=y-2-padding,bz=z-(int)Math.ceil(spread)-padding;
        int width=2*((int)Math.ceil(spread)+padding),height=2*(2+padding);
        boolean belowSurface=false;
        for(int px=bx;px<=bx+width&&!belowSurface;px++)for(int pz=bz;pz<=bz+width;pz++)
            if(loaded(world,px,pz)&&by<=world.getTopSolidOrLiquidBlock(px,pz)){belowSurface=true;break;}
        if(!belowSurface)return false;
        double[] spheres=new double[size*4];
        for(int i=0;i<size;i++){
            float t=(float)i/size;
            spheres[4*i]=x0+t*(x1-x0);spheres[4*i+1]=y0+t*(y1-y0);spheres[4*i+2]=z0+t*(z1-z0);
            double scale=random.nextDouble()*size/16.0D;
            spheres[4*i+3]=((MathHelper.sin((float)Math.PI*t)+1.0F)*scale+1.0D)/2.0D;
        }
        for(int i=0;i<size-1;i++)if(spheres[i*4+3]>0)for(int j=i+1;j<size;j++){
            if(spheres[j*4+3]<=0)continue;
            double dr=spheres[i*4+3]-spheres[j*4+3],dx=spheres[i*4]-spheres[j*4],
                    dy=spheres[i*4+1]-spheres[j*4+1],dz=spheres[i*4+2]-spheres[j*4+2];
            if(dr*dr>dx*dx+dy*dy+dz*dz)spheres[(dr>0?j:i)*4+3]=-1;
        }
        BitSet visited=new BitSet(width*height*width);boolean placed=false;
        for(int i=0;i<size;i++){
            double r=spheres[i*4+3],cx=spheres[i*4],cy=spheres[i*4+1],cz=spheres[i*4+2];
            if(r<0)continue;
            int minX=Math.max(MathHelper.floor_double(cx-r),bx),minY=Math.max(MathHelper.floor_double(cy-r),by),minZ=Math.max(MathHelper.floor_double(cz-r),bz);
            int maxX=Math.max(MathHelper.floor_double(cx+r),minX),maxY=Math.max(MathHelper.floor_double(cy+r),minY),maxZ=Math.max(MathHelper.floor_double(cz+r),minZ);
            for(int px=minX;px<=maxX;px++)for(int py=minY;py<=maxY;py++)for(int pz=minZ;pz<=maxZ;pz++){
                double dx=(px+.5-cx)/r,dy=(py+.5-cy)/r,dz=(pz+.5-cz)/r;
                if(dx*dx+dy*dy+dz*dz>=1||py<0||py>=world.getHeight())continue;
                int bit=px-bx+(py-by)*width+(pz-bz)*width*height;
                if(visited.get(bit))continue;visited.set(bit);
                if(!loaded(world,px,pz))continue;
                Block host=world.getBlock(px,py,pz);
                for(Target target:targets)if(target.matches(host)){
                    if(discard>0&&(discard>=1||random.nextFloat()<discard)&&exposed(world,px,py,pz))continue;
                    placed|=world.setBlock(px,py,pz,target.block,target.meta,2);break;
                }
            }
        }
        return placed;
    }
    private static boolean exposed(World world,int x,int y,int z){
        return air(world,x+1,y,z)||air(world,x-1,y,z)||air(world,x,y+1,z)
                ||air(world,x,y-1,z)||air(world,x,y,z+1)||air(world,x,y,z-1);
    }
    private static boolean loaded(World world,int x,int z){
        // The server provider tests residency without providing/populating a chunk.
        // Modern ore origins have no legacy +8 inset, so spheres can cross the
        // west/north edge of the 1.7 population region even on their first attempt.
        return world.getChunkProvider().chunkExists(x>>4,z>>4);
    }
    private static boolean air(World world,int x,int y,int z){
        if(y<0||y>=world.getHeight())return true;
        // Unknown neighbours are not evidence of air exposure.
        return loaded(world,x,z)&&world.isAirBlock(x,y,z);
    }
    private static final class Target{
        final String tag;final Block block;final int meta;
        final Block exactHost;
        Target(String tag,Block block,int meta){this.tag=tag;this.block=block;this.meta=meta;this.exactHost=null;}
        Target(Block host,Block block){this.tag="";this.block=block;this.meta=0;this.exactHost=host;}
        boolean matches(Block host){
            if(block==null)return false;
            if(exactHost!=null)return host==exactHost;
            boolean stone=host==Blocks.stone,deep=host==Backport120Content.deepslate||host==Backport120Content.tuff;
            return tag.equals("minecraft:base_stone_overworld")?stone||deep
                    :tag.equals("minecraft:stone_ore_replaceables")?stone:deep;
        }
    }
}
