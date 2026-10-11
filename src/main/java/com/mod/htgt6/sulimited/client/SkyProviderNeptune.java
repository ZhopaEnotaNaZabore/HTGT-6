package com.mod.htgt6.sulimited.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;

/** Opaque atmosphere in every view direction, independent of the vacuum sky. */
public final class SkyProviderNeptune extends IRenderHandler {
    @Override
    public void render(float partialTicks, WorldClient world, Minecraft minecraft) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_FOG);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDepthMask(false);
            float flash = world.lastLightningBolt > 0 ? 0.22F : 0.0F;
            float storm = world.getWeightedThunderStrength(partialTicks);
            GL11.glColor4f(0.035F + flash, 0.19F - storm * 0.06F + flash,
                    0.48F - storm * 0.16F + flash, 1.0F);
            // Rotate a face around the camera to close all six sides of the sky.
            for (int face = 0; face < 6; face++) {
                GL11.glPushMatrix();
                if (face < 4) GL11.glRotatef(face * 90.0F, 1, 0, 0);
                else GL11.glRotatef(face == 4 ? 90.0F : -90.0F, 0, 0, 1);
                drawFace();
                GL11.glPopMatrix();
            }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void drawFace() {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertex(-100, 100, -100); t.addVertex(-100, 100, 100);
        t.addVertex(100, 100, 100); t.addVertex(100, 100, -100);
        t.draw();
    }
}
