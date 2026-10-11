package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Reusable context for hot chunk-filling loops. */
public final class MutableDensityContext120 implements DensityContext120 {
    private int x;
    private int y;
    private int z;

    public MutableDensityContext120 set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override public int blockX() { return x; }
    @Override public int blockY() { return y; }
    @Override public int blockZ() { return z; }
}
