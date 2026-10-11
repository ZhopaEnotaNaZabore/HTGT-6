package com.mod.htgt6.common.turbine;

/** 1 steam mB = 0.5 base EU; rotor efficiency is applied exactly once. */
public final class SteamTurbineBalance {
    public static final long SCALE=1000000L;
    public static final int STEAM_PER_WATER=200;
    private SteamTurbineBalance() {}
    public static final class Step {
        public final long reserve, generated;
        public final int steam;
        private Step(long reserve,long generated,int steam) {this.reserve=reserve;this.generated=generated;this.steam=steam;}
    }
    public static Step plan(long reserve,int available,long maxFlow,long microEuPerMb,long budget) {
        reserve=Math.max(0,Math.min(100000000000000000L,reserve));
        budget=Math.max(0,Math.min(34359738368L,budget));
        microEuPerMb=Math.max(0,Math.min(1000000000L,microEuPerMb));
        long missing=budget*SCALE-reserve;
        int burn=0;
        if(missing>0 && microEuPerMb>0 && budget>0)
            burn=(int)Math.max(0,Math.min(Math.min(available,maxFlow),1+(missing-1)/microEuPerMb));
        reserve+=(long)burn*microEuPerMb;
        long generated=Math.min(budget,reserve/SCALE);
        return new Step(reserve-generated*SCALE,generated,burn);
    }
}
