package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

/** Server-owned BrushableBlockEntity timing, independent of each player's brush animation. */
public final class BrushingProgress120 {
    private int progress;
    private long cooldownEnds;
    private long resetAt;

    public boolean brush(long now) {
        // Even a swing during cooldown postpones decay in vanilla.
        resetAt = now + 40;
        if (now < cooldownEnds || progress >= 10) return false;
        cooldownEnds = now + 10;
        progress++;
        return true;
    }

    public boolean tick(long now) {
        if (progress == 0 || now < resetAt) return false;
        progress = Math.max(0, progress - 2);
        resetAt = progress == 0 ? 0 : now + 4;
        if (progress == 0) cooldownEnds = 0;
        return true;
    }

    public int progress() { return progress; }
    public long cooldownEnds() { return cooldownEnds; }
    public long resetAt() { return resetAt; }
    public int dusted() { return progress == 0 ? 0 : progress < 3 ? 1 : progress < 6 ? 2 : 3; }

    /** Preserve existing backport saves; vanilla's progress is transient. */
    public void restore(int value, long cooldown, long reset) {
        progress = Math.max(0, Math.min(9, value));
        cooldownEnds = progress == 0 ? 0 : cooldown;
        resetAt = progress == 0 ? 0 : reset;
    }
}
