package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import net.minecraft.client.renderer.entity.RenderSquid;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderGlowSquid120 extends RenderSquid {
    private static final ResourceLocation TEXTURE=new ResourceLocation("htgt6","textures/entity/squid/glow_squid.png");
    public RenderGlowSquid120(){super(new ModelGlowSquid120(),0.7F);}
    @Override protected ResourceLocation getEntityTexture(Entity entity){return TEXTURE;}
}
