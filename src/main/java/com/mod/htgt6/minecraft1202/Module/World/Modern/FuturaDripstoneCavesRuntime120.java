package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120PointedDripstone;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Random;

/** 1.20 Dripstone Caves UNDERGROUND_DECORATION and LOCAL_MODIFICATIONS runtime. */
public final class FuturaDripstoneCavesRuntime120 {
    public static final int LARGE_MIN=10,LARGE_MAX=48;
    public static final int CLUSTER_MIN=48,CLUSTER_MAX=96;
    public static final int POINTED_MIN=192,POINTED_MAX=256;
    public static final int MAX_ABSOLUTE_Y=256,FEATURE_MARGIN=20;
    private final FuturaMountainDensityRouter120 terrain;
    private final MutableDensityContext120 sample=new MutableDensityContext120();

    public FuturaDripstoneCavesRuntime120(FuturaMountainDensityRouter120 terrain){this.terrain=terrain;}

    public void populate(World world,Random random,int chunkX,int chunkZ,int generationHeight){
        populateFeature(world,random,chunkX,chunkZ,generationHeight,null);
    }
    public void populateFeature(World world,Random random,int chunkX,int chunkZ,int generationHeight,String selected){
        if(!(world.getWorldChunkManager() instanceof FuturaWorldChunkManager120))return;
        FuturaWorldChunkManager120 biomes=(FuturaWorldChunkManager120)world.getWorldChunkManager();
        int ox=chunkX<<4,oz=chunkZ<<4;
        int maxY=Math.min(generationHeight-2,FuturaMountainDensityRouter120.modernToFuturaY(MAX_ABSOLUTE_Y));
        random=FuturaPlacementData120.INSTANCE.random(world.getSeed(),chunkX,chunkZ,7,"dripstone_cluster");
        if(selected==null||selected.equals("minecraft:dripstone_cluster"))for(int i=0,n=between(random,CLUSTER_MIN,CLUSTER_MAX);i<n;i++){
            int x=ox+random.nextInt(16),z=oz+random.nextInt(16),y=random.nextInt(maxY+1);
            if(isDripstone(biomes,x,y,z))cluster(world,random,x,y,z,ox,oz);
        }
        random=FuturaPlacementData120.INSTANCE.random(world.getSeed(),chunkX,chunkZ,7,"pointed_dripstone");
        if(selected==null||selected.equals("minecraft:pointed_dripstone"))for(int i=0,n=between(random,POINTED_MIN,POINTED_MAX);i<n;i++){
            int x=ox+random.nextInt(16),z=oz+random.nextInt(16),y=random.nextInt(maxY+1);
            int repeats=between(random,1,5);
            for(int j=0;j<repeats;j++){
                int px=x+clampedGaussian(random,3,10),py=y+clampedGaussian(random,.6D,2);
                int pz=z+clampedGaussian(random,3,10);
                if(inside(px,pz,ox,oz)&&isDripstone(biomes,px,py,pz))pointed(world,random,px,py,pz);
            }
        }
    }

    public void populateLarge(World world,int chunkX,int chunkZ,int generationHeight){
        if(!(world.getWorldChunkManager() instanceof FuturaWorldChunkManager120))return;
        FuturaWorldChunkManager120 biomes=(FuturaWorldChunkManager120)world.getWorldChunkManager();
        Random random=FuturaPlacementData120.INSTANCE.random(world.getSeed(),chunkX,chunkZ,2,"large_dripstone");
        int ox=chunkX<<4,oz=chunkZ<<4,maxY=Math.min(generationHeight-1,320);
        for(int i=0,n=between(random,LARGE_MIN,LARGE_MAX);i<n;i++){
            int x=ox+random.nextInt(16),z=oz+random.nextInt(16),y=random.nextInt(maxY+1);
            if(isDripstone(biomes,x,y,z))largeDripstone(world,random,x,y,z,ox,oz);
        }
    }

