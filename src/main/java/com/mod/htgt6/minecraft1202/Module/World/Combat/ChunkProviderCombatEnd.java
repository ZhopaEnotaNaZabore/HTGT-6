package com.mod.htgt6.minecraft1202.Module.World.Combat;

import com.mod.htgt6.minecraft1202.Module.Data.Combat.EndFightData;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.EndMechanics;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.init.Blocks;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderEnd;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraft.entity.boss.EntityDragon;

/** Minecraft 1.12.2 End generator adapted to 1.7.10 block arrays. */
public final class ChunkProviderCombatEnd extends ChunkProviderEnd {
    private final World world;
    private final Random random;
    private final NoiseGeneratorOctaves lowNoise, highNoise, selectorNoise, depthNoise, scaleNoise;
    private final NoiseGeneratorSimplex islandNoise;
    private double[] density, selector, low, high;

    public ChunkProviderCombatEnd(World world, long seed) {
        super(world, seed); this.world = world; random = new Random(seed);
        lowNoise = new NoiseGeneratorOctaves(random, 16);
        highNoise = new NoiseGeneratorOctaves(random, 16);
        selectorNoise = new NoiseGeneratorOctaves(random, 8);
        depthNoise = new NoiseGeneratorOctaves(random, 10);
        scaleNoise = new NoiseGeneratorOctaves(random, 16);
        islandNoise = new NoiseGeneratorSimplex(random);
    }

    @Override public Chunk loadChunk(int x, int z) { return provideChunk(x, z); }
    @Override public Chunk provideChunk(int cx, int cz) {
        random.setSeed((long)cx * 341873128712L + (long)cz * 132897987541L);
        Block[] blocks = new Block[16 * 16 * 256]; byte[] meta = new byte[blocks.length];
        setBlocksInChunk(cx, cz, blocks);
        Chunk chunk = new Chunk(world, blocks, meta, cx, cz);
        byte[] biomes = chunk.getBiomeArray();
        for (int i = 0; i < biomes.length; i++) biomes[i] = (byte)BiomeGenBase.sky.biomeID;
        chunk.generateSkylightMap(); return chunk;
    }

    public void setBlocksInChunk(int cx, int cz, Block[] blocks) {
        density = getHeights(density, cx * 2, 0, cz * 2, 3, 33, 3);
        for (int qx=0;qx<2;qx++) for(int qz=0;qz<2;qz++) for(int qy=0;qy<32;qy++) {
            double a=density[(qx*3+qz)*33+qy], b=density[(qx*3+qz+1)*33+qy];
            double c=density[((qx+1)*3+qz)*33+qy], d=density[((qx+1)*3+qz+1)*33+qy];
            double da=(density[(qx*3+qz)*33+qy+1]-a)*.25D;
            double db=(density[(qx*3+qz+1)*33+qy+1]-b)*.25D;
            double dc=(density[((qx+1)*3+qz)*33+qy+1]-c)*.25D;
            double dd=(density[((qx+1)*3+qz+1)*33+qy+1]-d)*.25D;
            for(int sy=0;sy<4;sy++) {
                double xa=a, xb=b, dxa=(c-a)*.125D, dxb=(d-b)*.125D;
                for(int sx=0;sx<8;sx++) {
                    double value=xa, dz=(xb-xa)*.125D;
                    for(int sz=0;sz<8;sz++) {
                        if(value>0) blocks[((sx+qx*8)*16+(sz+qz*8))*256+(sy+qy*4)]=Blocks.end_stone;
                        value+=dz;
                    }
                    xa+=dxa; xb+=dxb;
                }
                a+=da;b+=db;c+=dc;d+=dd;
            }
        }
    }

    public float getIslandHeightValue(int cx,int cz,int lx,int lz) {
        float x=cx*2+lx,z=cz*2+lz;
        float result=clamp(100F-MathHelper.sqrt_float(x*x+z*z)*8F);
        for(int dx=-12;dx<=12;dx++) for(int dz=-12;dz<=12;dz++) {
            long ix=cx+dx,iz=cz+dz;
            if(ix*ix+iz*iz>4096L && islandNoise.func_151605_a(ix,iz)<-.8999999761581421D) {
                float factor=(MathHelper.abs((float)ix)*3439F+MathHelper.abs((float)iz)*147F)%13F+9F;
                float rx=lx-dx*2,rz=lz-dz*2;
                result=Math.max(result,clamp(100F-MathHelper.sqrt_float(rx*rx+rz*rz)*factor));
            }
        }
        return result;
    }
    private static float clamp(float v){return Math.max(-100F,Math.min(80F,v));}
    public boolean isIslandChunk(int x,int z){return (long)x*x+(long)z*z>4096L&&getIslandHeightValue(x,z,1,1)>=0;}

    public int getStructureGroundY(int cx,int cz,int rotation){
        Block[] b=new Block[16*16*256];setBlocksInChunk(cx,cz,b);
        int ox=rotation==1||rotation==2?-5:5,oz=rotation==2||rotation==3?-5:5;
        return Math.min(Math.min(top(b,7,7),top(b,7,7+oz)),Math.min(top(b,7+ox,7),top(b,7+ox,7+oz)));
    }
    private static int top(Block[] b,int x,int z){if(x<0||z<0||x>=16||z>=16)return 0;for(int y=255;y>=0;y--)if(b[(x*16+z)*256+y]!=null)return y+1;return 0;}

