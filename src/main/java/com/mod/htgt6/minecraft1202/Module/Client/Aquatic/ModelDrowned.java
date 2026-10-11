package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDrowned;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.entity.Entity;

/** Zombie geometry with the 1.20.2 THROW_SPEAR right-arm pose. */
public final class ModelDrowned extends ModelZombie {
    public ModelDrowned() { super(); }
    public ModelDrowned(float expansion) { super(expansion, true); }

    @Override
    public void setRotationAngles(float limb, float amount, float age, float yaw, float pitch,
                                  float scale, Entity entity) {
        super.setRotationAngles(limb, amount, age, yaw, pitch, scale, entity);
        if (entity instanceof EntityDrowned && ((EntityDrowned)entity).isChargingTrident()) {
            bipedRightArm.rotateAngleX = bipedHead.rotateAngleX - (float)Math.PI;
            bipedRightArm.rotateAngleY = 0.0F;
            bipedRightArm.rotateAngleZ = 0.0F;
        }
    }
}
