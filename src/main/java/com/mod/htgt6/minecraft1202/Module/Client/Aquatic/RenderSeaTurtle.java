package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import com.mod.htgt6.HTGT6;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderSeaTurtle extends RenderLiving {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HTGT6.MOD_ID.toLowerCase(),
            "textures/entity/turtle/big_sea_turtle.png");
    public RenderSeaTurtle() { super(new ModelSeaTurtle(), 0.7F); }
    @Override protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}
