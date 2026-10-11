package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

/** Pure part of the official 1.20.2 TargetBlock hit calculation. */
public final class TargetSignal116 {
    public static int strength(int side,double x,double y,double z){
        if(side<0||side>5||!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z))return 0;
        double dx=Math.abs(x-Math.floor(x)-.5D),dy=Math.abs(y-Math.floor(y)-.5D),dz=Math.abs(z-Math.floor(z)-.5D);
        double distance=side<2?Math.max(dx,dz):side<4?Math.max(dx,dy):Math.max(dy,dz);
        return Math.max(1,(int)Math.ceil(15D*Math.max(0D,Math.min(1D,(.5D-distance)/.5D))));
    }
    public static int pulseTicks(boolean arrowLike){return arrowLike?20:8;}
    private TargetSignal116(){}
}
