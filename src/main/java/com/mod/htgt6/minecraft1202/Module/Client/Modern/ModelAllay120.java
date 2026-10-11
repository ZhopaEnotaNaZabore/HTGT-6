package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAllay120;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

/** 32x32 Allay mesh and flying/dancing animation adapted to ModelRenderer. */
public final class ModelAllay120 extends ModelBase {
    public final ModelRenderer body,head,rightArm,leftArm,rightWing,leftWing;
    public ModelAllay120(){
        textureWidth=32;textureHeight=32;
        body=new ModelRenderer(this,0,6);body.addBox(-1.5F,0.0F,-1.0F,3,4,2);body.setRotationPoint(0,16,0);
        head=new ModelRenderer(this,0,0);head.addBox(-2.5F,-5.0F,-2.5F,5,5,5);body.addChild(head);
        rightArm=new ModelRenderer(this,23,0);rightArm.addBox(-1.0F,0.0F,-1.0F,1,5,2);rightArm.setRotationPoint(-1.5F,.5F,0);body.addChild(rightArm);
        leftArm=new ModelRenderer(this,23,0);leftArm.mirror=true;leftArm.addBox(0.0F,0.0F,-1.0F,1,5,2);leftArm.setRotationPoint(1.5F,.5F,0);body.addChild(leftArm);
        rightWing=new ModelRenderer(this,16,14);rightWing.addBox(0.0F,0.0F,0.0F,0,5,8);rightWing.setRotationPoint(-.5F,1.0F,.6F);body.addChild(rightWing);
        leftWing=new ModelRenderer(this,16,14);leftWing.mirror=true;leftWing.addBox(0.0F,0.0F,0.0F,0,5,8);leftWing.setRotationPoint(.5F,1.0F,.6F);body.addChild(leftWing);
    }
    @Override public void setRotationAngles(float limb,float amount,float age,float yaw,float pitch,float scale,Entity raw){
        EntityAllay120 allay=(EntityAllay120)raw;float flap=age*1.4F;
        head.rotateAngleY=yaw*.017453292F;head.rotateAngleX=pitch*.017453292F;
        rightWing.rotateAngleY=.45F+.55F*MathHelper.cos(flap);leftWing.rotateAngleY=-rightWing.rotateAngleY;
        rightWing.rotateAngleX=.35F;leftWing.rotateAngleX=.35F;
        boolean holding=allay.getCarriedStack()!=null;rightArm.rotateAngleX=leftArm.rotateAngleX=holding?-1.15F:-.25F;
        rightArm.rotateAngleZ=.15F;leftArm.rotateAngleZ=-.15F;
        if(allay.isDancing()){body.rotateAngleZ=.25F*MathHelper.sin(age*.35F);head.rotateAngleZ=-body.rotateAngleZ;
            rightArm.rotateAngleZ=.7F+.25F*MathHelper.sin(age*.5F);leftArm.rotateAngleZ=-rightArm.rotateAngleZ;}
        else{body.rotateAngleZ=0;head.rotateAngleZ=0;}
        body.rotationPointY=16.0F+MathHelper.sin(age*.2F)*.35F;
    }
    @Override public void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale){
        setRotationAngles(limb,amount,age,yaw,pitch,scale,entity);body.render(scale);}
}
