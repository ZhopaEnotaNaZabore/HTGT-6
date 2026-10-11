package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
public final class RenderDolphin120 extends RenderLiving {private static final ResourceLocation T=new ResourceLocation("htgt6","textures/entity/dolphin.png");public RenderDolphin120(){super(new ModelDolphin120(),.7F);}@Override protected ResourceLocation getEntityTexture(Entity e){return T;}}
