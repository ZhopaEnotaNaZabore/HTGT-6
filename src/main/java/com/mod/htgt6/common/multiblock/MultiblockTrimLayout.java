package com.mod.htgt6.common.multiblock;

/** Cosmetic structural roles shared by validation and previews. Only apply to solid casing cells. */
public final class MultiblockTrimLayout {
    private MultiblockTrimLayout() { }
    public static boolean mining(int u,int y,int v) { return y==4 && Math.abs(u)==2 && (v==0 || v==4); }
    public static boolean chemical(int u,int y,int v) { return (y==1 || y==8) && Math.abs(u)==4 && (v==0 || v==8); }
    public static boolean mixer(int u,int y,int v) {
        return y>0 && ((Math.abs(u)==2 && (v==0 || v==4)) || y==5
                || y==9 && (Math.abs(u)==2 || v==0 || v==4));
    }
    public static boolean compressor(int u,int y,int v) {
        return y>0 && (Math.abs(u)==2 && (v==0 || v==4)
                || y==4 && (Math.abs(u)==2 || v==0 || v==4));
    }
    public static boolean foundry(int u,int y,int v) {
        return v>0 && (((y==0 || y==6) && (Math.abs(u)==2 || v==15))
                || Math.abs(u)==2 && (v==8 || v==15));
    }
}
