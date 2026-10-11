package com.mod.htgt6.minecraft1202.Module.World.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120;
import cpw.mods.fml.common.IWorldGenerator;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * Chunk-clipped replay of the six 1.20.2 stone placed features.  Candidate
 * chunks are replayed around the target so 64-block veins cross borders
 * without requesting or populating neighbouring chunks.
 */
public final class BountifulRockGenerator120 implements IWorldGenerator {
    public static final int VEIN_SIZE=64,LOWER_COUNT=2,UPPER_RARITY=6;
    private static final long[] SALTS={0x4752414e495445L,0x44494f52495445L,0x414e4445534954L};

    @Override public void generate(Random ignored,int chunkX,int chunkZ,World world,
                                   IChunkProvider generator,IChunkProvider provider){
        if(world.provider.dimensionId!=0)return;
        int shift=FuturaWorldType120.isFutura(world)?64:0;
        for(int type=0;type<3;type++)for(int originX=chunkX-1;originX<=chunkX+1;originX++)
            for(int originZ=chunkZ-1;originZ<=chunkZ+1;originZ++)
                replayOrigin(world,chunkX,chunkZ,originX,originZ,type,shift);
    }

    private static void replayOrigin(World world,int targetX,int targetZ,int originX,int originZ,
                                     int type,int shift){
        Random random=new Random(mix(world.getSeed()^SALTS[type]
                ^(long)originX*341873128712L^(long)originZ*132897987541L));
        for(int i=0;i<LOWER_COUNT;i++)vein(world,random,targetX,targetZ,
                (originX<<4)+random.nextInt(16),shift+random.nextInt(61),
                (originZ<<4)+random.nextInt(16),type*2);
        if(random.nextInt(UPPER_RARITY)==0)vein(world,random,targetX,targetZ,
                (originX<<4)+random.nextInt(16),shift+64+random.nextInt(65),
                (originZ<<4)+random.nextInt(16),type*2);
    }

    private static void vein(World world,Random random,int chunkX,int chunkZ,int x,int y,int z,int meta){
        float angle=random.nextFloat()*(float)Math.PI;
        double x0=x+MathHelper.sin(angle)*8.0F,x1=x-MathHelper.sin(angle)*8.0F;
        double z0=z+MathHelper.cos(angle)*8.0F,z1=z-MathHelper.cos(angle)*8.0F;
        double y0=y+random.nextInt(3)-2,y1=y+random.nextInt(3)-2;
        int clipMinX=chunkX<<4,clipMaxX=clipMinX+15,clipMinZ=chunkZ<<4,clipMaxZ=clipMinZ+15;
        for(int i=0;i<=VEIN_SIZE;i++){
            double cx=x0+(x1-x0)*i/VEIN_SIZE,cy=y0+(y1-y0)*i/VEIN_SIZE,
                    cz=z0+(z1-z0)*i/VEIN_SIZE;
            double scale=random.nextDouble()*4.0D;
            double radius=(MathHelper.sin(i*(float)Math.PI/VEIN_SIZE)+1.0F)*scale+1.0D;
            double half=radius/2.0D;
            int minX=Math.max(clipMinX,MathHelper.floor_double(cx-half));
            int maxX=Math.min(clipMaxX,MathHelper.floor_double(cx+half));
            int minY=Math.max(1,MathHelper.floor_double(cy-half));
            int maxY=Math.min(world.getHeight()-2,MathHelper.floor_double(cy+half));
            int minZ=Math.max(clipMinZ,MathHelper.floor_double(cz-half));
            int maxZ=Math.min(clipMaxZ,MathHelper.floor_double(cz+half));
            for(int px=minX;px<=maxX;px++)for(int py=minY;py<=maxY;py++)
                for(int pz=minZ;pz<=maxZ;pz++){
                    double dx=(px+.5D-cx)/half,dy=(py+.5D-cy)/half,dz=(pz+.5D-cz)/half;
                    if(dx*dx+dy*dy+dz*dz>=1.0D)continue;
                    Block host=world.getBlock(px,py,pz);
                    if(host==Blocks.stone||host==Backport120Content.deepslate)
                        world.setBlock(px,py,pz,BountifulContent.stone,meta,2);
                }
        }
    }

    static long mix(long value){value=(value^(value>>>33))*0xff51afd7ed558ccdL;
        value=(value^(value>>>33))*0xc4ceb9fe1a85ec53L;return value^(value>>>33);}
}
