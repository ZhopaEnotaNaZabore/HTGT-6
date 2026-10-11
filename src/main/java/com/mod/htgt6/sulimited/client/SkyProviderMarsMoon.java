package com.mod.htgt6.sulimited.client;

import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysExpanded;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import net.minecraft.util.ResourceLocation;

/** Stable vacuum sky with Mars fixed above its tidally locked moons. */
public final class SkyProviderMarsMoon extends SkyProviderCharon {
    private static final ResourceLocation MARS_FALLBACK = new ResourceLocation(
            "galacticraftmars", "textures/gui/celestialbodies/mars.png");
    private final float horizontalAngle, verticalAngle, size;
    public SkyProviderMarsMoon(float horizontalAngle,float verticalAngle,float size){this.horizontalAngle=horizontalAngle;this.verticalAngle=verticalAngle;this.size=size;}
    protected ResourceLocation parentTexture(){Planet mars=SolSysExpanded.phobos!=null?SolSysExpanded.phobos.getParentPlanet():null;return mars!=null&&mars.getBodyIcon()!=null?mars.getBodyIcon():MARS_FALLBACK;}
    protected float parentHorizontalAngle(){return horizontalAngle;}
    protected float parentVerticalAngle(){return verticalAngle;}
    protected float parentSize(){return size;}
}
