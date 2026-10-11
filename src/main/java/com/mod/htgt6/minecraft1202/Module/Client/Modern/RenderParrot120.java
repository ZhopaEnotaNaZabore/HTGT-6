package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityParrot120;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderParrot120 extends RenderLiving {
    private static final ResourceLocation[] TEXTURES={
            texture("red_blue"),texture("blue"),texture("green"),texture("yellow_blue"),texture("grey")};
    public RenderParrot120(){super(new ModelParrot120(),.3F);}
    @Override protected float handleRotationFloat(net.minecraft.entity.EntityLivingBase entity,float partial){
        return ((EntityParrot120)entity).flapAmount(partial);
    }
    @Override protected ResourceLocation getEntityTexture(Entity raw){
        return TEXTURES[((EntityParrot120)raw).getVariant()];
    }
    private static ResourceLocation texture(String variant){
        return new ResourceLocation("htgt6","textures/entity/parrot/parrot_"+variant+".png");
    }
}
