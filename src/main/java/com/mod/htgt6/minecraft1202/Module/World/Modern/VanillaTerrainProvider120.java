package com.mod.htgt6.minecraft1202.Module.World.Modern;

import static com.mod.htgt6.minecraft1202.Module.World.Modern.VanillaCubicSpline120.*;

/** Standard (non-amplified) Overworld spline graph ported from 1.20.2 TerrainProvider (og). */
public final class VanillaTerrainProvider120 {
    private VanillaTerrainProvider120(){ }

    static VanillaCubicSpline120 overworldOffset(Coordinate continents,Coordinate erosion,Coordinate folded){
        VanillaCubicSpline120 a=erosionOffset(erosion,folded,-0.15F,0.0F,0.0F,0.1F,0.0F,-0.03F,false,false);
        VanillaCubicSpline120 b=erosionOffset(erosion,folded,-0.1F,0.03F,0.1F,0.1F,0.01F,-0.03F,false,false);
        VanillaCubicSpline120 c=erosionOffset(erosion,folded,-0.1F,0.03F,0.1F,0.7F,0.01F,-0.03F,true,true);
        VanillaCubicSpline120 d=erosionOffset(erosion,folded,-0.05F,0.03F,0.1F,1.0F,0.01F,0.01F,true,true);
        return builder(continents).add(-1.1F,0.044F).add(-1.02F,-0.2222F).add(-0.51F,-0.2222F)
                .add(-0.44F,-0.12F).add(-0.18F,-0.12F).add(-0.16F,a).add(-0.15F,a)
                .add(-0.1F,b).add(0.25F,c).add(1.0F,d).build();
    }

    static VanillaCubicSpline120 overworldFactor(Coordinate continents,Coordinate erosion,
                                                  Coordinate ridges,Coordinate folded){
        return builder(continents).add(-0.19F,3.95F)
                .add(-0.15F,erosionFactor(erosion,ridges,folded,6.25F,true))
                .add(-0.1F,erosionFactor(erosion,ridges,folded,5.47F,true))
                .add(0.03F,erosionFactor(erosion,ridges,folded,5.08F,true))
                .add(0.06F,erosionFactor(erosion,ridges,folded,4.69F,false)).build();
    }

    static VanillaCubicSpline120 overworldJaggedness(Coordinate continents,Coordinate erosion,
                                                      Coordinate ridges,Coordinate folded){
        return builder(continents).add(-0.11F,0.0F)
                .add(0.03F,erosionJaggedness(erosion,ridges,folded,1.0F,0.5F,0.0F,0.0F))
                .add(0.65F,erosionJaggedness(erosion,ridges,folded,1.0F,1.0F,1.0F,0.0F)).build();
    }

    private static VanillaCubicSpline120 erosionJaggedness(Coordinate erosion,Coordinate ridges,Coordinate folded,
                                                            float a,float b,float c,float d){
        VanillaCubicSpline120 low=ridgeJaggedness(ridges,folded,a,c);
        VanillaCubicSpline120 high=ridgeJaggedness(ridges,folded,b,d);
        return builder(erosion).add(-1.0F,low).add(-0.78F,high).add(-0.5775F,high).add(-0.375F,0.0F).build();
    }

    private static VanillaCubicSpline120 ridgeJaggedness(Coordinate ridges,Coordinate folded,float low,float high){
        float folded04=foldRidges(0.4F),folded056=foldRidges(0.56666666F),middle=(folded04+folded056)/2.0F;
        Builder result=builder(folded).add(folded04,0.0F);
        result.add(middle,high>0.0F?weirdnessJaggedness(ridges,high):constant(0.0F));
        result.add(1.0F,low>0.0F?weirdnessJaggedness(ridges,low):constant(0.0F));
        return result.build();
    }

    private static VanillaCubicSpline120 weirdnessJaggedness(Coordinate ridges,float value){
        return builder(ridges).add(-0.01F,0.63F*value).add(0.01F,0.3F*value).build();
    }

    private static VanillaCubicSpline120 erosionFactor(Coordinate erosion,Coordinate ridges,Coordinate folded,
                                                        float factor,boolean useFolded){
        VanillaCubicSpline120 ridge=builder(ridges).add(-0.2F,6.3F).add(0.2F,factor).build();
        Builder result=builder(erosion).add(-0.6F,ridge)
                .add(-0.5F,builder(ridges).add(-0.05F,6.3F).add(0.05F,2.67F).build())
                .add(-0.35F,ridge).add(-0.25F,ridge)
                .add(-0.1F,builder(ridges).add(-0.05F,2.67F).add(0.05F,6.3F).build()).add(0.03F,ridge);
        if(useFolded){
            VanillaCubicSpline120 transition=builder(ridges).add(0.0F,factor).add(0.1F,0.625F).build();
            VanillaCubicSpline120 foldedTransition=builder(folded).add(-0.9F,factor).add(-0.69F,transition).build();
            result.add(0.35F,factor).add(0.45F,foldedTransition).add(0.55F,foldedTransition).add(0.62F,factor);
        }else{
            VanillaCubicSpline120 low=builder(folded).add(-0.7F,ridge).add(-0.15F,1.37F).build();
            VanillaCubicSpline120 high=builder(folded).add(0.45F,ridge).add(0.7F,1.56F).build();
            result.add(0.05F,high).add(0.4F,high).add(0.45F,low).add(0.55F,low).add(0.58F,factor);
        }
        return result.build();
    }

