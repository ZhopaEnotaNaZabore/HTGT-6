package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderGoat120 extends RenderLiving {
    private static final ResourceLocation TEXTURE = new ResourceLocation("htgt6", "textures/entity/goat/goat.png");
    public RenderGoat120() { super(new ModelGoat120(), 0.5F); }
    @Override protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}
