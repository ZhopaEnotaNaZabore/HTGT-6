package com.mod.htgt6.sulimited.client;

import net.minecraft.client.Minecraft;

/** Vacuum sky for isolated trans-Neptunian objects. */
public final class SkyProviderOuterSol extends SkyProviderCharon {
    private final float apparentSunSize;

    public SkyProviderOuterSol(float apparentSunSize) {
        this.apparentSunSize = apparentSunSize;
    }

    @Override
    protected void renderParentBody(Minecraft minecraft) {
        // A dwarf planet has no giant parent body fixed in its sky.
    }

    @Override
    protected float sunSize(net.minecraft.client.multiplayer.WorldClient world) {
        return this.apparentSunSize;
    }
}
