package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** The vanilla 1.7 foil pass applied to the backported 3-D trident model. */
public final class TridentGlintRenderer {
    private static final ResourceLocation GLINT =
            new ResourceLocation("textures/misc/enchanted_item_glint.png");

    static void render(ModelTrident model) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(GLINT);
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE, 1, 0);
        GL11.glColor4f(0.38F, 0.19F, 0.608F, 1.0F);
        for (int pass = 0; pass < 2; pass++) {
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glScalef(0.125F, 0.125F, 0.125F);
            float offset = (Minecraft.getSystemTime() % (pass == 0 ? 3000L : 4873L))
                    / (float)(pass == 0 ? 3000L : 4873L) * 8.0F;
            GL11.glTranslatef(pass == 0 ? offset : -offset, 0.0F, 0.0F);
            GL11.glRotatef(pass == 0 ? -50.0F : 10.0F, 0.0F, 0.0F, 1.0F);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            model.render();
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glPopMatrix();
        }
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glColor4f(1, 1, 1, 1);
    }

    private TridentGlintRenderer() { }
}
