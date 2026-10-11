package com.mod.htgt6.sulimited.worldgen.constructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;

/** Sparse long fractures inspired by WE_RavineGen, generated without global state. */
public final class RavineConstructor implements SUChunkConstructor {
    private final Set<Block> replace = new HashSet<Block>();
    private final int rarity, minY, maxY;
    private final long salt;
    public RavineConstructor(int rarity,int minY,int maxY,long salt,Block... blocks){this.rarity=Math.max(1,rarity);this.minY=minY;this.maxY=maxY;this.salt=salt;replace.addAll(Arrays.asList(blocks));}
    public void generate(SUChunkContext context){int cell=160,minX=context.chunkX*16,minZ=context.chunkZ*16,cx=floorDiv(minX,cell),cz=floorDiv(minZ,cell);for(int gx=cx-1;gx<=cx+1;gx++)for(int gz=cz-1;gz<=cz+1;gz++){Random r=context.featureRandom(salt,gx,gz);if(r.nextInt(rarity)!=0)continue;double ox=gx*cell+r.nextInt(cell),oz=gz*cell+r.nextInt(cell),angle=r.nextDouble()*Math.PI,y=minY+r.nextInt(Math.max(1,maxY-minY+1));int length=80+r.nextInt(65);for(int i=0;i<length;i+=2){double wx=ox+Math.cos(angle)*(i-length/2.0),wz=oz+Math.sin(angle)*(i-length/2.0),width=1.7+Math.sin(i*Math.PI/length)*2.8;carve(context,wx,y+Math.sin(i*0.08)*3.0,wz,width,5.0+width*1.6);}}}
    private void carve(SUChunkContext c,double wx,double wy,double wz,double rx,double ry){int lx0=(int)Math.floor(wx-rx)-c.chunkX*16,lx1=(int)Math.ceil(wx+rx)-c.chunkX*16,lz0=(int)Math.floor(wz-rx)-c.chunkZ*16,lz1=(int)Math.ceil(wz+rx)-c.chunkZ*16;for(int x=lx0;x<=lx1;x++)for(int z=lz0;z<=lz1;z++)for(int y=(int)Math.floor(wy-ry);y<=(int)Math.ceil(wy+ry);y++){double dx=(c.worldX(x)+.5-wx)/rx,dy=(y+.5-wy)/ry,dz=(c.worldZ(z)+.5-wz)/rx;if(dx*dx+dy*dy+dz*dz<1&&replace.contains(c.getBlock(x,y,z)))c.setBlock(x,y,z,null,0);}}
    private int floorDiv(int a,int b){int q=a/b;return(a^b)<0&&q*b!=a?q-1:q;}
}
