package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderCamel120 extends RenderLiving {
    private static final ResourceLocation TEXTURE=new ResourceLocation("htgt6","textures/entity/camel/camel.png");
    public RenderCamel120(){super(new ModelCamel120(),.7F);}
    @Override protected ResourceLocation getEntityTexture(Entity entity){return TEXTURE;}
}
