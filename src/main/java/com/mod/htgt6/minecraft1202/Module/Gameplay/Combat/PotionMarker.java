package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import net.minecraft.potion.Potion;

/** Network-synchronised marker used by the adapted Spectral Arrow renderer contract. */
public final class PotionMarker extends Potion {
    public PotionMarker(int id, boolean harmful, int color, String name) {
        super(id, harmful, color);
        setPotionName(name);
    }
    @Override public boolean isReady(int duration, int amplifier) { return false; }
}
