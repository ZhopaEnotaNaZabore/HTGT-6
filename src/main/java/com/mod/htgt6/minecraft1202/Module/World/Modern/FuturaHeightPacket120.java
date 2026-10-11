package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Versioned wire primitives for the future S21/S26 high-section sidecar and block updates. */
public final class FuturaHeightPacket120 {
    public static final int MAGIC=0x46555438; // FUT8
    public static final int VERSION=1;
    /** Optional packet-tail marker. Vanilla-world S22/S23 packets have no tail. */
    public static final int BLOCK_UPDATE_MAGIC=0xF9;
    public static final int LOW_SECTION_MASK=0x0000FFFF;
    /** Only sections 16..23 exist in a 24-section Futura chunk. */
    public static final int HIGH_SECTION_MASK=0x00FF0000;
    public static final int VALID_SECTION_MASK=LOW_SECTION_MASK|HIGH_SECTION_MASK;

    /** Packs local X/Z (4 bits each) and Futura Y (9 bits) without vanilla's byte-Y truncation. */
    public static int packLocalPosition(int x,int y,int z){
        if((x&~15)!=0||(z&~15)!=0||y<0||y>=FuturaHeight120.PHYSICAL_HEIGHT)
            throw new IllegalArgumentException("Position outside Futura chunk: "+x+","+y+","+z);
        return x|(z<<4)|(y<<8);
    }
    public static int localX(int packed){return packed&15;}
    public static int localZ(int packed){return packed>>>4&15;}
    public static int y(int packed){return packed>>>8&511;}
    public static int lowMask(int mask32){return mask32&LOW_SECTION_MASK;}
    public static int highMask(int mask32){return mask32>>>16&0xFF;}
    public static int joinMasks(int low,int high){return low&0xFFFF|(high&0xFF)<<16;}
    public static boolean isValidSectionMask(int mask32){return (mask32&~VALID_SECTION_MASK)==0;}

    public static void validateContract(){
        int packed=packLocalPosition(15,FuturaHeight120.MAX_BUILD_Y,15);
        if(localX(packed)!=15||localZ(packed)!=15||y(packed)!=FuturaHeight120.MAX_BUILD_Y)
            throw new IllegalStateException("Futura position codec is not reversible");
        int mask=joinMasks(0xA55A,0xA5);
        if(mask!=0x00A5A55A||lowMask(mask)!=0xA55A||highMask(mask)!=0xA5)
            throw new IllegalStateException("Futura section-mask codec is not reversible");
        if(!isValidSectionMask(mask)||isValidSectionMask(1<<FuturaHeight120.SECTION_COUNT))
            throw new IllegalStateException("Futura section-mask bounds are invalid");
        boolean rejected=false;
        try{packLocalPosition(0,FuturaHeight120.PHYSICAL_HEIGHT,0);}
        catch(IllegalArgumentException expected){rejected=true;}
        if(!rejected)throw new IllegalStateException("Futura position codec accepted exclusive upper Y");
    }
    private FuturaHeightPacket120(){ }
}
