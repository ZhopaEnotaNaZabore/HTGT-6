package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Exact ShiftA/ShiftB + ShiftedNoise2D arrangement used by NoiseRouterData. */
public final class ShiftedNoise120 implements DensityFunction120 {
    private final DensityFunction120 shiftX,shiftZ;
    private final Noise120 targetNoise;
    public ShiftedNoise120(DensityFunction120 shiftX,DensityFunction120 shiftZ,Noise120 targetNoise){
        if(shiftX==null||shiftZ==null||targetNoise==null)throw new NullPointerException("noise");
        this.shiftX=shiftX;this.shiftZ=shiftZ;this.targetNoise=targetNoise;
    }
    @Override public double sample(DensityContext120 context){
        double x=context.blockX(),z=context.blockZ();
        return targetNoise.sample(x*0.25D+shiftX.sample(context),0.0D,z*0.25D+shiftZ.sample(context));
    }
}
