package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Vanilla overworld slide transforms from NoiseRouterData 1.20.2. */
public final class OverworldDensityTransforms120 {
    private OverworldDensityTransforms120(){ }

    static DensityFunction120 initialDensityWithoutJaggedness(final DensityFunction120 shaped){
        if(shaped==null)throw new NullPointerException("shaped");
        return new DensityFunction120(){
            @Override public double sample(DensityContext120 context){
                double value=clamp(shaped.sample(context)-0.703125D,-64.0D,64.0D);
                int modernY=FuturaMountainDensityRouter120.futuraToModernY(context.blockY());
                // Standard Overworld: minY=-64, height=384, top slide 80/64,
                // bottom slide 0/24. Order matches NoiseRouterData.slideOverworld.
                double topAlpha=clampedMap(modernY,240.0D,256.0D,1.0D,0.0D);
                value=lerp(topAlpha,-0.078125D,value);
                double bottomAlpha=clampedMap(modernY,-64.0D,-40.0D,0.0D,1.0D);
                return lerp(bottomAlpha,0.1171875D,value);
            }
        };
    }

    private static double clampedMap(double value,double from,double to,double fromValue,double toValue){
        if(value<=from)return fromValue;if(value>=to)return toValue;
        return lerp((value-from)/(to-from),fromValue,toValue);
    }
    private static double clamp(double value,double min,double max){return Math.max(min,Math.min(max,value));}
    private static double lerp(double t,double a,double b){return a+t*(b-a);}
}
