package com.mod.htgt6.sulimited.client;

import com.mod.htgt6.sulimited.galacticraft.Glise792.sysGliese792;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;

/** No constructor GL work or per-dimension display lists. One shared lazy star field. */
public final class SkyProviderGliese extends IRenderHandler {
    private static final ResourceLocation STAR = new ResourceLocation("htgt6", "textures/gui/celestialbodies/star_Gliese792.png");
    private final boolean oceanWorld;
    private long textureTick = Long.MIN_VALUE;
    private ResourceLocation star, neighbour, glow;

    public SkyProviderGliese(boolean oceanWorld) { this.oceanWorld = oceanWorld; }

    private void updateFrames(long tick) {
        if (textureTick == tick) return;
        textureTick = tick;
        star = CelestialTexturePainter.getSkyStarFrame(tick);
        neighbour = CelestialTexturePainter.getSkyPlanetFrame(oceanWorld, tick);
        glow = oceanWorld ? CelestialTexturePainter.getAtmosphereGlowFrame(tick) : null;
        if (star == null) star = STAR;
        if (neighbour == null) {
            CelestialBody body = oceanWorld ? sysGliese792.planetGlieseB : sysGliese792.planetGlieseC;
            if (body != null) neighbour = body.getBodyIcon();
        }
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        if (mc.renderViewEntity != null && mc.renderViewEntity.isInsideOfMaterial(Material.water)) return;
        updateFrames(world.getTotalWorldTime());
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            float daylight = world.getSunBrightness(partialTicks);
            drawAtmosphere(daylight);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            float stars = (1.0F - daylight) * (oceanWorld ? 0.7F : 0.18F);
            GL11.glPushMatrix();
            try {
                GL11.glRotatef(world.getCelestialAngle(partialTicks) * 360, 1, 0, 0);
                if (stars > 0.02F) {
                    GL11.glColor4f(0.85F, 0.9F, 1.0F, stars);
                    GL11.glCallList(SkyProviderCharon.stars());
                }
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, 1, 0);
                if (glow != null) drawImage(mc, glow, 23, 0.72F);
                drawDisc(mc, star, oceanWorld ? 4 : 8, oceanWorld ? 0.95F : 0.65F,
                        GlieseDiscAnimation.angle(world.getTotalWorldTime(), partialTicks, 4800));
            } finally {
                GL11.glPopMatrix();
            }
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            if (neighbour != null) {
                GL11.glRotatef(oceanWorld ? -58 : 42, 0, 1, 0);
                GL11.glRotatef(oceanWorld ? 28 : -18, 1, 0, 0);
                drawDisc(mc, neighbour, oceanWorld ? 7.5F : 5, oceanWorld ? 0.8F : 0.4F,
                        GlieseDiscAnimation.angle(world.getTotalWorldTime(), partialTicks, oceanWorld ? 2400 : 3600));
            }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void drawAtmosphere(float daylight) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        // Four vertical faces plus caps, batched into one draw call.
        for (int side = 0; side < 4; side++) {
            double x1 = side == 0 || side == 3 ? -100 : 100;
            double z1 = side < 2 ? -100 : 100;
            double x2 = side < 2 ? 100 : -100;
            double z2 = side == 0 || side == 3 ? -100 : 100;
            vertex(t, x1, -100, z1, daylight); vertex(t, x2, -100, z2, daylight);
            vertex(t, x2, 100, z2, daylight); vertex(t, x1, 100, z1, daylight);
        }
        for (int y = -100; y <= 100; y += 200) {
            vertex(t,-100,y,-100,daylight); vertex(t,100,y,-100,daylight);
            vertex(t,100,y,100,daylight); vertex(t,-100,y,100,daylight);
        }
        t.draw();
    }
    private void vertex(Tessellator t, double x, double y, double z, float day) {
        float brightness = 0.06F + day * 0.94F;
        float high = (float)((y+100)/200);
        float red = oceanWorld ? 0.38F-high*0.2F : 0.28F-high*0.19F;
        float green = oceanWorld ? 0.59F-high*0.21F : 0.21F-high*0.16F;
        float blue = oceanWorld ? 0.66F-high*0.15F : 0.17F-high*0.14F;
        t.setColorOpaque_F(red*brightness,green*brightness,blue*brightness);
        t.addVertex(x,y,z);
    }
    private void drawImage(Minecraft mc, ResourceLocation image, float size, float opacity) {
        mc.getTextureManager().bindTexture(image);
        GL11.glColor4f(1,1,1,opacity);
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(-size,100,-size,0,0); t.addVertexWithUV(size,100,-size,1,0);
        t.addVertexWithUV(size,100,size,1,1); t.addVertexWithUV(-size,100,size,0,1);
        t.draw();
    }

    private void drawDisc(Minecraft mc, ResourceLocation image, float size, float opacity, float angle) {
        GL11.glPushMatrix();
        try {
            // The sky billboard lies in XZ at y=100; rotating about local Y
            // preserves its centre, circular silhouette and original alpha.
            GL11.glRotatef(angle, 0, 1, 0);
            drawImage(mc, image, size, opacity);
        } finally { GL11.glPopMatrix(); }
    }
}
