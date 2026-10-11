package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** DensityFunction adapter for the float-valued vanilla terrain splines. */
public final class SplineDensityFunction120 implements DensityFunction120 {
    private final VanillaCubicSpline120 spline;
    public SplineDensityFunction120(VanillaCubicSpline120 spline){if(spline==null)throw new NullPointerException("spline");this.spline=spline;}
    @Override public double sample(DensityContext120 context){return spline.sample(context);}
}
