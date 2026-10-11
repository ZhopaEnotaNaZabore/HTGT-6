package com.mod.htgt6.minecraft1202.Module.Gameplay.Combat;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public final class PotionLevitation extends Potion {
    public PotionLevitation(int id) { super(id, true, 0xCEFFFF); setPotionName("potion.levitation"); }
    @Override public boolean isReady(int duration, int amplifier) { return true; }
    @Override public void performEffect(EntityLivingBase entity, int amplifier) {
        entity.fallDistance = 0.0F;
    }
    /** Replaces normal air gravity after movement, before drag, as in LivingEntity.travel. */
    public static double gravityAdjustment(double gravity, EntityLivingBase entity) {
        for(Object value:entity.getActivePotionEffects()) {
            PotionEffect effect=(PotionEffect)value;
            int id=effect.getPotionID();
            if(id>=0&&id<Potion.potionTypes.length&&Potion.potionTypes[id] instanceof PotionLevitation) {
                entity.fallDistance=0;
                return -(0.05D*(effect.getAmplifier()+1)-entity.motionY)*0.2D;
            }
        }
        return gravity;
    }
}
