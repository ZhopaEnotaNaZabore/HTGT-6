package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Network.VillageAndPillage.RaidNetwork150.Snapshot;

public final class RaidHudState160 {
    private Snapshot current;
    private int age;
    private Object world;

    public RaidHudState160() {
    }

    public void accept(Snapshot snapshot, Object currentWorld, int currentDimension) {
        if (currentWorld == null || snapshot == null) {
            clear();
            return;
        }

        // Invalidate the previous world before filtering the packet.
        if (world != currentWorld) {
            clear();
            world = currentWorld;
        }

        if (snapshot.dimension != currentDimension) {
            return;
        }

        if (!snapshot.visible) {
            clear();
            return;
        }

        float progress = snapshot.progress;
        progress = Float.isNaN(progress)
                ? 0.0F
                : Math.max(0.0F, Math.min(1.0F, progress));

        current = new Snapshot(
                snapshot.dimension,
                true,
                Math.max(0, Math.min(2, snapshot.status)),
                Math.max(0, Math.min(1024, snapshot.remaining)),
                progress);
        age = 0;
    }

    public void tick(Object currentWorld) {
        if (currentWorld == null || world != currentWorld) {
            clear();
            return;
        }

        if (current != null && ++age > 60) {
            clear();
        }
    }

    public Snapshot snapshot() {
        return current == null ? null : new Snapshot(
                current.dimension,
                current.visible,
                current.status,
                current.remaining,
                current.progress);
    }

    private void clear() {
        current = null;
        age = 0;
        world = null;
    }
}
