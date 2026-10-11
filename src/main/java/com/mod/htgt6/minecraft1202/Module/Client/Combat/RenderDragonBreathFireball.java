package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Camera-facing 1.9 dragon-fireball sprite using Mojang's dedicated texture. */
public final class RenderDragonBreathFireball extends Render {
    private static final ResourceLocation TEXTURE = new ResourceLocation("htgt6",
            "textures/entity/combat_update/dragon_fireball.png");

    @Override public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTick) {
        GL11.glPushMatrix();
        bindEntityTexture(entity);
        GL11.glTranslatef((float) x, (float) y, (float) z);
        GL11.glEnable(32826);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        GL11.glRotatef(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        tessellator.addVertexWithUV(-0.5D, -0.25D, 0.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV(0.5D, -0.25D, 0.0D, 1.0D, 1.0D);
        tessellator.addVertexWithUV(0.5D, 0.75D, 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV(-0.5D, 0.75D, 0.0D, 0.0D, 0.0D);
        tessellator.draw();
        GL11.glDisable(32826);
        GL11.glPopMatrix();
    }

    @Override protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}
