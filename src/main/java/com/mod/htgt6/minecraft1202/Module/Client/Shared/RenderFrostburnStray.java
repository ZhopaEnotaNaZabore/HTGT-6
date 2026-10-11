package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

/** 1.7-compatible renderer for the 1.10 Stray texture. */
public final class RenderFrostburnStray extends RenderBiped {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("htgt6", "textures/entity/frostburn/stray.png");
    private static final ResourceLocation OVERLAY =
            new ResourceLocation("htgt6", "textures/entity/frostburn/stray_overlay.png");
    private final ModelFrostburnStray baseModel;
    private final ModelFrostburnStray clothing = new ModelFrostburnStray(0.25F);

    public RenderFrostburnStray() {
        this(new ModelFrostburnStray(0.0F));
    }

    private RenderFrostburnStray(ModelFrostburnStray baseModel) {
        super(baseModel, 0.5F);
        this.baseModel = baseModel;
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TEXTURE;
    }

    @Override
    protected void renderModel(EntityLivingBase entity, float limbSwing, float limbAmount,
                               float age, float yaw, float pitch, float scale) {
        super.renderModel(entity, limbSwing, limbAmount, age, yaw, pitch, scale);
        if (entity.isInvisible()) return;
        bindTexture(OVERLAY);
        clothing.copyRenderStateFrom(baseModel);
        clothing.setLivingAnimations(entity, limbSwing, limbAmount, age);
        clothing.render(entity, limbSwing, limbAmount, age, yaw, pitch, scale);
    }
}
