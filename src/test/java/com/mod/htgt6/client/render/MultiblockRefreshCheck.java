package com.mod.htgt6.client.render;

import com.mod.htgt6.common.multiblock.MultiblockTrimLayout;

/** Standalone CPU checks for cosmetic structure compatibility and paused animation. */
public final class MultiblockRefreshCheck {
    private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    public static void main(String[] args) {
        MultiblockAnimationClock clock=new MultiblockAnimationClock();
        Object first=new Object(),second=new Object();
        check(clock.sample(first,100,false)==0,"Idle starts stationary");
        check(clock.sample(first,101,true)==1,"Active advance");
        check(clock.sample(first,102,false)==1,"Paused pose retained");
        check(clock.sample(first,200,false)==1,"No idle advance");
        check(clock.sample(first,201,true)==2,"Resume without snap");
        check(clock.sample(first,301,true)==4,"Off-screen delta bounded");
        check(clock.sample(second,301,true)==0,"Independent machine clock");
        check(clock.sample(first,20,true)==4,"World time rewind safe");
        int mining=0,chemical=0,mixer=0,press=0,foundry=0;
        for(int u=-4;u<=4;u++) for(int y=0;y<13;y++) for(int v=0;v<16;v++) {
            if(Math.abs(u)<=2 && v<5 && y<12 && MultiblockTrimLayout.mining(u,y,v)) {
                check(y==4 && Math.abs(u)==2 && (v==0 || v==4),"Mining ring only"); mining++;
            }
            if(v<9 && MultiblockTrimLayout.chemical(u,y,v)) {
                check((y==1 || y==8) && Math.abs(u)==4 && (v==0 || v==8),"Chemical ring only"); chemical++;
            }
            if(Math.abs(u)<=2 && v<5 && y<10) {
                boolean casing=y==9 || y>0 && (Math.abs(u)==2 || v==0 || v==4)
                        && (Math.abs(u)==2 && (v==0 || v==4) || y==5);
                if(casing && MultiblockTrimLayout.mixer(u,y,v)) mixer++;
                if(y<5 && MultiblockTrimLayout.compressor(u,y,v)) {
                    check(y>0 && (y==4 || Math.abs(u)==2 && (v==0 || v==4)),"Press shell only"); press++;
                }
            }
            if(Math.abs(u)<=2 && y<7 && MultiblockTrimLayout.foundry(u,y,v)) {
                check(v>0 && (y==0 || y==6 || v==15 || Math.abs(u)==2 && v==8),"Foundry shell only"); foundry++;
            }
            check(!MultiblockTrimLayout.mixer(u,0,v),"Mixer service floor preserved");
            check(!MultiblockTrimLayout.compressor(u,0,v),"Press service floor preserved");
            check(!MultiblockTrimLayout.foundry(u,y,0),"Foundry service wall preserved");
        }
        check(mining==4 && chemical==8 && mixer==60 && press==28 && foundry==86,"Rib counts");
        System.out.println("PASS: rib counts 4/8/60/28/86; service cells, independent clocks, pause/resume");
    }
}
