package com.mod.htgt6.common.turbine;

/** Controller at front centre (0,0,0); envelope is 7 wide, 7 high, 13 deep. */
public final class SteamTurbineGeometry {
    public static final int AIR=0, CONTROLLER=1, BASE=2, CASING=3, RIB=4, GLASS=5;
    private SteamTurbineGeometry() {}
    public static int element(int u,int y,int v) {
        if(u==0 && y==0 && v==0) return CONTROLLER;
        // Open 5x5 front socket with the controller hub retained in its centre.
        if(v==0 && Math.abs(u)<=2 && Math.abs(y)<=2) return AIR;
        if(y==-3) return BASE;
        boolean side=Math.abs(u)==3, end=v==0||v==12, horizontal=Math.abs(y)==3;
        if((side&&end)||(side&&horizontal)||(end&&horizontal)) return RIB;
        if(side && y>=-1 && y<=1 && v>=2 && v<=10) return GLASS;
        return side||end||horizontal ? CASING : AIR;
    }
}
