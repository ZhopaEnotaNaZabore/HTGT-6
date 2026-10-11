package com.mod.htgt6.sulimited.client;

import galaxyspace.systems.SolarSystem.SolarSystemBodies;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Vacuum sky used by Saturn's inner icy moons. */
public final class SkyProviderSaturnMoon extends SkyProviderCharon {
    private static final ResourceLocation SATURN_FALLBACK = new ResourceLocation(
            "galaxyspace", "textures/gui/celestialbodies/sol/saturn.png");
    private static final ResourceLocation SATURN_RINGS = new ResourceLocation(
            "galaxyspace", "textures/gui/celestialbodies/sol/saturn_rings.png");

    private final float horizontalAngle;
    private final float verticalAngle;
    private final float size;
    private final float ringRotation;

    public SkyProviderSaturnMoon(float horizontalAngle, float verticalAngle,
            float size, float ringRotation) {
        this.horizontalAngle = horizontalAngle;
        this.verticalAngle = verticalAngle;
        this.size = size;
        this.ringRotation = ringRotation;
    }

    @Override
    protected ResourceLocation parentTexture() {
        return SolarSystemBodies.planetSaturn != null
                && SolarSystemBodies.planetSaturn.getBodyIcon() != null
                ? SolarSystemBodies.planetSaturn.getBodyIcon() : SATURN_FALLBACK;
    }

    @Override protected float parentHorizontalAngle() { return this.horizontalAngle; }
    @Override protected float parentVerticalAngle() { return this.verticalAngle; }
    @Override protected float parentSize() { return this.size; }

    @Override
    protected void renderParentBody(Minecraft minecraft) {
        GL11.glPushMatrix();
        GL11.glRotatef(parentHorizontalAngle(), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(parentVerticalAngle(), 1.0F, 0.0F, 0.0F);

        // Draw the transparent rings first, then Saturn itself over their
        // centre.  Depth writes are disabled by the enclosing sky renderer.
        GL11.glPushMatrix();
        GL11.glRotatef(this.ringRotation, 0.0F, 1.0F, 0.0F);
        minecraft.getTextureManager().bindTexture(SATURN_RINGS);
        drawSkyQuad(parentSize() * 1.85F);
        GL11.glPopMatrix();

        minecraft.getTextureManager().bindTexture(parentTexture());
        drawSkyQuad(parentSize());
        GL11.glPopMatrix();
    }
}