    private static VanillaCubicSpline120 mountainRidge(Coordinate folded,float value,boolean forcePositive){
        Builder result=builder(folded);
        float low=-0.7F;
        float atMinusOne=mountainContinentalness(-1.0F,value,low);
        float atOne=mountainContinentalness(1.0F,value,low);
        float zeroCrossing=mountainZeroCrossing(value);
        if(-0.65F<zeroCrossing&&zeroCrossing<1.0F){
            float atMinus065=mountainContinentalness(-0.65F,value,low);
            float atMinus075=mountainContinentalness(-0.75F,value,low);
            float slope0=slope(atMinusOne,atMinus075,-1.0F,-0.75F);
            result.add(-1.0F,atMinusOne,slope0).add(-0.75F,atMinus075).add(-0.65F,atMinus065);
            float atCrossing=mountainContinentalness(zeroCrossing,value,low);
            float slope1=slope(atCrossing,atOne,zeroCrossing,1.0F);
            result.add(zeroCrossing-0.01F,atCrossing).add(zeroCrossing,atCrossing,slope1).add(1.0F,atOne,slope1);
        }else{
            float endSlope=slope(atMinusOne,atOne,-1.0F,1.0F);
            if(forcePositive)result.add(-1.0F,Math.max(0.2F,atMinusOne)).add(0.0F,lerp(0.5F,atMinusOne,atOne),endSlope);
            else result.add(-1.0F,atMinusOne,endSlope);
            result.add(1.0F,atOne,endSlope);
        }
        return result.build();
    }

    private static float mountainContinentalness(float ridge,float value,float threshold){
        float scale=1.0F-(1.0F-value)*0.5F,bias=0.5F*(1.0F-value);
        float result=(ridge+1.17F)*0.46082947F*scale-bias;
        return ridge<threshold?Math.max(result,-0.2222F):Math.max(result,0.0F);
    }
    private static float mountainZeroCrossing(float value){
        float scale=1.0F-(1.0F-value)*0.5F,bias=0.5F*(1.0F-value);
        return bias/(0.46082947F*scale)-1.17F;
    }

    private static VanillaCubicSpline120 erosionOffset(Coordinate erosion,Coordinate folded,
                                                        float p2,float p3,float p4,float p5,float p6,float p7,
                                                        boolean addHigh,boolean forcePositive){
        VanillaCubicSpline120 mountains15=mountainRidge(folded,lerp(p5,0.6F,1.5F),forcePositive);
        VanillaCubicSpline120 mountains10=mountainRidge(folded,lerp(p5,0.6F,1.0F),forcePositive);
        VanillaCubicSpline120 mountains=mountainRidge(folded,p5,forcePositive);
        VanillaCubicSpline120 a=ridgeSpline(folded,p2-0.15F,0.5F*p5,0.5F*p5,0.5F*p5,0.6F*p5,0.5F);
        VanillaCubicSpline120 b=ridgeSpline(folded,p2,p6*p5,p3*p5,0.5F*p5,0.6F*p5,0.5F);
        VanillaCubicSpline120 c=ridgeSpline(folded,p2,p6,p6,p3,p4,0.5F);
        VanillaCubicSpline120 d=ridgeSpline(folded,p2,p6,p6,p3,p4,0.5F);
        VanillaCubicSpline120 high=builder(folded).add(-1.0F,p2).add(-0.4F,c).add(0.0F,p4+0.07F).build();
        VanillaCubicSpline120 end=ridgeSpline(folded,-0.02F,p7,p7,p3,p4,0.0F);
        Builder result=builder(erosion).add(-0.85F,mountains15).add(-0.7F,mountains10).add(-0.4F,mountains)
                .add(-0.35F,a).add(-0.1F,b).add(0.2F,c);
        if(addHigh)result.add(0.4F,d).add(0.45F,high).add(0.55F,high).add(0.58F,d);
        return result.add(0.7F,end).build();
    }

    private static VanillaCubicSpline120 ridgeSpline(Coordinate folded,float a,float b,float c,float d,float e,float minSlope){
        float slope0=Math.max(0.5F*(b-a),minSlope),slope1=5.0F*(c-b);
        return builder(folded).add(-1.0F,a,slope0).add(-0.4F,b,Math.min(slope0,slope1))
                .add(0.0F,c,slope1).add(0.4F,d,2.0F*(d-c)).add(1.0F,e,0.7F*(e-d)).build();
    }

    private static float foldRidges(float value){return -(Math.abs(Math.abs(value)-0.6666667F)-0.33333334F)*3.0F;}
    private static float slope(float a,float b,float x0,float x1){return (b-a)/(x1-x0);}
    private static float lerp(float t,float a,float b){return a+t*(b-a);}
}
