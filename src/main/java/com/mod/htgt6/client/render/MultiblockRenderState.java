package com.mod.htgt6.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/** Scoped fixed-function state: no lightmap/blend/depth leakage between machine renderers. */
final class MultiblockRenderState {
    private final int textureUnit = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    private final float lightX=OpenGlHelper.lastBrightnessX, lightY=OpenGlHelper.lastBrightnessY;
    MultiblockRenderState(String material) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("htgt6", "textures/blocks/multiblock_v2_"+material+"_inner.png"));
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,220,220);
    }
    void close() {
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,lightX,lightY);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
        OpenGlHelper.setActiveTexture(textureUnit);
    }
}
