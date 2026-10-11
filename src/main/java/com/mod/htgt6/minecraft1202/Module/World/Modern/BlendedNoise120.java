package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Exact standard Overworld BlendedNoise (dzm) without codec plumbing. */
public final class BlendedNoise120 {
    private final ImprovedNoise120[] minLimit=new ImprovedNoise120[16];
    private final ImprovedNoise120[] maxLimit=new ImprovedNoise120[16];
    private final ImprovedNoise120[] main=new ImprovedNoise120[8];
    private final double xzMultiplier,yMultiplier,xzFactor,yFactor,smearScale;

    public BlendedNoise120(Xoroshiro128PlusPlus120 random,double xzMultiplier,double yMultiplier,
                           double xzFactor,double yFactor,double smearScale){
        if(random==null)throw new NullPointerException("random");
        fillLegacyOctaves(minLimit,random);
        fillLegacyOctaves(maxLimit,random);
        fillLegacyOctaves(main,random);
        this.xzMultiplier=xzMultiplier;this.yMultiplier=yMultiplier;
        this.xzFactor=xzFactor;this.yFactor=yFactor;this.smearScale=smearScale;
    }

    public double sample(int blockX,int modernY,int blockZ){
        double x=blockX*(684.412D*xzMultiplier),y=modernY*(684.412D*yMultiplier),z=blockZ*(684.412D*xzMultiplier);
        double mainX=x/xzFactor,mainY=y/yFactor,mainZ=z/xzFactor;
        double smear=684.412D*yMultiplier*smearScale,mainSmear=smear/yFactor;
        double min=0.0D,max=0.0D,selector=0.0D,frequency=1.0D;
        for(int octave=0;octave<8;octave++){
            ImprovedNoise120 noise=octave(main,octave);
            selector+=noise.sample(wrap(mainX*frequency),wrap(mainY*frequency),wrap(mainZ*frequency),
                    mainSmear*frequency,mainY*frequency)/frequency;
            frequency/=2.0D;
        }
        double blend=(selector/10.0D+1.0D)/2.0D;
        boolean maxOnly=blend>=1.0D,minOnly=blend<=0.0D;
        frequency=1.0D;
        for(int octave=0;octave<16;octave++){
            double sx=wrap(x*frequency),sy=wrap(y*frequency),sz=wrap(z*frequency),verticalSmear=smear*frequency;
            if(!maxOnly)min+=octave(minLimit,octave).sample(sx,sy,sz,verticalSmear,y*frequency)/frequency;
            if(!minOnly)max+=octave(maxLimit,octave).sample(sx,sy,sz,verticalSmear,y*frequency)/frequency;
            frequency/=2.0D;
        }
        return clampedLerp(min/512.0D,max/512.0D,blend)/128.0D;
    }

    private static void fillLegacyOctaves(ImprovedNoise120[] levels,Xoroshiro128PlusPlus120 random){
        levels[levels.length-1]=new ImprovedNoise120(random);
        for(int index=levels.length-2;index>=0;index--)levels[index]=new ImprovedNoise120(random);
    }
    private static ImprovedNoise120 octave(ImprovedNoise120[] levels,int octave){return levels[levels.length-1-octave];}
    private static double wrap(double value){return value-Math.floor(value/3.3554432E7D+0.5D)*3.3554432E7D;}
    private static double clampedLerp(double a,double b,double t){return t<=0.0D?a:t>=1.0D?b:a+t*(b-a);}
}