    private double[] getHeights(double[] out,int x,int y,int z,int sx,int sy,int sz){
        if(out==null)out=new double[sx*sy*sz];double f=1368.824D;
        selector=selectorNoise.generateNoiseOctaves(selector,x,y,z,sx,sy,sz,f/80D,4.277575000000001D,f/80D);
        low=lowNoise.generateNoiseOctaves(low,x,y,z,sx,sy,sz,f,684.412D,f);
        high=highNoise.generateNoiseOctaves(high,x,y,z,sx,sy,sz,f,684.412D,f);
        int baseX=x/2,baseZ=z/2,index=0;
        for(int ix=0;ix<sx;ix++)for(int iz=0;iz<sz;iz++){
            float island=getIslandHeightValue(baseX,baseZ,ix,iz);
            for(int iy=0;iy<sy;iy++){
                double a=low[index]/512D,b=high[index]/512D,m=(selector[index]/10D+1D)/2D;
                double v=(m<0?a:m>1?b:a+(b-a)*m)-8D+island;
                if(iy>sy/2-2){double t=MathHelper.clamp_double((iy-(sy/2-2))/64D,0,1);v=v*(1-t)-3000D*t;}
                if(iy<8){double t=(8-iy)/7D;v=v*(1-t)-30D*t;}out[index++]=v;
            }
        }return out;
    }

    @Override public void populate(IChunkProvider provider,int cx,int cz){
        boolean oldInstant=BlockFalling.fallInstantly;BlockFalling.fallInstantly=true;
        try {random.setSeed(world.getSeed());
        long ax=random.nextLong()/2*2+1,az=random.nextLong()/2*2+1;
        random.setSeed((long)cx*ax+(long)cz*az^world.getSeed());
        EndCityGenerator.populateChunk(world,this,cx,cz);
        EndSpikeGenerator.populate(world,cx,cz);
        if(cx==0&&cz==0&&!world.isRemote&&EndFightData.get(world).claimInitialDragon()){
            EntityDragon dragon=new EntityDragon(world);dragon.setLocationAndAngles(0,128,0,random.nextFloat()*360F,0);world.spawnEntityInWorld(dragon);
        }
        if((long)cx*cx+(long)cz*cz>4096L){float h=getIslandHeightValue(cx,cz,1,1);int bx=cx*16,bz=cz*16;
            if(h< -20F&&random.nextInt(14)==0){smallIsland(bx+random.nextInt(16)+8,55+random.nextInt(16),bz+random.nextInt(16)+8);if(random.nextInt(4)==0)smallIsland(bx+random.nextInt(16)+8,55+random.nextInt(16),bz+random.nextInt(16)+8);}
            if(h>40F){for(int i=0,n=random.nextInt(5);i<n;i++){int px=bx+random.nextInt(16)+8,pz=bz+random.nextInt(16)+8,py=world.getTopSolidOrLiquidBlock(px,pz);if(py>0&&world.isAirBlock(px,py,pz)&&world.getBlock(px,py-1,pz)==Blocks.end_stone)chorus(px,py,pz,px,pz,8,0);}
                if(random.nextInt(700)==0){int gx=bx+random.nextInt(16)+8,gz=bz+random.nextInt(16)+8,gy=world.getTopSolidOrLiquidBlock(gx,gz)+3+random.nextInt(7);EndMechanics.generateGateway(world,gx,gy,gz,0,-1,0,false);}}
        }
        }finally{BlockFalling.fallInstantly=oldInstant;}
    }
    private void smallIsland(int x,int y,int z){float r=random.nextInt(3)+4;for(int dy=0;r>.5F;dy--,r-=random.nextInt(2)+.5F){int n=MathHelper.ceiling_float_int(r);for(int dx=-n;dx<=n;dx++)for(int dz=-n;dz<=n;dz++)if(dx*dx+dz*dz<=(r+1)*(r+1))world.setBlock(x+dx,y+dy,z+dz,Blocks.end_stone,0,2);}}
    private void chorus(int x,int y,int z,int rx,int rz,int range,int depth){world.setBlock(x,y,z,CombatContent.chorusPlant,0,2);int n=random.nextInt(4)+1+(depth==0?1:0);for(int i=1;i<=n;i++){if(!clear(x,y+i,z,-1))return;world.setBlock(x,y+i,z,CombatContent.chorusPlant,0,2);}boolean branch=false;if(depth<4){int count=random.nextInt(4)+(depth==0?1:0);for(int i=0;i<count;i++){int side=2+random.nextInt(4),nx=x+Facing.offsetsXForSide[side],nz=z+Facing.offsetsZForSide[side];if(Math.abs(nx-rx)<range&&Math.abs(nz-rz)<range&&world.isAirBlock(nx,y+n,nz)&&world.isAirBlock(nx,y+n-1,nz)&&clear(nx,y+n,nz,Facing.oppositeSide[side])){branch=true;chorus(nx,y+n,nz,rx,rz,range,depth+1);}}}if(!branch)world.setBlock(x,y+n,z,CombatContent.chorusFlower,5,2);}
    /** 1.12 checks only horizontal neighbours here; the block below is the newly grown stem. */
    private boolean clear(int x,int y,int z,int except){for(int s=2;s<6;s++)if(s!=except&&!world.isAirBlock(x+Facing.offsetsXForSide[s],y,z+Facing.offsetsZForSide[s]))return false;return true;}
}
