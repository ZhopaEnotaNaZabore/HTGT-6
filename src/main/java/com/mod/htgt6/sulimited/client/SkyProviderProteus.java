package com.mod.htgt6.sulimited.client;

import galaxyspace.systems.SolarSystem.SolarSystemBodies;
import net.minecraft.util.ResourceLocation;

/** Vacuum sky above Proteus with Neptune dominating the fixed parent-body view. */
public final class SkyProviderProteus extends SkyProviderCharon {
    private static final ResourceLocation NEPTUNE_FALLBACK = new ResourceLocation(
            "galaxyspace", "textures/gui/celestialbodies/sol/neptune.png");

    @Override protected ResourceLocation parentTexture() {
        return SolarSystemBodies.planetNeptune != null
                && SolarSystemBodies.planetNeptune.getBodyIcon() != null
                ? SolarSystemBodies.planetNeptune.getBodyIcon() : NEPTUNE_FALLBACK;
    }
    @Override protected float parentHorizontalAngle() { return 47.0F; }
    @Override protected float parentVerticalAngle() { return 31.0F; }
    @Override protected float parentSize() { return 28.0F; }
}
