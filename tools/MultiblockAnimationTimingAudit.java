package com.mod.htgt6.client.render;

/** Offline scenarios; uses the production animation clock, not a live server. */
public final class MultiblockAnimationTimingAudit {
    public static void main(String[] args) {
        for (long age : new long[] {0L, 1000L, 1000000L, 10000000L, 100000000L, 1000000000L, 1600000000L}) {
            MultiblockAnimationClock actual = new MultiblockAnimationClock();
            MultiblockAnimationClock precise = new MultiblockAnimationClock();
            Object tile = new Object();
            double last = 0, lastPrecise = 0;
            int frozen = 0, frozenPrecise = 0;
            for (int frame = 0; frame <= 600; frame++) {
                long tick = age + frame / 3;
                float partial = (frame % 3) / 3F;
                double a = actual.sample(tile, tick + partial, true);
                double b = precise.sample(tile, MultiblockAnimationClock.frameTime(tick, partial), true);
                if (frame > 0 && Math.abs(b - lastPrecise - 1D / 3D) > .001)
                    throw new AssertionError("Uneven frame at age=" + age + " frame=" + frame);
                if (frame > 0 && a == last) frozen++;
                if (frame > 0 && b == lastPrecise) frozenPrecise++;
                last = a; lastPrecise = b;
            }
            System.out.println("age=" + age + " frozenFrames=" + frozen + "/600"
                    + " elapsed=" + last + " preciseFrozen=" + frozenPrecise
                    + " preciseElapsed=" + lastPrecise);
            if (frozenPrecise != 0 || Math.abs(lastPrecise - 200) > .001)
                throw new AssertionError("Precise clock regression");
        }
        MultiblockAnimationClock clock = new MultiblockAnimationClock();
        Object tile = new Object();
        clock.sample(tile, 100, true);
        clock.sample(tile, 101, true);
        double corrected = clock.sample(tile, 96, true);
        double resumed = clock.sample(tile, 97, true);
        System.out.println("backwardTimeCorrection: elapsed=" + corrected + " next=" + resumed);
        if (corrected != 1 || resumed != 2) throw new AssertionError("Unexpected correction behavior");
    }
}
