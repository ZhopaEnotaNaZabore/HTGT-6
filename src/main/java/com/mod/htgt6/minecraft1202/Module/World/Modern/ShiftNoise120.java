package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Shared ShiftA/ShiftB nodes; each is flat-cached by the router. */
public final class ShiftNoise120 implements DensityFunction120 {
    private final Noise120 noise;private final boolean shiftB;
    public ShiftNoise120(Noise120 noise,boolean shiftB){if(noise==null)throw new NullPointerException("noise");this.noise=noise;this.shiftB=shiftB;}
    @Override public double sample(DensityContext120 context){
        double x=context.blockX(),z=context.blockZ();
        return (shiftB?noise.sample(z*0.25D,x*0.25D,0.0D):noise.sample(x*0.25D,0.0D,z*0.25D))*4.0D;
    }
}
