package com.mod.htgt6.sulimited.worldgen.constructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;

/** Seam-crossing deterministic cave tubes with configurable host blocks. */
public final class CaveConstructor implements SUChunkConstructor {
    private final Set<Block> replace = new HashSet<Block>();
    private final int rarity, minY, maxY;
    private final long salt;

    public CaveConstructor(int rarity, int minY, int maxY, long salt, Block... replaceBlocks) {
        this.rarity=Math.max(1,rarity);this.minY=Math.max(2,minY);this.maxY=Math.min(250,maxY);this.salt=salt;
        replace.addAll(Arrays.asList(replaceBlocks));
    }

    public void generate(SUChunkContext context) {
        int cellSize=64, chunkMinX=context.chunkX*16, chunkMinZ=context.chunkZ*16;
        int baseCellX=floorDiv(chunkMinX,cellSize),baseCellZ=floorDiv(chunkMinZ,cellSize);
        for(int cx=baseCellX-2;cx<=baseCellX+2;cx++)for(int cz=baseCellZ-2;cz<=baseCellZ+2;cz++){
            Random random=context.featureRandom(salt,cx,cz);if(random.nextInt(rarity)!=0)continue;
            double x=cx*cellSize+random.nextInt(cellSize),z=cz*cellSize+random.nextInt(cellSize);
            double y=minY+random.nextInt(Math.max(1,maxY-minY+1));double yaw=random.nextDouble()*Math.PI*2.0;
            int length=32+random.nextInt(45);
            for(int step=0;step<length;step++){
                double radius=1.4+Math.sin(step*Math.PI/length)*(1.5+random.nextDouble()*1.8);
                carve(context,x,y,z,radius,radius*0.72);
                x+=Math.cos(yaw)*1.35;z+=Math.sin(yaw)*1.35;y+=Math.sin(step*0.31+salt)*0.22;
                yaw+=(random.nextDouble()-0.5)*0.16;
            }
        }
    }
    private void carve(SUChunkContext c,double wx,double wy,double wz,double rx,double ry){int minX=(int)Math.floor(wx-rx)-c.chunkX*16,maxX=(int)Math.ceil(wx+rx)-c.chunkX*16,minZ=(int)Math.floor(wz-rx)-c.chunkZ*16,maxZ=(int)Math.ceil(wz+rx)-c.chunkZ*16;for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++)for(int y=(int)Math.floor(wy-ry);y<=(int)Math.ceil(wy+ry);y++){double dx=(c.worldX(x)+0.5-wx)/rx,dy=(y+0.5-wy)/ry,dz=(c.worldZ(z)+0.5-wz)/rx;if(dx*dx+dy*dy+dz*dz<1&&replace.contains(c.getBlock(x,y,z)))c.setBlock(x,y,z,null,0);}}
    private int floorDiv(int a,int b){int q=a/b;return(a^b)<0&&q*b!=a?q-1:q;}
}
