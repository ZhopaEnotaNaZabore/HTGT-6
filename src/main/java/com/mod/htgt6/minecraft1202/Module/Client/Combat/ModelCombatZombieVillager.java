package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatZombieVillager;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

/** Minecraft 1.12 zombie-villager geometry and UV layout. */
public final class ModelCombatZombieVillager extends ModelBiped {
    public ModelCombatZombieVillager() { this(0.0F, 0.0F, false); }

    public ModelCombatZombieVillager(float scale, float yOffset, boolean armor) {
        super(scale, 0.0F, 64, armor ? 32 : 64);
        if (armor) {
            bipedHead = new ModelRenderer(this, 0, 0);
            bipedHead.addBox(-4.0F, -10.0F, -4.0F, 8, 8, 8, scale);
            bipedHead.setRotationPoint(0.0F, yOffset, 0.0F);
            bipedBody = new ModelRenderer(this, 16, 16);
            bipedBody.setRotationPoint(0.0F, yOffset, 0.0F);
            bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, scale + 0.1F);
            bipedRightLeg = new ModelRenderer(this, 0, 16);
            bipedRightLeg.setRotationPoint(-2.0F, 12.0F + yOffset, 0.0F);
            bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, scale + 0.1F);
            bipedLeftLeg = new ModelRenderer(this, 0, 16);
            bipedLeftLeg.mirror = true;
            bipedLeftLeg.setRotationPoint(2.0F, 12.0F + yOffset, 0.0F);
            bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, scale + 0.1F);
            return;
        }

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.setRotationPoint(0.0F, yOffset, 0.0F);
        bipedHead.addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, scale);
        bipedHead.setTextureOffset(24, 0).addBox(-1.0F, -3.0F, -6.0F, 2, 4, 2, scale);
        bipedBody = new ModelRenderer(this, 16, 20);
        bipedBody.setRotationPoint(0.0F, yOffset, 0.0F);
        bipedBody.addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6, scale);
        bipedBody.setTextureOffset(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8, 18, 6, scale + 0.05F);
        bipedRightArm = new ModelRenderer(this, 44, 38);
        bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, scale);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F + yOffset, 0.0F);
        bipedLeftArm = new ModelRenderer(this, 44, 38);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, scale);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F + yOffset, 0.0F);
        bipedRightLeg = new ModelRenderer(this, 0, 22);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F + yOffset, 0.0F);
        bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, scale);
        bipedLeftLeg = new ModelRenderer(this, 0, 22);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F + yOffset, 0.0F);
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, scale);
    }

    @Override public void setRotationAngles(float limbSwing, float limbAmount, float age,
            float yaw, float pitch, float unit, Entity entity) {
        super.setRotationAngles(limbSwing, limbAmount, age, yaw, pitch, unit, entity);
        float attack = MathHelper.sin(onGround * (float) Math.PI);
        float attackEase = MathHelper.sin((1.0F - (1.0F - onGround) * (1.0F - onGround))
                * (float) Math.PI);
        bipedRightArm.rotateAngleZ = 0.0F;
        bipedLeftArm.rotateAngleZ = 0.0F;
        bipedRightArm.rotateAngleY = -(0.1F - attack * 0.6F);
        bipedLeftArm.rotateAngleY = 0.1F - attack * 0.6F;
        boolean raised = entity instanceof EntityCombatZombieVillager
                && ((EntityCombatZombieVillager) entity).getAttackTarget() != null;
        float armAngle = -(float) Math.PI / (raised ? 1.5F : 2.25F);
        bipedRightArm.rotateAngleX = armAngle + attack * 1.2F - attackEase * 0.4F;
        bipedLeftArm.rotateAngleX = armAngle + attack * 1.2F - attackEase * 0.4F;
        bipedRightArm.rotateAngleZ += MathHelper.cos(age * 0.09F) * 0.05F + 0.05F;
        bipedLeftArm.rotateAngleZ -= MathHelper.cos(age * 0.09F) * 0.05F + 0.05F;
        bipedRightArm.rotateAngleX += MathHelper.sin(age * 0.067F) * 0.05F;
        bipedLeftArm.rotateAngleX -= MathHelper.sin(age * 0.067F) * 0.05F;
    }
}
