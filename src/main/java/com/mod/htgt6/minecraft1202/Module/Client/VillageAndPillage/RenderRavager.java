package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;

import com.mod.htgt6.HTGT6;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderRavager extends RenderLiving {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            HTGT6.MOD_ID.toLowerCase(), "textures/entity/village_pillage/ravager.png");

    public RenderRavager() { super(new ModelRavager(), 1.1F); }
    @Override protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}
