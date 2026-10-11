package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/** Complete Minecraft 1.8 armor-stand geometry adapted to the 1.7 biped renderer. */
public final class ModelBountifulArmorStand extends ModelBiped {
    private final ModelRenderer rightBodySide;
    private final ModelRenderer leftBodySide;
    private final ModelRenderer waist;
    private final ModelRenderer basePlate;

    public ModelBountifulArmorStand() {
        super(0.0F, 0.0F, 64, 64);
        bipedHead = part(0, 0, -1.0F, -7.0F, -1.0F, 2, 7, 2, 0.0F, 1.0F, 0.0F);
        bipedBody = part(0, 26, -6.0F, 0.0F, -1.5F, 12, 3, 3, 0.0F, 0.0F, 0.0F);
        bipedRightArm = part(24, 0, -2.0F, -2.0F, -1.0F, 2, 12, 2, -5.0F, 2.0F, 0.0F);
        bipedLeftArm = part(32, 16, 0.0F, -2.0F, -1.0F, 2, 12, 2, 5.0F, 2.0F, 0.0F, true);
        bipedRightLeg = part(8, 0, -1.0F, 0.0F, -1.0F, 2, 11, 2, -1.9F, 12.0F, 0.0F);
        bipedLeftLeg = part(40, 16, -1.0F, 0.0F, -1.0F, 2, 11, 2, 1.9F, 12.0F, 0.0F, true);
        rightBodySide = part(16, 0, -3.0F, 3.0F, -1.0F, 2, 7, 2, 0.0F, 0.0F, 0.0F);
        leftBodySide = part(48, 16, 1.0F, 3.0F, -1.0F, 2, 7, 2, 0.0F, 0.0F, 0.0F);
        waist = part(0, 48, -4.0F, 10.0F, -1.0F, 8, 2, 2, 0.0F, 0.0F, 0.0F);
        basePlate = part(0, 32, -6.0F, 11.0F, -6.0F, 12, 1, 12, 0.0F, 12.0F, 0.0F);
        bipedHeadwear.showModel = false;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbAmount, float age, float yaw, float pitch,
            float scale) {
        setRotationAngles(limbSwing, limbAmount, age, yaw, pitch, scale, entity);
        EntityBountifulArmorStand stand = (EntityBountifulArmorStand) entity;
        bipedHead.render(scale);
        bipedBody.render(scale);
        if (stand.hasArms()) {
            bipedRightArm.render(scale);
            bipedLeftArm.render(scale);
        }
        bipedRightLeg.render(scale);
        bipedLeftLeg.render(scale);
        rightBodySide.render(scale);
        leftBodySide.render(scale);
        waist.render(scale);
        if (stand.hasBasePlate()) basePlate.render(scale);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbAmount, float age, float yaw, float pitch,
            float scale, Entity entity) {
        EntityBountifulArmorStand stand = (EntityBountifulArmorStand) entity;
        applyPose(bipedHead, stand.getPose(0));
        applyPose(bipedBody, stand.getPose(1));
        applyPose(bipedLeftArm, stand.getPose(2));
        applyPose(bipedRightArm, stand.getPose(3));
        applyPose(bipedLeftLeg, stand.getPose(4));
        applyPose(bipedRightLeg, stand.getPose(5));
        copyRotation(bipedBody, rightBodySide);
        copyRotation(bipedBody, leftBodySide);
        copyRotation(bipedBody, waist);
        rotate(basePlate, 0.0F, -entity.rotationYaw, 0.0F);
    }

    private ModelRenderer part(int u, int v, float x, float y, float z, int w, int h, int d,
            float px, float py, float pz) {
        return part(u,v,x,y,z,w,h,d,px,py,pz,false);
    }
    private ModelRenderer part(int u,int v,float x,float y,float z,int w,int h,int d,float px,float py,float pz,boolean mirror) {
        ModelRenderer model = new ModelRenderer(this, u, v);
        model.mirror = mirror;
        model.addBox(x, y, z, w, h, d);
        model.setRotationPoint(px, py, pz);
        return model;
    }

    private static void rotate(ModelRenderer part, float x, float y, float z) {
        float radians = (float) Math.PI / 180.0F;
        part.rotateAngleX = x * radians;
        part.rotateAngleY = y * radians;
        part.rotateAngleZ = z * radians;
    }

    private static void applyPose(ModelRenderer part, float[] pose) { rotate(part, pose[0], pose[1], pose[2]); }

    private static void copyRotation(ModelRenderer from, ModelRenderer to) {
        to.rotateAngleX = from.rotateAngleX;
        to.rotateAngleY = from.rotateAngleY;
        to.rotateAngleZ = from.rotateAngleZ;
    }
}
