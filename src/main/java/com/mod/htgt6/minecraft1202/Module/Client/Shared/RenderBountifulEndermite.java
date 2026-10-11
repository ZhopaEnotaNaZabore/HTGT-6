package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderBountifulEndermite extends RenderLiving {
    private static final ResourceLocation TEXTURE=new ResourceLocation("htgt6","textures/entity/bountiful/endermite.png");
    public RenderBountifulEndermite(){super(new ModelBountifulEndermite(),0.2F);}
    @Override protected ResourceLocation getEntityTexture(Entity entity){return TEXTURE;}
}
