package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityCamel120;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

/** Official 1.20.2 Camel geometry translated to the flat 1.7 ModelRenderer root. */
public final class ModelCamel120 extends ModelBase {
    public final ModelRenderer body,hump,head,leftFrontLeg,rightFrontLeg,leftHindLeg,rightHindLeg,tail,saddle,bridle,reins;

    public ModelCamel120(){
        textureWidth=128;textureHeight=128;
        body=new ModelRenderer(this,0,25);body.addBox(-7.5F,-12,-23.5F,15,12,27);body.setRotationPoint(0,4,9.5F);
        hump=new ModelRenderer(this,74,0);hump.addBox(-4.5F,-5,-5.5F,9,5,11);hump.setRotationPoint(0,-12,-10);body.addChild(hump);
        tail=new ModelRenderer(this,122,0);tail.addBox(-1.5F,0,0,3,14,0);tail.setRotationPoint(0,-9,3.5F);body.addChild(tail);
        head=new ModelRenderer(this,60,24);head.addBox(-3.5F,-7,-15,7,8,19);
        head.setTextureOffset(21,0).addBox(-3.5F,-21,-15,7,14,7);
        head.setTextureOffset(50,0).addBox(-2.5F,-21,-21,5,5,6);head.setRotationPoint(0,-3,-19.5F);body.addChild(head);
        ModelRenderer leftEar=new ModelRenderer(this,45,0);leftEar.addBox(-.5F,.5F,-1,3,1,2);leftEar.setRotationPoint(2.5F,-21,-9.5F);head.addChild(leftEar);
        ModelRenderer rightEar=new ModelRenderer(this,67,0);rightEar.addBox(-2.5F,.5F,-1,3,1,2);rightEar.setRotationPoint(-2.5F,-21,-9.5F);head.addChild(rightEar);
        leftHindLeg=leg(58,16,4.9F,1,9.5F);rightHindLeg=leg(94,16,-4.9F,1,9.5F);
        leftFrontLeg=leg(0,0,4.9F,1,-10.5F);rightFrontLeg=leg(0,26,-4.9F,1,-10.5F);
        saddle=new ModelRenderer(this,74,64);saddle.addBox(-4.5F,-17,-15.5F,9,5,11,.05F);
        saddle.setTextureOffset(92,114).addBox(-3.5F,-20,-15.5F,7,3,11,.05F);
        saddle.setTextureOffset(0,89).addBox(-7.5F,-12,-23.5F,15,12,27,.05F);body.addChild(saddle);
        reins=new ModelRenderer(this,98,42);reins.addBox(3.51F,-18,-17,0,7,15);
        reins.setTextureOffset(84,57).addBox(-3.5F,-18,-2,7,7,0);
        reins.setTextureOffset(98,42).addBox(-3.51F,-18,-17,0,7,15);head.addChild(reins);
        bridle=new ModelRenderer(this,60,87);bridle.addBox(-3.5F,-7,-15,7,8,19,.05F);
        bridle.setTextureOffset(21,64).addBox(-3.5F,-21,-15,7,14,7,.05F);
        bridle.setTextureOffset(50,64).addBox(-2.5F,-21,-21,5,5,6,.05F);
        bridle.setTextureOffset(74,70).addBox(2.5F,-19,-18,1,2,2);
        bridle.mirror=true;bridle.addBox(-3.5F,-19,-18,1,2,2);bridle.mirror=false;head.addChild(bridle);
    }

    private ModelRenderer leg(int u,int v,float x,float y,float z){ModelRenderer part=new ModelRenderer(this,u,v);
        part.addBox(-2.5F,2,-2.5F,5,21,5);part.setRotationPoint(x,y,z);return part;}

    @Override public void setRotationAngles(float limb,float amount,float age,float yaw,float pitch,float scale,Entity raw){
        EntityCamel120 camel=(EntityCamel120)raw;float sit=camel.getPoseProgress(0.0F);
        head.rotateAngleY=MathHelper.clamp_float(yaw,-30,30)*.017453292F;
        head.rotateAngleX=MathHelper.clamp_float(pitch,-25,45)*.017453292F+sit*.12F;
        float walk=MathHelper.cos(limb*.6662F)*1.0F*amount*(1.0F-sit);
        leftFrontLeg.rotateAngleX=rightHindLeg.rotateAngleX=walk;
        rightFrontLeg.rotateAngleX=leftHindLeg.rotateAngleX=-walk;
        body.rotationPointY=4.0F+sit*7.0F;
        leftFrontLeg.rotateAngleX+=sit*-1.35F;rightFrontLeg.rotateAngleX+=sit*-1.35F;
        leftHindLeg.rotateAngleX+=sit*1.35F;rightHindLeg.rotateAngleX+=sit*1.35F;
        tail.rotateAngleX=.1F*MathHelper.cos(age*.1F);
        saddle.showModel=bridle.showModel=camel.isSaddled();
        reins.showModel=camel.isSaddled()&&camel.riddenByEntity!=null;
    }

    private void renderAdult(float scale){body.render(scale);leftFrontLeg.render(scale);rightFrontLeg.render(scale);
        leftHindLeg.render(scale);rightHindLeg.render(scale);}

    @Override public void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale){
        setRotationAngles(limb,amount,age,yaw,pitch,scale,entity);
        if(isChild){GL11.glPushMatrix();GL11.glScalef(.45F,.45F,.45F);GL11.glTranslatef(0,29.35F*scale,0);renderAdult(scale);GL11.glPopMatrix();}
        else renderAdult(scale);
    }
}
