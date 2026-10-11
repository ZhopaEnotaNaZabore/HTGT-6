package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelVillager;
import net.minecraft.entity.Entity;

/** 64x64 Village & Pillage villager geometry implemented on the 1.7 model API. */
public final class ModelVillager120 extends ModelVillager {
    public final ModelRenderer headwear;
    public final ModelRenderer hatRim;

    public ModelVillager120(float scale) {
        super(scale, 0.0F, 64, 64);
        // Legacy ModelVillager sizes its own renderers but leaves ModelBase at
        // 64x32. Set the defaults BEFORE addBox bakes UVs for replacement parts.
        textureWidth = 64;
        textureHeight = 64;
        villagerBody = new ModelRenderer(this,16,20);
        villagerBody.addBox(-4,0,-3,8,12,6,scale);
        villagerBody.setTextureOffset(0,38).addBox(-4,0,-3,8,20,6,scale+.5F);
        villagerArms = new ModelRenderer(this,44,22);
        villagerArms.addBox(-8,-2,-2,4,8,4,scale);
        ModelRenderer left=new ModelRenderer(this,44,22);left.mirror=true;
        left.addBox(4,-2,-2,4,8,4,scale);villagerArms.addChild(left);
        villagerArms.setTextureOffset(40,38).addBox(-4,2,-2,8,4,4,scale);
        headwear = new ModelRenderer(this).setTextureSize(64, 64);
        headwear.setTextureOffset(32, 0).addBox(-4.0F, -10.0F, -4.0F,
                8, 10, 8, scale + 0.51F);
        headwear.setRotationPoint(0.0F, 0.0F, 0.0F);
        hatRim = new ModelRenderer(this).setTextureSize(64, 64);
        hatRim.setTextureOffset(30,47).addBox(-8,-8,-6,16,16,1);
        hatRim.rotateAngleX=-(float)Math.PI/2;
        headwear.addChild(hatRim);
    }

    @Override public void setRotationAngles(float limbSwing, float limbAmount, float age,
            float yaw, float pitch, float scale, Entity entity) {
        super.setRotationAngles(limbSwing, limbAmount, age, yaw, pitch, scale, entity);
        villagerHead.rotateAngleZ=0;
        if(entity instanceof net.minecraft.entity.passive.EntityVillager&&
                com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112.getUnhappy((net.minecraft.entity.passive.EntityVillager)entity)>0){
            villagerHead.rotateAngleZ=.3F*net.minecraft.util.MathHelper.sin(.45F*age);
            villagerHead.rotateAngleX=.4F;
        }
        headwear.rotateAngleZ=villagerHead.rotateAngleZ;
        headwear.rotateAngleX = villagerHead.rotateAngleX;
        headwear.rotateAngleY = villagerHead.rotateAngleY;
    }

    @Override public void render(Entity entity, float limbSwing, float limbAmount, float age,
            float yaw, float pitch, float scale) {
        super.render(entity, limbSwing, limbAmount, age, yaw, pitch, scale);
        headwear.render(scale);
    }
    public void setHeadVisible(boolean visible){villagerHead.showModel=visible;headwear.showModel=visible;}
}
