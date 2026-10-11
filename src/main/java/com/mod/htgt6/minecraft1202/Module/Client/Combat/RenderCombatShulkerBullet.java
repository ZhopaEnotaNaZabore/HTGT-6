package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class RenderCombatShulkerBullet extends Render {
    private static final ResourceLocation TEXTURE = new ResourceLocation("htgt6", "textures/entity/combat_update/shulker/spark.png");
    @Override public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(180.0F - renderManager.playerViewY, 0, 1, 0);
        GL11.glRotatef(-renderManager.playerViewX, 1, 0, 0);
        bindTexture(TEXTURE);
        float s = 0.35F;
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setNormal(0, 1, 0);
        t.addVertexWithUV(-s, -s, 0, 0, 1); t.addVertexWithUV(s, -s, 0, 1, 1);
        t.addVertexWithUV(s, s, 0, 1, 0); t.addVertexWithUV(-s, s, 0, 0, 0);
        t.draw();
        GL11.glPopMatrix();
    }
    @Override protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}
