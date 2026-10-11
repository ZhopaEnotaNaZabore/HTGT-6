package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

/** Side-neutral compass math, in turns. No target or animation singleton lives here. */
public final class LodestoneMath116 {
    public static double wrap(double turns){return turns-Math.floor(turns);}
    public static double bearing(double x,double z,double targetX,double targetZ,double yaw){
        return wrap(.75D-yaw/360D+Math.atan2(targetZ-z,targetX-x)/(2D*Math.PI));
    }
    public static int textureFrame(double angle){return ((int)Math.floor(wrap(angle)*32D+.5D)+16)&31;}
    public static final class Wobble {
        private double angle,velocity;
        private long tick=Long.MIN_VALUE;
        public double update(long now,double target){
            if(now!=tick){
                if(now<tick){angle=wrap(target);velocity=0;}
                tick=now;
                double delta=wrap(target-angle+.5D)-.5D;
                velocity=(velocity+delta*.1D)*.8D;
                angle=wrap(angle+velocity);
            }
            return angle;
        }
    }
    private LodestoneMath116(){}
}
