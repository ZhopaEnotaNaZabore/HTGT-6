package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatArrow;
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ResourceLocation;

public final class RenderCombatArrow extends RenderArrow {
    private static final ResourceLocation TIPPED = new ResourceLocation("htgt6",
            "textures/entity/combat_update/projectiles/tipped_arrow.png");
    private static final ResourceLocation SPECTRAL = new ResourceLocation("htgt6",
            "textures/entity/combat_update/projectiles/spectral_arrow.png");
    private static final ResourceLocation NORMAL = new ResourceLocation("textures/entity/arrow.png");
    @Override protected ResourceLocation getEntityTexture(EntityArrow entity) {
        if(entity instanceof EntityCombatArrow && ((EntityCombatArrow)entity).getPotionColor()==-1
                && ((EntityCombatArrow)entity).getCombatType()!=EntityCombatArrow.TYPE_SPECTRAL)return NORMAL;
        return entity instanceof EntityCombatArrow
                && ((EntityCombatArrow) entity).getCombatType() == EntityCombatArrow.TYPE_SPECTRAL
                ? SPECTRAL : TIPPED;
    }
}
