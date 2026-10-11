package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/** 1.7-compatible renderer for the 1.10 Husk texture. */
public final class RenderFrostburnHusk extends RenderBiped {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("htgt6", "textures/entity/frostburn/husk.png");

    public RenderFrostburnHusk() {
        super(new ModelZombie(), 0.5F);
    }

    @Override protected void preRenderCallback(net.minecraft.entity.EntityLivingBase entity,float partialTick) {
        org.lwjgl.opengl.GL11.glScalef(1.0625F,1.0625F,1.0625F);
        super.preRenderCallback(entity,partialTick);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TEXTURE;
    }
}
