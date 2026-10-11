package com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage;

/** Metadata: low two bits N/E/S/W, high two bits attachment. Support mask uses Forge sides. */
public final class PoiAttachment120 {
    public static int facingForSide(int side) { return side == 2 ? 0 : side == 5 ? 1 : side == 3 ? 2 : 3; }
    public static int sideForFacing(int facing) { return new int[]{2, 5, 3, 4}[facing & 3]; }
    public static boolean canRing(int metadata,int side,float hitY) {
        if(side<2||side>5||hitY>0.8124F)return false;
        int attachment=(metadata>>>2)&3;
        boolean sameAxis=((metadata&1)==0)==(side==2||side==3);
        return attachment==1|| (attachment==0?sameAxis:!sameAxis);
    }
    private static boolean supported(int mask, int side) { return (mask & (1 << side)) != 0; }
    public static int grindstone(int side, int facing) {
        return side == 0 ? 8 | facing : side == 1 ? facing : 4 | facingForSide(side);
    }
    public static int withPlacerFacing(boolean bell, int placed, int facing) {
        boolean wall = bell ? (placed & 12) >= 8 : (placed & 12) == 4;
        return wall ? placed : (placed & 12) | (facing & 3);
    }
    public static int bell(int side, int facing, int supports) {
        if (side < 2) return supported(supports, side ^ 1) ? (side == 0 ? 4 : 0) | facing : -1;
        int towardSupport = facingForSide(side ^ 1);
        if (supported(supports, side ^ 1))
            return (supported(supports, side) ? 12 : 8) | towardSupport;
        if (supported(supports, 0)) return towardSupport;
        return supported(supports, 1) ? 4 | towardSupport : -1;
    }
    public static int updateBell(int metadata, int supports) {
        int attachment = (metadata >>> 2) & 3;
        int facing = metadata & 3;
        if (attachment < 2) return supported(supports, attachment == 0 ? 0 : 1) ? metadata : -1;
        int side = sideForFacing(facing);
        boolean front = supported(supports, side), back = supported(supports, side ^ 1);
        if (front) return (back ? 12 : 8) | facing;
        return attachment == 3 && back ? 8 | ((facing + 2) & 3) : -1;
    }
    private PoiAttachment120() { }
}
