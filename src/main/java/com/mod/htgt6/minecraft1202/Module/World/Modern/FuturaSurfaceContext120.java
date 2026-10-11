package com.mod.htgt6.minecraft1202.Module.World.Modern;

/**
 * Mutable scalar equivalent of the 1.20.2 SurfaceRules.Context values.
 * One instance is reused while a column is scanned from top to bottom.
 */
public final class FuturaSurfaceContext120 {
    public enum CaveSurface { FLOOR, CEILING }

    private int y;
    private int surfaceDepth;
    private double surfaceSecondary;
    private int stoneDepthAbove;
    private int stoneDepthBelow;
    private int waterHeight;
    private int preliminarySurface;
    private boolean steep;
    private boolean cold;

    public FuturaSurfaceContext120 setColumn(int surfaceDepth, double surfaceSecondary,
                                             int preliminarySurface, boolean steep) {
        this.surfaceDepth = surfaceDepth;
        this.surfaceSecondary = surfaceSecondary;
        this.preliminarySurface = preliminarySurface;
        this.steep = steep;
        return this;
    }

    public FuturaSurfaceContext120 setBlock(int y, int stoneDepthAbove,
                                            int stoneDepthBelow, int waterHeight, boolean cold) {
        this.y = y;
        this.stoneDepthAbove = stoneDepthAbove;
        this.stoneDepthBelow = stoneDepthBelow;
        this.waterHeight = waterHeight;
        this.cold = cold;
        return this;
    }

    /** SurfaceRules.stoneDepthCheck, including its secondary-depth term. */
    public boolean stoneDepth(CaveSurface surface, int offset, boolean addSurfaceDepth,
                              int secondaryDepthRange) {
        int depth = surface == CaveSurface.FLOOR ? stoneDepthAbove : stoneDepthBelow;
        int addedDepth = addSurfaceDepth ? surfaceDepth : 0;
        int secondary = secondaryDepthRange == 0 ? 0
                : (int)map(surfaceSecondary, -1.0D, 1.0D, 0.0D, secondaryDepthRange);
        return depth <= 1 + offset + addedDepth + secondary;
    }

    /** SurfaceRules.waterBlockCheck. Integer.MIN_VALUE means no fluid above. */
    public boolean waterBlock(int offset, int surfaceDepthMultiplier, boolean addStoneDepth) {
        return waterHeight == Integer.MIN_VALUE
                || y + (addStoneDepth ? stoneDepthAbove : 0)
                >= waterHeight + offset + surfaceDepth * surfaceDepthMultiplier;
    }

    public boolean onFloor() {
        return stoneDepth(CaveSurface.FLOOR, 0, false, 0);
    }

    public boolean underFloor() {
        return stoneDepth(CaveSurface.FLOOR, 0, true, 0);
    }

    public boolean onCeiling() {
        return stoneDepth(CaveSurface.CEILING, 0, false, 0);
    }

    public boolean underCeiling() {
        return stoneDepth(CaveSurface.CEILING, 0, true, 0);
    }

    /** SurfaceRules.hole. */
    public boolean hole() { return surfaceDepth <= 0; }

    /** SurfaceRules.abovePreliminarySurface. */
    public boolean abovePreliminarySurface() { return y >= preliminarySurface; }

    /** Overworld's abovePreliminarySurface wrapper plus the caller's exact host allowlist. */
    public boolean mayApply(boolean replaceableBlock, boolean protectedBlock) {
        if (protectedBlock) return false;
        return replaceableBlock && abovePreliminarySurface();
    }

    public boolean steep() { return steep; }
    public boolean cold() { return cold; }
    public int y() { return y; }
    public int surfaceDepth() { return surfaceDepth; }
    public double surfaceSecondary() { return surfaceSecondary; }
    public int stoneDepthAbove() { return stoneDepthAbove; }
    public int stoneDepthBelow() { return stoneDepthBelow; }
    public int waterHeight() { return waterHeight; }

    public boolean yBlockCheck(int physicalAnchor,int surfaceDepthMultiplier,boolean addStoneDepth){
        return y+(addStoneDepth?stoneDepthAbove:0)>=physicalAnchor+surfaceDepth*surfaceDepthMultiplier;
    }

    private static double map(double value, double fromLow, double fromHigh,
                              double toLow, double toHigh) {
        return toLow + (value - fromLow) / (fromHigh - fromLow) * (toHigh - toLow);
    }
}
