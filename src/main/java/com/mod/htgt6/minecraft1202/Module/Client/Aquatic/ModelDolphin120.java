package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/** Minecraft 1.20.2 dolphin mesh and swimming animation, expressed with 1.7 ModelRenderer parts. */
public final class ModelDolphin120 extends ModelBase {
    private static final float DEG_TO_RAD = 0.017453292F;
    private static final float TAIL_REST_X = -0.10471976F;

    private final ModelRenderer body;
    private final ModelRenderer backFin;
    private final ModelRenderer leftFin;
    private final ModelRenderer rightFin;
    private final ModelRenderer tail;
    private final ModelRenderer tailFin;
    private final ModelRenderer head;
    private final ModelRenderer nose;

    public ModelDolphin120() {
        textureWidth = 64;
        textureHeight = 64;

        body = new ModelRenderer(this, 22, 0);
        body.setRotationPoint(0.0F, 22.0F, -5.0F);
        body.addBox(-4.0F, -7.0F, 0.0F, 8, 7, 13);

        backFin = new ModelRenderer(this, 51, 0);
        backFin.addBox(-0.5F, 0.0F, 8.0F, 1, 4, 5);
        backFin.rotateAngleX = 1.0471976F;
        body.addChild(backFin);

        leftFin = new ModelRenderer(this, 48, 20);
        leftFin.mirror = true;
        leftFin.setRotationPoint(2.0F, -2.0F, 4.0F);
        leftFin.addBox(-0.5F, -4.0F, 0.0F, 1, 4, 7);
        leftFin.rotateAngleX = 1.0471976F;
        leftFin.rotateAngleZ = 2.0943952F;
        body.addChild(leftFin);

        rightFin = new ModelRenderer(this, 48, 20);
        rightFin.setRotationPoint(-2.0F, -2.0F, 4.0F);
        rightFin.addBox(-0.5F, -4.0F, 0.0F, 1, 4, 7);
        rightFin.rotateAngleX = 1.0471976F;
        rightFin.rotateAngleZ = -2.0943952F;
        body.addChild(rightFin);

        tail = new ModelRenderer(this, 0, 19);
        tail.setRotationPoint(0.0F, -2.5F, 11.0F);
        tail.addBox(-2.0F, -2.5F, 0.0F, 4, 5, 11);
        tail.rotateAngleX = TAIL_REST_X;
        body.addChild(tail);

        tailFin = new ModelRenderer(this, 19, 20);
        tailFin.setRotationPoint(0.0F, 0.0F, 9.0F);
        tailFin.addBox(-5.0F, -0.5F, 0.0F, 10, 1, 6);
        tail.addChild(tailFin);

        head = new ModelRenderer(this, 0, 0);
        head.setRotationPoint(0.0F, -4.0F, -3.0F);
        head.addBox(-4.0F, -3.0F, -3.0F, 8, 7, 6);
        body.addChild(head);

        nose = new ModelRenderer(this, 0, 13);
        nose.addBox(-1.0F, 2.0F, -7.0F, 2, 2, 4);
        head.addChild(nose);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        body.rotateAngleX = headPitch * DEG_TO_RAD;
        body.rotateAngleY = netHeadYaw * DEG_TO_RAD;
        tail.rotateAngleX = TAIL_REST_X;
        tailFin.rotateAngleX = 0.0F;

        double speedSquared = entity.motionX * entity.motionX
                + entity.motionY * entity.motionY
                + entity.motionZ * entity.motionZ;
        if (speedSquared > 1.0E-7D) {
            float wave = (float)Math.cos(ageInTicks * 0.3F);
            body.rotateAngleX += -0.05F - 0.05F * wave;
            tail.rotateAngleX = -0.1F * wave;
            tailFin.rotateAngleX = -0.2F * wave;
        }
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scaleFactor) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
        body.render(scaleFactor);
    }
}
