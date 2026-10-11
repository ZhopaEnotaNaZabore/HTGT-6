package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/** 64x64 illager geometry; avoids applying the modern skin to legacy Steve UVs. */
public final class ModelPillager extends ModelBiped {
    private final ModelRenderer nose;
    private final ModelRenderer foldedArms;

    public ModelPillager() {
        super(0.0F, 0.0F, 64, 64);
        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHeadwear = new ModelRenderer(this, 32, 0);
        bipedHeadwear.addBox(-4.0F, -10.0F, -4.0F, 8, 12, 8, 0.45F);
        bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHeadwear.showModel = false; // IllagerModel hides the hat for pillagers.
        nose = new ModelRenderer(this, 24, 0);
        nose.addBox(-1.0F, -1.0F, -6.0F, 2, 4, 2);
        nose.setRotationPoint(0.0F, -2.0F, 0.0F);
        bipedHead.addChild(nose);
        bipedBody = new ModelRenderer(this, 16, 20);
        bipedBody.addBox(-4.0F, 0.0F, -3.0F, 8, 12, 6);
        bipedBody.setTextureOffset(0, 38).addBox(-4, 0, -3, 8, 20, 6, 0.5F);
        bipedRightArm = new ModelRenderer(this, 40, 46);
        bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedLeftArm = new ModelRenderer(this, 40, 46);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedRightLeg = new ModelRenderer(this, 0, 22);
        bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedLeftLeg = new ModelRenderer(this, 0, 22);
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.mirror = true;
        bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        foldedArms = new ModelRenderer(this, 44, 22);
        foldedArms.addBox(-8, -2, -2, 4, 8, 4);
        foldedArms.setTextureOffset(40, 38).addBox(-4, 2, -2, 8, 4, 4);
        foldedArms.setRotationPoint(0, 3, -1);
        foldedArms.rotateAngleX = -0.75F;
        ModelRenderer leftShoulder = new ModelRenderer(this, 44, 22);
        leftShoulder.mirror = true;
        leftShoulder.addBox(4, -2, -2, 4, 8, 4);
        foldedArms.addChild(leftShoulder);
    }

    @Override public void setRotationAngles(float limbSwing, float limbAmount, float age,
            float yaw, float pitch, float scale, Entity entity) {
        super.setRotationAngles(limbSwing, limbAmount, age, yaw, pitch, scale, entity);
        // Modern illagers have half the legacy biped leg stride.
        if (!isRiding) {
            bipedRightLeg.rotateAngleX *= 0.5F;
            bipedLeftLeg.rotateAngleX *= 0.5F;
        }
        net.minecraft.item.ItemStack held = entity instanceof net.minecraft.entity.EntityLivingBase
                ? ((net.minecraft.entity.EntityLivingBase)entity).getHeldItem() : null;
        boolean crossbow = held != null && held.getItem() instanceof
                com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
        int chargeTicks = entity instanceof com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager
                ? ((com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager)entity).getCrossbowChargeTicks() : 0;
        if (crossbow) applyCrossbowPose(chargeTicks, true, com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow.chargeDuration(held));
        // Pillager.getArmPose returns HOLD for any held crossbow, even unloaded.
        // Empty-handed pillagers are NEUTRAL, unlike the generic illager CROSSED pose.
        foldedArms.showModel = false;
        bipedRightArm.showModel = true;
        bipedLeftArm.showModel = true;
    }

    /** Right-handed AnimationUtils.animateCrossbowCharge/Hold from 1.20.2. */
    private boolean applyCrossbowPose(int ticks, boolean holdingCrossbow, int duration) {
        if (ticks <= 0 && !holdingCrossbow) return false;
        bipedRightArm.rotateAngleZ = bipedLeftArm.rotateAngleZ = 0;
        if (ticks > 0) {
            float progress = net.minecraft.util.MathHelper.clamp_float((float)ticks /
                    Math.max(1, duration), 0, 1);
            bipedRightArm.rotateAngleY = -0.8F;
            bipedRightArm.rotateAngleX = -0.97079635F;
            bipedLeftArm.rotateAngleY = 0.4F + progress * 0.45F;
            bipedLeftArm.rotateAngleX = -0.97079635F + progress * (-1.5707964F + 0.97079635F);
        } else {
            bipedRightArm.rotateAngleY = -0.3F + bipedHead.rotateAngleY;
            bipedLeftArm.rotateAngleY = 0.6F + bipedHead.rotateAngleY;
            bipedRightArm.rotateAngleX = -(float)Math.PI / 2 + bipedHead.rotateAngleX + 0.1F;
            bipedLeftArm.rotateAngleX = -1.5F + bipedHead.rotateAngleX;
        }
        return true;
    }

    @Override public void render(Entity entity, float limbSwing, float limbAmount, float age,
            float yaw, float pitch, float scale) {
        super.render(entity, limbSwing, limbAmount, age, yaw, pitch, scale);
        // Nose is a head child; rendering it again here would duplicate it and lose the pivot.
        foldedArms.render(scale);
    }
}
