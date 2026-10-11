package com.mod.htgt6.sulimited.client;

import galaxyspace.systems.SolarSystem.SolarSystemBodies;
import java.util.Random;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;

/** Self-contained vacuum sky; it does not depend on GalaxySpace sky-image settings. */
public class SkyProviderCharon extends IRenderHandler {
    private static final ResourceLocation SUN = new ResourceLocation("textures/environment/sun.png");
    private static final ResourceLocation PLUTO_FALLBACK = new ResourceLocation(
            "galaxyspace", "textures/gui/celestialbodies/sol/pluto.png");
    // All vacuum providers use the same immutable star field. Allocate only on
    // the render thread and reuse it across dimension changes.
    private static int starList = -1;

    static int stars() {
        if (starList < 0) {
            starList = GLAllocation.generateDisplayLists(1);
            GL11.glNewList(starList, GL11.GL_COMPILE);
            buildStars();
            GL11.glEndList();
        }
        return starList;
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft minecraft) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glDepthMask(false);

        // Charon has no atmosphere: stars remain visible even while the Sun is above the horizon.
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.82F, 0.88F, 1.0F, 1.0F);
        GL11.glCallList(stars());

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        renderSun(world, minecraft, partialTicks);
        renderParentBody(minecraft);

        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private void renderSun(WorldClient world, Minecraft minecraft, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, 1, 0);
        minecraft.getTextureManager().bindTexture(SUN);
        drawSkyQuad(sunSize(world));
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glPopMatrix();
    }

    protected void renderParentBody(Minecraft minecraft) {
        GL11.glPushMatrix();
        // Pluto remains almost stationary because Pluto and Charon are mutually tidally locked.
        GL11.glRotatef(parentHorizontalAngle(), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(parentVerticalAngle(), 1.0F, 0.0F, 0.0F);
        minecraft.getTextureManager().bindTexture(parentTexture());
        drawSkyQuad(parentSize());
        GL11.glPopMatrix();
    }

    protected ResourceLocation parentTexture() {
        return SolarSystemBodies.planetPluto != null
                && SolarSystemBodies.planetPluto.getBodyIcon() != null
                ? SolarSystemBodies.planetPluto.getBodyIcon() : PLUTO_FALLBACK;
    }
    protected float parentHorizontalAngle() { return -58.0F; }
    protected float parentVerticalAngle() { return 24.0F; }
    protected float parentSize() { return 18.0F; }
    protected float sunSize(WorldClient world) {
        return world.provider instanceof IGalacticraftWorldProvider
                ? Math.max(0.28F, 10.0F * ((IGalacticraftWorldProvider) world.provider).getSolarSize())
                : 1.2F;
    }

    protected void drawSkyQuad(float size) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        // Standalone sky coordinates use the upper (+Y) hemisphere.  The old
        // -Y quad only works inside SkyProviderBase after its private rotations.
        tessellator.addVertexWithUV(-size, 100.0D, -size, 0.0D, 0.0D);
        tessellator.addVertexWithUV(size, 100.0D, -size, 1.0D, 0.0D);
        tessellator.addVertexWithUV(size, 100.0D, size, 1.0D, 1.0D);
        tessellator.addVertexWithUV(-size, 100.0D, size, 0.0D, 1.0D);
        tessellator.draw();
    }

    private static void buildStars() {
        Random random = new Random(10842L);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        for (int i = 0; i < 2200; i++) {
            double x = random.nextFloat() * 2.0F - 1.0F;
            double y = random.nextFloat() * 2.0F - 1.0F;
            double z = random.nextFloat() * 2.0F - 1.0F;
            double lengthSquared = x * x + y * y + z * z;
            if (lengthSquared >= 1.0D || lengthSquared <= 0.01D) continue;
            double invLength = 1.0D / Math.sqrt(lengthSquared);
            x *= invLength; y *= invLength; z *= invLength;
            double centerX = x * 100.0D, centerY = y * 100.0D, centerZ = z * 100.0D;
            double azimuth = Math.atan2(x, z);
            double sinAzimuth = Math.sin(azimuth), cosAzimuth = Math.cos(azimuth);
            double elevation = Math.atan2(Math.sqrt(x * x + z * z), y);
            double sinElevation = Math.sin(elevation), cosElevation = Math.cos(elevation);
            double rotation = random.nextDouble() * Math.PI * 2.0D;
            double sinRotation = Math.sin(rotation), cosRotation = Math.cos(rotation);
            double size = 0.10D + random.nextDouble() * 0.18D;
            for (int corner = 0; corner < 4; corner++) {
                double localX = ((corner & 2) - 1) * size;
                double localY = (((corner + 1) & 2) - 1) * size;
                double rotatedX = localX * cosRotation - localY * sinRotation;
                double rotatedY = localY * cosRotation + localX * sinRotation;
                double vertical = rotatedX * sinElevation;
                double radial = -rotatedX * cosElevation;
                double offsetX = radial * sinAzimuth - rotatedY * cosAzimuth;
                double offsetZ = rotatedY * sinAzimuth + radial * cosAzimuth;
                tessellator.addVertex(centerX + offsetX, centerY + vertical, centerZ + offsetZ);
            }
        }
        tessellator.draw();
    }
}
