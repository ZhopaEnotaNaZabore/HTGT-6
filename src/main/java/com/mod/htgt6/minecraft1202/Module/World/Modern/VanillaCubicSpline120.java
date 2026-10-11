package com.mod.htgt6.minecraft1202.Module.World.Modern;

import java.util.ArrayList;
import java.util.List;

/** Allocation-free evaluation port of Minecraft 1.20.2 CubicSpline (ara). */
public abstract class VanillaCubicSpline120 {
    public interface Coordinate { float apply(DensityContext120 context); }

    public abstract float sample(DensityContext120 context);

    public static VanillaCubicSpline120 constant(float value){return new Constant(value);}
    public static Builder builder(Coordinate coordinate){return new Builder(coordinate);}

    private static final class Constant extends VanillaCubicSpline120 {
        private final float value;
        private Constant(float value){this.value=value;}
        @Override public float sample(DensityContext120 context){return value;}
    }

    private static final class Multipoint extends VanillaCubicSpline120 {
        private final Coordinate coordinate;
        private final float[] locations,derivatives;
        private final VanillaCubicSpline120[] values;
        private Multipoint(Coordinate coordinate,float[] locations,VanillaCubicSpline120[] values,float[] derivatives){
            this.coordinate=coordinate;this.locations=locations;this.values=values;this.derivatives=derivatives;
        }
        @Override public float sample(DensityContext120 context){
            float point=coordinate.apply(context);
            int index=lowerBound(point)-1,last=locations.length-1;
            if(index<0)return extrapolate(point,0,values[0].sample(context));
            if(index==last)return extrapolate(point,last,values[last].sample(context));
            float left=locations[index],right=locations[index+1];
            float t=(point-left)/(right-left);
            float leftValue=values[index].sample(context),rightValue=values[index+1].sample(context);
            float delta=right-left;
            float leftTerm=derivatives[index]*delta-(rightValue-leftValue);
            float rightTerm=-derivatives[index+1]*delta+(rightValue-leftValue);
            return lerp(t,leftValue,rightValue)+t*(1.0F-t)*lerp(t,leftTerm,rightTerm);
        }
        private float extrapolate(float point,int index,float value){
            float derivative=derivatives[index];
            return derivative==0.0F?value:value+derivative*(point-locations[index]);
        }
        private int lowerBound(float value){
            int low=0,high=locations.length;
            while(low<high){int mid=(low+high)>>>1;if(value<locations[mid])high=mid;else low=mid+1;}
            return low;
        }
    }

    public static final class Builder {
        private final Coordinate coordinate;
        private final List<Float> locations=new ArrayList<Float>();
        private final List<VanillaCubicSpline120> values=new ArrayList<VanillaCubicSpline120>();
        private final List<Float> derivatives=new ArrayList<Float>();
        private Builder(Coordinate coordinate){if(coordinate==null)throw new NullPointerException("coordinate");this.coordinate=coordinate;}
        public Builder add(float location,float value){return add(location,constant(value),0.0F);}
        public Builder add(float location,float value,float derivative){return add(location,constant(value),derivative);}
        public Builder add(float location,VanillaCubicSpline120 value){return add(location,value,0.0F);}
        public Builder add(float location,VanillaCubicSpline120 value,float derivative){
            if(value==null)throw new NullPointerException("value");
            if(!locations.isEmpty()&&location<=locations.get(locations.size()-1))
                throw new IllegalArgumentException("Spline points must be ascending");
            locations.add(location);values.add(value);derivatives.add(derivative);return this;
        }
        public VanillaCubicSpline120 build(){
            int size=locations.size();if(size==0)throw new IllegalStateException("No spline points");
            float[] points=new float[size],slopes=new float[size];
            VanillaCubicSpline120[] nested=new VanillaCubicSpline120[size];
            for(int i=0;i<size;i++){points[i]=locations.get(i);nested[i]=values.get(i);slopes[i]=derivatives.get(i);}
            return new Multipoint(coordinate,points,nested,slopes);
        }
    }

    private static float lerp(float t,float a,float b){return a+t*(b-a);}
}
