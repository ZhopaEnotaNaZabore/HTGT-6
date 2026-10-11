package com.mod.htgt6.sulimited.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;

/** Fast, deep methane cloud decks for Neptune's upper atmosphere. */
public final class CloudRendererNeptune extends IRenderHandler {
    public static final CloudRendererNeptune INSTANCE = new CloudRendererNeptune();
    private static final ResourceLocation CLOUDS = new ResourceLocation("textures/environment/clouds.png");

    private CloudRendererNeptune() { }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft minecraft) {
        Entity camera = minecraft.renderViewEntity;
        if (camera == null) return;

        double x = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks;
        double y = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks;
        double z = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks;
        float storm = world.isThundering() ? 1.0F : 0.0F;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glDepthMask(false);
        minecraft.getTextureManager().bindTexture(CLOUDS);

        // Transparent layers must be drawn farthest first, including when the
        // camera descends between or below them.
        int[] order = y < 175 ? new int[] {2, 1, 0} : y < 180 ? new int[] {2, 0, 1}
                : y < 186 ? new int[] {0, 2, 1} : new int[] {0, 1, 2};
        for (int layer : order) {
            if (layer == 2) drawLayer(world, partialTicks, x, y, z, 191, .032, .30F + storm * .16F, .34F, .48F, .68F);
            else if (layer == 1) drawLayer(world, partialTicks, x, y, z, 181, -.024, .25F + storm * .15F, .25F, .39F, .61F);
            else drawLayer(world, partialTicks, x, y, z, 169, .041, .22F + storm * .14F, .18F, .31F, .54F);
        }

        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private void drawLayer(WorldClient world, float partialTicks, double cameraX, double cameraY,
                           double cameraZ, double height, double speed, float alpha,
                           float red, float green, float blue) {
        double time = (world.getTotalWorldTime() + partialTicks) * speed;
        double u0 = (cameraX + time) / 256.0D;
        double v0 = (cameraZ + time * 0.37D) / 256.0D;
        double radius = 256.0D;
        double layerY = height - cameraY;
        Tessellator t = Tessellator.instance;
        GL11.glColor4f(red, green, blue, alpha);
        t.startDrawingQuads();
        t.addVertexWithUV(-radius, layerY, -radius, u0 - 1.0D, v0 - 1.0D);
        t.addVertexWithUV(-radius, layerY, radius, u0 - 1.0D, v0 + 1.0D);
        t.addVertexWithUV(radius, layerY, radius, u0 + 1.0D, v0 + 1.0D);
        t.addVertexWithUV(radius, layerY, -radius, u0 + 1.0D, v0 - 1.0D);
        t.draw();
    }
}
