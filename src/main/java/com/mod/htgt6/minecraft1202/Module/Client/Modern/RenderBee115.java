package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import net.minecraft.client.renderer.entity.RenderLiving;import net.minecraft.entity.Entity;import net.minecraft.util.ResourceLocation;import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityBee115;
public final class RenderBee115 extends RenderLiving {public RenderBee115(){super(new ModelBee115(),.4F);}protected ResourceLocation getEntityTexture(Entity entity){EntityBee115 b=(EntityBee115)entity;return new ResourceLocation("htgt6","textures/entity/bee/bee"+(b.angry()?"_angry":"")+(b.hasNectar()?"_nectar":"")+".png");}}
