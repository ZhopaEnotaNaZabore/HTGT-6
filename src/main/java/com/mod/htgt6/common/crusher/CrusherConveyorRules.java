package com.mod.htgt6.common.crusher;

/** Pure conveyor policy shared with regression checks. Distances are controller-local blocks. */
public final class CrusherConveyorRules {
    public static final int POWER=8, MAX_ENTITIES=16, MAX_MOBS=3, DAMAGE_INTERVAL=20;
    public static final float DAMAGE=8F;
    public static final double END=6.30, SPEED=.055, MOB_GAP=2.1;
    public static final double MOB_END=7.10, DECK_Y=3.10;
    public static final int OUTPUT_TICKS=120;
    public static int advanceOutput(int ticks,boolean powered){return Math.min(OUTPUT_TICKS,Math.max(0,ticks)+(powered?1:0));}
    public static boolean canUnload(int ticks,boolean powered){return powered&&ticks>=OUTPUT_TICKS;}
    public static double outputZ(float ticks){
        double t=Math.max(0,Math.min(OUTPUT_TICKS,ticks));
        return t<20?7.9+t/20*1.8:9.7+(t-20)/100*4.95;
    }
    public static double outputY(float ticks){return 2.60-Math.max(0,Math.min(20,ticks))/20*1.17;}
    private CrusherConveyorRules(){}
    public static double[] worldOffset(int facing,double u,double v){
        switch(facing){case 3:return new double[]{-u,-v};case 4:return new double[]{v,-u};
            case 5:return new double[]{-v,u};default:return new double[]{u,v};}
    }
    public static double[] localOffset(int facing,double x,double z){
        switch(facing){case 3:return new double[]{-x,-z};case 4:return new double[]{-z,x};
            case 5:return new double[]{z,-x};default:return new double[]{x,z};}
    }
    public static boolean hasCapacity(int entities,int mobs,boolean mob){
        return entities<MAX_ENTITIES&&(!mob||mobs<MAX_MOBS);
    }
    public static double advance(double v,double limit,boolean running){return running?Math.min(limit,v+SPEED):v;}
}
