package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderFrostburnPolarBear extends RenderLiving {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("htgt6", "textures/entity/frostburn/polar_bear.png");

    public RenderFrostburnPolarBear() {
        super(new ModelFrostburnPolarBear(), 0.7F);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TEXTURE;
    }
}
