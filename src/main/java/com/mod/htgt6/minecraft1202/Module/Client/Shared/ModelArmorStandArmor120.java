package com.mod.htgt6.minecraft1202.Module.Client.Shared;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulArmorStand;

/** Modern armor-stand armor poses on vanilla 1.7 armor render passes. */
public final class ModelArmorStandArmor120 extends ModelBiped {
    public ModelArmorStandArmor120(float inflation) {
        super(inflation);
        bipedHead.setRotationPoint(0,1,0); bipedHeadwear.setRotationPoint(0,1,0);
        bipedRightLeg=new ModelRenderer(this,0,16);
        bipedRightLeg.addBox(-2,0,-2,4,12,4,inflation-.1F);
        bipedRightLeg.setRotationPoint(-1.9F,11,0);
        bipedLeftLeg=new ModelRenderer(this,0,16);bipedLeftLeg.mirror=true;
        bipedLeftLeg.addBox(-2,0,-2,4,12,4,inflation-.1F);
        bipedLeftLeg.setRotationPoint(1.9F,11,0);
    }
    @Override public void setRotationAngles(float swing,float amount,float age,float yaw,float pitch,float scale,Entity entity) {
        EntityBountifulArmorStand stand=(EntityBountifulArmorStand)entity;
        ModelRenderer[] parts={bipedHead,bipedBody,bipedLeftArm,bipedRightArm,bipedLeftLeg,bipedRightLeg};
        for(int i=0;i<parts.length;i++) {
            float[] pose=stand.getPose(i);float radians=(float)Math.PI/180;
            parts[i].rotateAngleX=pose[0]*radians;parts[i].rotateAngleY=pose[1]*radians;parts[i].rotateAngleZ=pose[2]*radians;
        }
        bipedHeadwear.rotateAngleX=bipedHead.rotateAngleX;
        bipedHeadwear.rotateAngleY=bipedHead.rotateAngleY;
        bipedHeadwear.rotateAngleZ=bipedHead.rotateAngleZ;
    }
}
