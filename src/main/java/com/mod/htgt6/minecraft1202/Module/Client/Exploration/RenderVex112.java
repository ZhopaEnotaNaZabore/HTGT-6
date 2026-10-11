package com.mod.htgt6.minecraft1202.Module.Client.Exploration;

import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVex112;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderVex112 extends RenderBiped {
    private static final ResourceLocation NORMAL = new ResourceLocation("htgt6", "textures/entity/exploration_update/illager/vex.png");
    private static final ResourceLocation CHARGING = new ResourceLocation("htgt6", "textures/entity/exploration_update/illager/vex_charging.png");
    public RenderVex112() { super(new ModelVex112(), 0.3F); }
    @Override protected void preRenderCallback(net.minecraft.entity.EntityLivingBase entity, float partial) {
        org.lwjgl.opengl.GL11.glScalef(0.4F, 0.4F, 0.4F);
    }
    @Override protected ResourceLocation getEntityTexture(Entity entity) {
        return ((EntityVex112)entity).isCharging() ? CHARGING : NORMAL;
    }
}
