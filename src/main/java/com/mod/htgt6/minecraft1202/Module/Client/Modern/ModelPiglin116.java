package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import net.minecraft.client.model.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPiglin116;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityZombifiedPiglin116;

/** Official 64x64 piglin geometry, including snout, tusks, ears and left limb UVs. */
public final class ModelPiglin116 extends ModelBiped {
    public final ModelRenderer leftEar,rightEar;
    public ModelPiglin116(){super(0,0,64,64);textureWidth=textureHeight=64;
        bipedHead=part(0,0,-5,-8,-4,10,8,8,0,0,0);
        bipedHead.addChild(part(31,1,-2,-4,-5,4,4,1,0,0,0));
        bipedHead.addChild(part(2,4,2,-2,-5,1,2,1,0,0,0));bipedHead.addChild(part(2,0,-3,-2,-5,1,2,1,0,0,0));
        leftEar=part(51,6,0,0,-2,1,5,4,4.5F,-6,0);rightEar=part(39,6,-1,0,-2,1,5,4,-4.5F,-6,0);bipedHead.addChild(leftEar);bipedHead.addChild(rightEar);
        bipedHeadwear=new ModelRenderer(this);bipedBody=part(16,16,-4,0,-2,8,12,4,0,0,0);
        bipedRightArm=part(40,16,-3,-2,-2,4,12,4,-5,2,0);bipedLeftArm=part(32,48,-1,-2,-2,4,12,4,5,2,0);
        bipedRightLeg=part(0,16,-2,0,-2,4,12,4,-1.9F,12,0);bipedLeftLeg=part(16,48,-2,0,-2,4,12,4,1.9F,12,0);
        overlay(bipedBody,16,32,-4,0,-2,8,12,4);
        overlay(bipedRightArm,40,32,-3,-2,-2,4,12,4);overlay(bipedLeftArm,48,48,-1,-2,-2,4,12,4);
        overlay(bipedRightLeg,0,32,-2,0,-2,4,12,4);overlay(bipedLeftLeg,0,48,-2,0,-2,4,12,4);
    }
    private void overlay(ModelRenderer parent,int u,int v,float x,float y,float z,int w,int h,int d){ModelRenderer p=new ModelRenderer(this,u,v);p.addBox(x,y,z,w,h,d,.25F);parent.addChild(p);}
    private ModelRenderer part(int u,int v,float x,float y,float z,int w,int h,int d,float px,float py,float pz){ModelRenderer p=new ModelRenderer(this,u,v);p.addBox(x,y,z,w,h,d);p.setRotationPoint(px,py,pz);return p;}
    @Override public void setRotationAngles(float walk,float amount,float age,float yaw,float pitch,float scale,Entity e){
        bipedHead.setRotationPoint(0,0,0);bipedBody.rotationPointY=0;bipedRightArm.rotationPointY=bipedLeftArm.rotationPointY=2;
        bipedHead.rotateAngleZ=0;super.setRotationAngles(walk,amount,age,yaw,pitch,scale,e);
        float phase=age*.1F+walk*.5F,amplitude=.08F+amount*.4F;leftEar.rotateAngleZ=-.5235988F-MathHelper.cos(phase*1.2F)*amplitude;rightEar.rotateAngleZ=.5235988F+MathHelper.cos(phase)*amplitude;
        if(e instanceof EntityPiglin116){EntityPiglin116 p=(EntityPiglin116)e;
            if(p.isDancing()){
                float t=age/60F;rightEar.rotateAngleZ=.5235988F+(float)Math.PI/180*MathHelper.sin(t*30)*10;leftEar.rotateAngleZ=-.5235988F-(float)Math.PI/180*MathHelper.cos(t*30)*10;
                bipedHead.rotationPointX=MathHelper.sin(t*10);bipedHead.rotationPointY=MathHelper.sin(t*40)+.4F;
                bipedRightArm.rotateAngleZ=(float)Math.PI/180*(70+MathHelper.cos(t*40)*10);bipedLeftArm.rotateAngleZ=-bipedRightArm.rotateAngleZ;
                bipedRightArm.rotationPointY=bipedLeftArm.rotationPointY=MathHelper.sin(t*40)*.5F+1.5F;bipedBody.rotationPointY=MathHelper.sin(t*40)*.35F;
            }
            else if(p.isAdmiring()){bipedHead.rotateAngleX=.5F;bipedHead.rotateAngleY=0;bipedLeftArm.rotateAngleY=.5F;bipedLeftArm.rotateAngleX=-.9F;}
            else if(p.getHeldItem()!=null&&p.getHeldItem().getItem() instanceof com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow&&(p.getCrossbowChargeTicks()>0||p.isAggressive())){
                bipedRightArm.rotateAngleY=bipedHead.rotateAngleY-.3F;bipedLeftArm.rotateAngleY=bipedHead.rotateAngleY+.6F;bipedRightArm.rotateAngleX=bipedHead.rotateAngleX-1.5707964F;bipedLeftArm.rotateAngleX=bipedHead.rotateAngleX-1.5F;
                bipedRightArm.rotateAngleZ=bipedLeftArm.rotateAngleZ=0;
                if(p.getCrossbowChargeTicks()>0){float progress=MathHelper.clamp_float((float)p.getCrossbowChargeTicks()/com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow.chargeDuration(p.getHeldItem()),0,1);bipedRightArm.rotateAngleY=-.8F;bipedRightArm.rotateAngleX=-.97079635F;bipedLeftArm.rotateAngleY=.4F+progress*.45F;bipedLeftArm.rotateAngleX=-.97079635F-progress*.6F;}
            }
            else if(p.isAggressive()&&onGround<=0)bipedRightArm.rotateAngleX=-1.8F;
        }else if(e instanceof EntityZombifiedPiglin116){
            float swing=MathHelper.sin(onGround*(float)Math.PI);bipedRightArm.rotateAngleX=bipedLeftArm.rotateAngleX=-1.5707964F+swing*1.2F;
        }
    }
}
