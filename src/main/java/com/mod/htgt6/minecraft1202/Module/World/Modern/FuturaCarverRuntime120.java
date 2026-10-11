package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import java.util.BitSet;
import java.util.Random;

/** CaveWorldCarver / CanyonWorldCarver 1.20.2 with a +64 storage translation. */
public final class FuturaCarverRuntime120 {
    public static final int RANGE=8;
    public static final double CAVE_PROBABILITY=.15D,EXTRA_CAVE_PROBABILITY=.07D,CANYON_PROBABILITY=.01D;
    private final long seed;
    private final AquiferSampler120 aquifer;
    public FuturaCarverRuntime120(long seed,AquiferSampler120 aquifer){this.seed=seed;this.aquifer=aquifer;}

    public void carve(Block[] blocks,byte[] metadata,int height,int chunkX,int chunkZ){
        if(blocks==null||metadata==null||blocks.length!=256*height||metadata.length!=blocks.length)
            throw new IllegalArgumentException("Invalid Futura carver arrays");
        Target target=new Target(blocks,metadata,height,chunkX,chunkZ);
        for(int sx=chunkX-RANGE;sx<=chunkX+RANGE;sx++)for(int sz=chunkZ-RANGE;sz<=chunkZ+RANGE;sz++){
            for(int i=0;i<3;i++){
                Random random=WorldgenRandom120.carver(seed,i,sx,sz);
                float chance=(float)(i==0?CAVE_PROBABILITY:i==1?EXTRA_CAVE_PROBABILITY:CANYON_PROBABILITY);
                if(random.nextFloat()>chance)continue;
                if(i==2)canyon(target,sx,sz,random);else caves(target,sx,sz,random,i==1);
            }
        }
    }
    private void caves(Target t,int sx,int sz,Random r,boolean extra){
        int count=r.nextInt(r.nextInt(r.nextInt(15)+1)+1);
        for(int i=0;i<count;i++){
            double x=(sx<<4)+r.nextInt(16),y=8+r.nextInt((extra?47:180)+64-8+1),z=(sz<<4)+r.nextInt(16);
            double horizontal=.7F+r.nextFloat()*(1.4F-.7F),vertical=.8F+r.nextFloat()*(1.3F-.8F);
            double floor=-1.0F+r.nextFloat()*(-.4F-(-1.0F));
            int branches=1;
            if(r.nextInt(4)==0){
                double yScale=.1F+r.nextFloat()*(.9F-.1F);
                float width=1.0F+r.nextFloat()*6.0F;
                double radius=1.5D+MathHelper.sin(1.5707964F)*width;
                carveEllipsoid(t,x+1,y,z,radius,radius*yScale,floor,null);
                branches+=r.nextInt(4);
            }
            for(int branch=0;branch<branches;branch++){
                float yaw=r.nextFloat()*((float)Math.PI*2),pitch=(r.nextFloat()-.5F)/4;
                float width=r.nextFloat()*2+r.nextFloat();
                if(r.nextInt(10)==0)width*=r.nextFloat()*r.nextFloat()*3+1;
                int length=112-r.nextInt(28);
                tunnel(t,r.nextLong(),x,y,z,horizontal,vertical,width,yaw,pitch,0,length,1,floor);
            }
        }
    }
    private void tunnel(Target t,long seed,double x,double y,double z,double horizontal,double vertical,
                        float width,float yaw,float pitch,int start,int length,double yScale,double floor){
        Random r=new Random(seed);int fork=r.nextInt(length/2)+length/4;
        boolean gentle=r.nextInt(6)==0;float yawVelocity=0,pitchVelocity=0;
        for(int step=start;step<length;step++){
            double radius=1.5D+MathHelper.sin((float)Math.PI*step/length)*width,yr=radius*yScale;
            float cosPitch=MathHelper.cos(pitch);
            x+=MathHelper.cos(yaw)*cosPitch;y+=MathHelper.sin(pitch);z+=MathHelper.sin(yaw)*cosPitch;
            pitch*=gentle?.92F:.7F;pitch+=pitchVelocity*.1F;yaw+=yawVelocity*.1F;
            pitchVelocity*=.9F;yawVelocity*=.75F;
            pitchVelocity+=(r.nextFloat()-r.nextFloat())*r.nextFloat()*2;
            yawVelocity+=(r.nextFloat()-r.nextFloat())*r.nextFloat()*4;
            if(step==fork&&width>1){
                tunnel(t,r.nextLong(),x,y,z,horizontal,vertical,r.nextFloat()*.5F+.5F,yaw-1.5707964F,pitch/3,step,length,1,floor);
                tunnel(t,r.nextLong(),x,y,z,horizontal,vertical,r.nextFloat()*.5F+.5F,yaw+1.5707964F,pitch/3,step,length,1,floor);
                return;
            }
            if(r.nextInt(4)==0)continue;
            if(!canReach(t,x,z,step,length,width))return;
            carveEllipsoid(t,x,y,z,radius*horizontal,yr*vertical,floor,null);
        }
    }
    private void canyon(Target t,int sx,int sz,Random random){
        double x=(sx<<4)+random.nextInt(16),y=74+random.nextInt(58),z=(sz<<4)+random.nextInt(16);
        float yaw=random.nextFloat()*((float)Math.PI*2),pitch=-.125F+random.nextFloat()*.25F;
        // TrapezoidFloat(0,6,plateau=2): lower spread 2, upper spread 4.
        float width=random.nextFloat()*4+random.nextFloat()*2;
        int length=(int)(112*(.75F+random.nextFloat()*.25F));
        Random r=new Random(random.nextLong());float[] widths=new float[t.height];float current=1;
        for(int i=0;i<widths.length;i++){
            if(i==0||r.nextInt(3)==0)current=1+r.nextFloat()*r.nextFloat();
            widths[i]=current*current;
        }
        float yawVelocity=0,pitchVelocity=0;
        for(int step=0;step<length;step++){
            double radius=1.5D+MathHelper.sin(step*(float)Math.PI/length)*width;
            double yr=radius*3.0D;
            radius*=.75F+r.nextFloat()*.25F;
            yr*=.75F+r.nextFloat()*.25F;
            float cosPitch=MathHelper.cos(pitch);
            x+=MathHelper.cos(yaw)*cosPitch;y+=MathHelper.sin(pitch);z+=MathHelper.sin(yaw)*cosPitch;
            pitch*=.7F;pitch+=pitchVelocity*.05F;yaw+=yawVelocity*.05F;
            pitchVelocity*=.8F;yawVelocity*=.5F;
            pitchVelocity+=(r.nextFloat()-r.nextFloat())*r.nextFloat()*2;
            yawVelocity+=(r.nextFloat()-r.nextFloat())*r.nextFloat()*4;
            if(r.nextInt(4)==0)continue;
            if(!canReach(t,x,z,step,length,width))return;
            carveEllipsoid(t,x,y,z,radius,yr,-1,widths);
        }
    }
    private static boolean canReach(Target t,double x,double z,int step,int length,float width){
        double dx=x-(t.ox+8),dz=z-(t.oz+8),remaining=length-step,max=width+2.0F+16.0F;
        return dx*dx+dz*dz-remaining*remaining<=max*max;
    }
    private void carveEllipsoid(Target t,double cx,double cy,double cz,double rx,double ry,double floor,float[] widths){
        if(Math.abs(cx-(t.ox+8))>16+rx*2||Math.abs(cz-(t.oz+8))>16+rx*2)return;
        int minX=Math.max(MathHelper.floor_double(cx-rx)-t.ox-1,0),maxX=Math.min(MathHelper.floor_double(cx+rx)-t.ox,15);
        int minZ=Math.max(MathHelper.floor_double(cz-rx)-t.oz-1,0),maxZ=Math.min(MathHelper.floor_double(cz+rx)-t.oz,15);
        int minY=Math.max(MathHelper.floor_double(cy-ry)-1,1),maxY=Math.min(MathHelper.floor_double(cy+ry)+1,t.height-8);
        for(int lx=minX;lx<=maxX;lx++)for(int lz=minZ;lz<=maxZ;lz++){
            int x=t.ox+lx,z=t.oz+lz;double nx=(x+.5-cx)/rx,nz=(z+.5-cz)/rx,h=nx*nx+nz*nz;
            if(h>=1)continue;boolean surface=false;
            for(int y=maxY;y>minY;y--){
                double ny=(y-.5-cy)/ry;
                if(widths==null?(ny<=floor||h+ny*ny>=1):h*widths[y-1]+ny*ny/6>=1)continue;
                int index=(lx*16+lz)*t.height+y;
                if(t.mask.get(index))continue;t.mask.set(index);
                Block old=t.blocks[index];surface|=old==Blocks.grass||old==Blocks.mycelium;
                if(!replaceable(old))continue;
                Block replacement;
                if(y<=8)replacement=Blocks.lava;
                else{
                    AquiferSampler120.Result fluid=aquifer.sampleDensity(x,y-64,z,0.0D);
                    if(fluid.decision==AquiferSampler120.Decision.SOLID)continue;
                    replacement=fluid.decision==AquiferSampler120.Decision.LAVA?Blocks.lava
                            :fluid.decision==AquiferSampler120.Decision.WATER?Backport120Content.aquiferWater:null;
                }
                t.blocks[index]=replacement;t.meta[index]=0;
                // Legacy biome storage has no topMaterial callback. Restore the removed turf kind.
                if(surface&&t.blocks[index-1]==Blocks.dirt){t.blocks[index-1]=old==Blocks.mycelium?Blocks.mycelium:Blocks.grass;t.meta[index-1]=0;}
            }
        }
    }
    static boolean replaceable(Block b){
        if(b==null||b==Blocks.bedrock||b==Blocks.water||b==Blocks.flowing_water||b==Blocks.lava
                ||b==Blocks.flowing_lava||b==Backport120Content.aquiferWater)return false;
        Material m=b.getMaterial();
        return m==Material.rock||m==Material.ground||m==Material.grass||m==Material.clay||m==Material.sand;
    }
    private static final class Target{
        final Block[] blocks;final byte[] meta;final int height,ox,oz;final BitSet mask;
        Target(Block[] blocks,byte[] meta,int height,int x,int z){this.blocks=blocks;this.meta=meta;this.height=height;
            ox=x<<4;oz=z<<4;mask=new BitSet(blocks.length);}
    }
}
