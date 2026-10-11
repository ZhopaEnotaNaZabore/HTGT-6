package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
public final class RenderHoglin116 extends RenderLiving {
    private final ResourceLocation texture;
    public RenderHoglin116(boolean zombie){super(new ModelHoglin116(),.7F);texture=new ResourceLocation("htgt6","textures/entity/nether/"+(zombie?"zoglin":"hoglin")+".png");}
    @Override protected ResourceLocation getEntityTexture(Entity e){return texture;}
}