    private boolean isDripstone(FuturaWorldChunkManager120 biomes,int x,int y,int z){
        return y>0&&biomes.getUndergroundBiomeAt(x,y,z,terrain,sample).biomeID
                ==FuturaBiomeIds120.DRIPSTONE_CAVES;
    }

    private static void largeDripstone(World world,Random random,int x,int y,int z,int ox,int oz){
        int[] gap=findGap(world,x,y,z,30);
        if(gap==null)return;
        int caveHeight=gap[1]-gap[0]-1;
        int radius=Math.min(between(random,3,19),Math.max(1,(int)(caveHeight*.33D)));
        if(radius<3)return;
        double heightScale=.4D+random.nextDouble()*1.6D;
        int maxColumn=Math.max(2,Math.min(caveHeight-1,(int)(radius*heightScale*2D)));
        boolean floor=random.nextBoolean();
        boolean ceiling=random.nextBoolean()||!floor;
        for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
            int px=x+dx,pz=z+dz;if(!inside(px,pz,ox,oz))continue;
            double distance=Math.sqrt(dx*dx+dz*dz)/radius;
            if(distance>1D)continue;
            int column=Math.max(0,(int)Math.round(maxColumn*(1D-distance)*(0.75D+random.nextDouble()*.25D)));
            if(column==0)continue;
            if(floor)solidCone(world,px,gap[0]+1,pz,1,column,gap[1]-1);
            if(ceiling)solidCone(world,px,gap[1]-1,pz,-1,column,gap[0]+1);
        }
    }

    private static void solidCone(World world,int x,int startY,int z,int step,int length,int limit){
        int placed=0;
        for(int y=startY;placed<length&&(step>0?y<=limit:y>=limit);y+=step){
            Block old=loadedBlock(world,x,y,z);
            if(old!=Blocks.air&&old!=Blocks.water&&old!=Blocks.flowing_water&&old!=Backport120Content.aquiferWater)break;
            if(!setLoaded(world,x,y,z,Backport120Content.dripstoneBlock,0))break;placed++;
        }
    }

    private static void cluster(World world,Random random,int x,int y,int z,int ox,int oz){
        int radius=between(random,2,8),targetHeight=between(random,3,6);
        for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
            int px=x+dx,pz=z+dz;if(!inside(px,pz,ox,oz))continue;
            double normalized=Math.sqrt(dx*dx+dz*dz)/radius;
            double chance=.7D*(1D-normalized)+.1D*normalized;
            if(normalized>1D||random.nextDouble()>chance)continue;
            int[] gap=findGap(world,px,y+random.nextInt(7)-3,pz,12);
            if(gap==null)continue;
            int layer=between(random,2,4);
            coat(world,px,gap[0],pz,-1,layer);
            coat(world,px,gap[1],pz,1,layer);
            int max=Math.max(1,targetHeight-random.nextInt(4));
            int floorLength=Math.min(max,Math.max(0,(gap[1]-gap[0]-2)/2));
            int ceilingLength=Math.min(floorLength+random.nextInt(3)-1,Math.max(0,gap[1]-gap[0]-2-floorLength));
            if(floorLength>0)placeColumn(world,px,gap[0]+1,pz,false,floorLength);
            if(ceilingLength>0)placeColumn(world,px,gap[1]-1,pz,true,ceilingLength);
        }
    }

    private static void coat(World world,int x,int y,int z,int solidStep,int thickness){
        for(int i=0;i<thickness;i++,y+=solidStep){
            Block old=loadedBlock(world,x,y,z);
            if(!replaceable(old))break;
            if(!setLoaded(world,x,y,z,Backport120Content.dripstoneBlock,0))break;
        }
    }

    private static void pointed(World world,Random random,int x,int y,int z){
        if(!isCaveCell(loadedBlock(world,x,y,z)))return;
        boolean down=random.nextBoolean();
        int surface=scanSolid(world,x,y,z,down?1:-1,12);
        if(surface<0){down=!down;surface=scanSolid(world,x,y,z,down?1:-1,12);}
        if(surface<0)return;
        int rootY=surface+(down?-1:1);
        spreadDripstoneBlock(world,random,x,surface,z);
        int length=random.nextFloat()<.2F?2:1;
        placeColumn(world,x,rootY,z,down,length);
    }

    private static void spreadDripstoneBlock(World world,Random random,int x,int y,int z){
        if(replaceable(loadedBlock(world,x,y,z)))setLoaded(world,x,y,z,Backport120Content.dripstoneBlock,0);
        int[][] offsets={{1,0},{-1,0},{0,1},{0,-1},{2,0},{-2,0},{0,2},{0,-2},{1,1},{1,-1},{-1,1},{-1,-1}};
        for(int i=0;i<offsets.length;i++){
            float chance=i<4?.7F:i<8?.35F:.175F;
            int px=x+offsets[i][0],pz=z+offsets[i][1];
            if(random.nextFloat()<chance&&replaceable(loadedBlock(world,px,y,pz)))
                setLoaded(world,px,y,pz,Backport120Content.dripstoneBlock,0);
        }
    }

    private static void placeColumn(World world,int x,int rootY,int z,boolean down,int requested){
        int step=down?-1:1,length=0;
        while(length<requested&&isCaveCell(loadedBlock(world,x,rootY+step*length,z)))length++;
        for(int i=0;i<length;i++){
            int thickness=length==1||i==length-1?BlockBackport120PointedDripstone.TIP
                    :i==0?BlockBackport120PointedDripstone.BASE
                    :i==length-2?BlockBackport120PointedDripstone.FRUSTUM
                    :BlockBackport120PointedDripstone.MIDDLE;
            if(!setLoaded(world,x,rootY+step*i,z,Backport120Content.pointedDripstone,
                    BlockBackport120PointedDripstone.metadata(down,thickness)))break;
        }
    }

    private static int[] findGap(World world,int x,int y,int z,int range){
        if(!isCaveCell(loadedBlock(world,x,y,z)))return null;
        int floor=scanSolid(world,x,y,z,-1,range),ceiling=scanSolid(world,x,y,z,1,range);
        return floor>=1&&ceiling>floor+2?new int[]{floor,ceiling}:null;
    }
    private static int scanSolid(World world,int x,int y,int z,int step,int range){
        for(int i=0;i<=range;i++,y+=step){Block block=loadedBlock(world,x,y,z);if(!isCaveCell(block))
            return block.getMaterial().isSolid()?y:-1;}
        return -1;
    }
    /** Population must never synchronously provide an adjacent chunk. */
    private static Block loadedBlock(World world,int x,int y,int z){
        if(y<0||y>=world.getActualHeight()||!world.getChunkProvider().chunkExists(x>>4,z>>4))return Blocks.bedrock;
        return world.getBlock(x,y,z);
    }
    private static boolean setLoaded(World world,int x,int y,int z,Block block,int metadata){
        return y>=0&&y<world.getActualHeight()&&world.getChunkProvider().chunkExists(x>>4,z>>4)
                &&world.setBlock(x,y,z,block,metadata,2);
    }
    private static boolean isCaveCell(Block b){return b==Blocks.air||b==Blocks.water||b==Blocks.flowing_water
            ||b==Backport120Content.aquiferWater;}
    private static boolean replaceable(Block b){return b==Blocks.stone||b==Backport120Content.deepslate
            ||b==Blocks.dirt||b==Blocks.gravel||b==Backport120Content.tuff||b==Backport120Content.dripstoneBlock;}
    private static int between(Random r,int min,int max){return min+r.nextInt(max-min+1);}
    private static int clampedGaussian(Random r,double deviation,int limit){
        return Math.max(-limit,Math.min(limit,(int)Math.round(r.nextGaussian()*deviation)));
    }
    private static boolean inside(int x,int z,int ox,int oz){return x>=ox-FEATURE_MARGIN&&x<=ox+15+FEATURE_MARGIN
            &&z>=oz-FEATURE_MARGIN&&z<=oz+15+FEATURE_MARGIN;}
}
