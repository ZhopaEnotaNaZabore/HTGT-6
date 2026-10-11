package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatZombieVillager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

/** Minecraft 1.12 profession textures for the separate zombie-villager entity. */
public final class RenderCombatZombieVillager extends RenderBiped {
    private static final String[] TEXTURES = {
        "zombie_farmer.png", "zombie_librarian.png", "zombie_priest.png",
        "zombie_smith.png", "zombie_butcher.png", "zombie_villager.png"
    };

    public RenderCombatZombieVillager() {
        super(new ModelCombatZombieVillager(), 0.5F);
    }

    @Override protected void func_82421_b() {
        field_82423_g = new ModelCombatZombieVillager(1.0F, 0.0F, true);
        field_82425_h = new ModelCombatZombieVillager(0.5F, 0.0F, true);
    }

    @Override protected ResourceLocation getEntityTexture(EntityLiving entity) {
        int profession = ((EntityCombatZombieVillager) entity).getProfession();
        if (profession < 0 || profession >= TEXTURES.length) profession = 0;
        return new ResourceLocation("htgt6",
                "textures/entity/combat_update/zombie_villager/" + TEXTURES[profession]);
    }

    @Override protected void rotateCorpse(EntityLivingBase entity, float age, float yaw, float partialTicks) {
        if (((EntityCombatZombieVillager) entity).isConverting()) {
            yaw += (float) (Math.cos(entity.ticksExisted * 3.25D) * Math.PI * 0.25D);
        }
        super.rotateCorpse(entity, age, yaw, partialTicks);
    }
}
