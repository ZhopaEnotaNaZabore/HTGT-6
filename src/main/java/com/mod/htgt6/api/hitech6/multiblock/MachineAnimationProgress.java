package com.mod.htgt6.api.hitech6.multiblock;

/** Visual prediction only. Never starts a new cycle or changes server recipe progress. */
public final class MachineAnimationProgress {
    public static float ticks(int progress, int duration, long elapsedTicks, float partialTicks, boolean running) {
        if (duration <= 0) return 0F;
        double value = progress;
        if (running) {
            // Subtract snapshot time as longs first; absolute world time must not enter float arithmetic.
            value += Math.max(0L, elapsedTicks) + (double) Math.max(0F, Math.min(1F, partialTicks));
        }
        return (float) Math.max(0D, Math.min(duration, value));
    }

    private MachineAnimationProgress() { }
}
