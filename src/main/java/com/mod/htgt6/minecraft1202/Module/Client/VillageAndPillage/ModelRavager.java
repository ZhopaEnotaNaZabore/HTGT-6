package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

/** Minecraft 1.20.2 RavagerModel mesh adapted to the 1.7.10 ModelRenderer tree. */
public final class ModelRavager extends ModelBase {
    private final ModelRenderer neck;
    private final ModelRenderer head;
    private final ModelRenderer mouth;
    private final ModelRenderer body;
    // Right hind, left hind, right front, left front: the reference animation order.
    private final ModelRenderer[] legs = new ModelRenderer[4];

    public ModelRavager() {
        textureWidth = 128; textureHeight = 128;
        neck = new ModelRenderer(this, 68, 73);
        neck.addBox(-5, -1, -18, 10, 10, 18);
        neck.setRotationPoint(0, -7, 5.5F);
        head = new ModelRenderer(this, 0, 0);
        head.addBox(-8, -20, -14, 16, 20, 16);
        head.addBox(-2, -6, -18, 4, 8, 4);
        head.setRotationPoint(0, 16, -17);
        neck.addChild(head);
        for (int i = 0; i < 2; i++) {
            ModelRenderer horn = new ModelRenderer(this, 74, 55);
            horn.mirror = i == 1;
            horn.addBox(0, -14, -2, 2, 14, 4);
            horn.setRotationPoint(i == 0 ? -10 : 8, -14, -8);
            horn.rotateAngleX = 1.0995574F;
            head.addChild(horn);
        }
        mouth = new ModelRenderer(this, 0, 36);
        mouth.addBox(-8, 0, -16, 16, 3, 16);
        mouth.setRotationPoint(0, -2, 2);
        head.addChild(mouth);
        body = new ModelRenderer(this, 0, 55);
        body.addBox(-7, -10, -7, 14, 16, 20);
        body.setTextureOffset(0, 91).addBox(-6, 6, -7, 12, 13, 18);
        body.setRotationPoint(0, 1, 2);
        body.rotateAngleX = (float)Math.PI / 2.0F;
        for (int i = 0; i < 4; i++) {
            legs[i] = new ModelRenderer(this, i < 2 ? 96 : 64, 0);
            legs[i].mirror = i % 2 == 1;
            legs[i].addBox(-4, 0, -4, 8, 37, 8);
            legs[i].setRotationPoint(i % 2 == 0 ? -8 : 8, -13, i < 2 ? 18 : -5);
        }
    }

    @Override public void setRotationAngles(float limbSwing, float limbAmount, float age,
            float yaw, float pitch, float scale, Entity entity) {
        head.rotateAngleY = yaw * ((float)Math.PI / 180.0F);
        head.rotateAngleX = pitch * ((float)Math.PI / 180.0F);
        float first = MathHelper.cos(limbSwing * 0.6662F) * 0.4F * limbAmount;
        float second = MathHelper.cos(limbSwing * 0.6662F + (float)Math.PI) * 0.4F * limbAmount;
        legs[0].rotateAngleX = first; legs[1].rotateAngleX = second;
        legs[2].rotateAngleX = second; legs[3].rotateAngleX = first;
    }

    @Override public void setLivingAnimations(net.minecraft.entity.EntityLivingBase entity,
            float limbSwing, float limbAmount, float partialTicks) {
        int attack = 0, stun = 0, roar = 0;
        if (entity instanceof com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRavager) {
            com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRavager ravager =
                    (com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRavager)entity;
            attack = ravager.getAttackTick(); stun = ravager.getStunnedTick(); roar = ravager.getRoarTick();
        }
        animateCombat(attack, stun, roar, MathHelper.clamp_float(partialTicks, 0, 1));
    }

    private void animateCombat(int attack, int stun, int roar, float partialTicks) {
        neck.rotationPointX = 0;
        // Reset every render: a single model instance is shared by all ravagers.
        neck.rotateAngleX = stun > 0 ? .21991149F : 0;
        if (attack > 0) {
            float ticks = attack - partialTicks;
            float triangle = (Math.abs(ticks % 10.0F - 5.0F) - 2.5F) / 2.5F;
            float progress = (1 + triangle) * .5F;
            float extension = progress * progress * progress * 12;
            neck.rotationPointZ = -6.5F + extension;
            neck.rotationPointY = -7 - extension * MathHelper.sin(neck.rotateAngleX);
            mouth.rotateAngleX = attack > 5
                    ? MathHelper.sin((-4 + attack - partialTicks) / 4.0F) * (float)Math.PI * .4F
                    : .15707964F * MathHelper.sin((float)Math.PI * ticks / 10);
        } else {
            neck.rotationPointY = -7 + MathHelper.sin(neck.rotateAngleX);
            neck.rotationPointZ = 5.5F;
            mouth.rotateAngleX = (float)Math.PI * (stun > 0 ? .05F : .01F);
            if (stun > 0) neck.rotationPointX = (float)Math.sin(stun / 40.0D * 10.0D) * 3;
            else if (roar > 0) mouth.rotateAngleX = (float)Math.PI / 2 * MathHelper.sin(
                    (20 - roar - partialTicks) / 20 * (float)Math.PI * .25F);
        }
    }

    @Override public void render(Entity entity, float limbSwing, float limbAmount, float age,
            float yaw, float pitch, float scale) {
        setRotationAngles(limbSwing, limbAmount, age, yaw, pitch, scale, entity);
        neck.render(scale); body.render(scale);
        for (ModelRenderer leg : legs) leg.render(scale);
    }
}
