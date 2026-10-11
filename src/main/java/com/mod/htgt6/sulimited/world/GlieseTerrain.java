package com.mod.htgt6.sulimited.world;
/** Stateless world-coordinate fields, including signed coordinates. */
public final class GlieseTerrain {
    private GlieseTerrain() {}
    public static long hash(long seed,int x,int z) {
        long h=seed+x*341873128712L+z*132897987541L;
        h=(h^(h>>>33))*0xff51afd7ed558ccdL; h=(h^(h>>>33))*0xc4ceb9fe1a85ec53L;
        return h^(h>>>33);
    }
    private static double value(long s,int x,int z) { return (hash(s,x,z)>>>11)*0x1.0p-52-1; }
    public static double fade(double v) { return v*v*(3-2*v); }
    public static double smooth(double a,double b,double v) { return fade(Math.max(0,Math.min(1,(v-a)/(b-a)))); }
    public static double lerp(double a,double b,double t) { return a+(b-a)*t; }
    public static double noise(long seed,double x,double z,double scale) {
        x/=scale; z/=scale; int ix=(int)Math.floor(x), iz=(int)Math.floor(z);
        double tx=fade(x-ix),tz=fade(z-iz);
        return lerp(lerp(value(seed,ix,iz),value(seed,ix+1,iz),tx),lerp(value(seed,ix,iz+1),value(seed,ix+1,iz+1),tx),tz);
    }
    /** Neighbour cells participate so features are continuous across chunk/cell edges. */
    public static double radial(long seed,int x,int z,int cellSize,int mask,double radius) {
        int cx=(int)Math.floor(x/(double)cellSize),cz=(int)Math.floor(z/(double)cellSize); double best=0;
        for(int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) {
            long h=hash(seed,cx+dx,cz+dz); if((h&mask)!=0) continue;
            double px=(cx+dx)*(double)cellSize+cellSize*(0.2+((h>>>8)&255)/425.0);
            double pz=(cz+dz)*(double)cellSize+cellSize*(0.2+((h>>>16)&255)/425.0);
            double r=radius*(0.8+((h>>>24)&255)/637.5);
            best=Math.max(best,1-Math.hypot(x-px,z-pz)/r);
        } return Math.max(0,best);
    }
    public static final class VolcanicSample {
        public final int height,lavaLevel;
        public final boolean volcano,wasteland,dryChannel,fault;
        private VolcanicSample(double h,int lava,boolean v,boolean ash,boolean dry,boolean f) {
            height=Math.max(12,Math.min(230,(int)Math.round(h))); lavaLevel=lava;
            volcano=v; wasteland=ash; dryChannel=dry; fault=f;
        }
    }
    public static VolcanicSample volcanic(long seed,int x,int z) {
        double continent=noise(seed+11,x,z,560), hills=noise(seed+19,x,z,100);
        double ash=smooth(-0.12,0.38,noise(seed+31,x,z,380));
        double h=81+continent*17+hills*(9-6*ash);
        // Broad meanders and narrower faults excavate independently of rock selection.
        double river=1-smooth(0.018,0.13,Math.abs(noise(seed+83,x,z,240)));
        double fracture=1-smooth(0.012,0.065,Math.abs(noise(seed+91,x,z,530)));
        double dry=1-smooth(0.016,0.09,Math.abs(noise(seed+109,x,z,190)));
        h=lerp(h,59,river); h=lerp(h,51,fracture); h-=dry*7;
        double lake=smooth(-0.47,-0.66,noise(seed+127,x,z,270)); h=lerp(h,54,lake);
        // Shield cone with a raised rim and a flat, elevated lava caldera.
        double v=radial(seed+131,x,z,512,3,165);
        double cone=smooth(0,0.78,v)*99, caldera=smooth(0.78,0.93,v);
        h=lerp(h,82+cone-caldera*35,smooth(0,0.22,v));
        int lava=v>0.78 && h<150?150:v<0.12 && h<62 && (river>0.85||fracture>0.82||lake>0.8)?62:-1;
        return new VolcanicSample(h,lava,v>0.18,ash>0.5,dry>0.5,fracture>0.5);
    }
}
