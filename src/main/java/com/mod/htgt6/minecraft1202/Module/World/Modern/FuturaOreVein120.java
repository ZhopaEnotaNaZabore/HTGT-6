package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/** NoiseRouter veinToggle/veinRidged/veinGap and OreVeinifier, with modern Y sampling. */
public final class FuturaOreVein120 {
    private final Noise120 toggle,a,b,gap;
    private final PositionalRandomFactory120 random;
    public FuturaOreVein120(long seed){
        NoiseFactory120 factory=new VanillaNormalNoiseFactory120(seed);
        toggle=factory.create(NoiseParameters120.ORE_VEININESS);
        a=factory.create(NoiseParameters120.ORE_VEIN_A);b=factory.create(NoiseParameters120.ORE_VEIN_B);
        gap=factory.create(NoiseParameters120.ORE_GAP);
        random=new PositionalRandomFactory120(seed).fork("minecraft:ore");
    }
    private NoiseChunk120 grid(final Noise120 noise,final double scale,int x,int z,int height){
        return new NoiseChunk120(new DensityFunction120(){
            public double sample(DensityContext120 c){
                int y=c.blockY()-64;
                return y>=-60&&y<=50?noise.sample(c.blockX()*scale,y*scale,c.blockZ()*scale):0;
            }
        },x<<4,0,z<<4,16,height,16);
    }
    public Chunk chunk(int x,int z,int height){return new Chunk(x,z,grid(toggle,1.5,x,z,height),
            grid(a,4,x,z,height),grid(b,4,x,z,height));}
    public final class Chunk{
        private final int ox,oz;private final NoiseChunk120 toggle,a,b;
        private Chunk(int x,int z,NoiseChunk120 toggle,NoiseChunk120 a,NoiseChunk120 b){
            ox=x<<4;oz=z<<4;this.toggle=toggle;this.a=a;this.b=b;
        }
        public State sample(int x,int y,int z){
            int modernY=y-64;double t=toggle.sampleLocal(x,y,z),strength=Math.abs(t);
            boolean copper=t>0;int min=copper?0:-60,max=copper?50:-8;
            if(modernY<min||modernY>max)return null;
            double edge=clampedMap(Math.min(max-modernY,modernY-min),0,20,-.2,0);
            if(strength+edge<(double).4F)return null;
            Xoroshiro128PlusPlus120 r=random.at(ox+x,modernY,oz+z);
            if(r.nextFloat()>.7F)return null;
            if(Math.max(Math.abs(a.sampleLocal(x,y,z)),Math.abs(b.sampleLocal(x,y,z)))-.07999999821186066>=0)return null;
            double richness=clampedMap(strength,(double).4F,(double).6F,(double).1F,(double).3F);
            if(r.nextFloat()<richness&&gap.sample(ox+x,modernY,oz+z)>(double)-.3F)
                return r.nextFloat()<.02F?(copper?RAW_COPPER:RAW_IRON):(copper?COPPER:IRON);
            return copper?GRANITE:TUFF;
        }
    }
    private static double clampedMap(double v,double a,double b,double c,double d){
        return c+Math.max(0,Math.min(1,(v-a)/(b-a)))*(d-c);
    }
    public static final class State{
        public final Block block;public final byte metadata;
        private State(Block block,int metadata){this.block=block;this.metadata=(byte)metadata;}
    }
    private static final State COPPER=new State(Backport120Content.copperOre,0),IRON=new State(Backport120Content.deepslateIronOre,0),
            RAW_COPPER=new State(Backport120Content.rawCopperBlock,0),RAW_IRON=new State(Backport120Content.rawIronBlock,0),
            GRANITE=new State(Blocks.stone,1),TUFF=new State(Backport120Content.tuff,0);
}
