package com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.block.material.Material;

/** Server-safe hooks: no client classes and no synthetic vanilla potion effects. */
public final class ConduitPowerHooks120 {
    private ConduitPowerHooks120() {}
    public static boolean breathing(EntityLivingBase entity,int id){return entity.isPotionActive(id)||AquaticEffects120.hasConduitPower(entity);}
    public static boolean vision(EntityLivingBase entity,Potion potion){return entity.isPotionActive(potion)||underwaterConduit(entity);}
    public static boolean conduitVisionOnly(EntityLivingBase entity){return !entity.isPotionActive(Potion.nightVision)&&underwaterConduit(entity);}
    private static boolean underwaterConduit(EntityLivingBase entity){return entity.isInsideOfMaterial(Material.water)&&AquaticEffects120.hasConduitPower(entity);}
}
