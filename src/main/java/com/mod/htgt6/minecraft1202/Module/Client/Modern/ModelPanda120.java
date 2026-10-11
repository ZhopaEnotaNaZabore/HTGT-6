package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPanda120;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

/** Vanilla 1.20 panda layer geometry adapted to ModelRenderer's 1.7 animation API. */
public final class ModelPanda120 extends ModelBase {
    public final ModelRenderer head,body,rightHindLeg,leftHindLeg,rightFrontLeg,leftFrontLeg;
    private float sitAmount,onBackAmount,rollAmount;
    @Override public void setLivingAnimations(net.minecraft.entity.EntityLivingBase raw,float limb,float amount,float partialTick) {
        EntityPanda120 panda=(EntityPanda120)raw;
        sitAmount=panda.getSitAmount(partialTick);
        onBackAmount=panda.getOnBackAmount(partialTick);
        rollAmount=panda.isChild()?0F:panda.getRollAmount(partialTick);
    }

    public ModelPanda120(){
        textureWidth=64;textureHeight=64;
        head=new ModelRenderer(this,0,6);head.addBox(-6.5F,-5.0F,-4.0F,13,10,9);
        head.setTextureOffset(45,16).addBox(-3.5F,0.0F,-6.0F,7,5,2);
        head.setTextureOffset(52,25).addBox(3.5F,-8.0F,-1.0F,5,4,1);
        head.setTextureOffset(52,25).addBox(-8.5F,-8.0F,-1.0F,5,4,1);
        head.setRotationPoint(0.0F,11.5F,-17.0F);
        body=new ModelRenderer(this,0,25);body.addBox(-9.5F,-13.0F,-6.5F,19,26,13);
        body.setRotationPoint(0.0F,10.0F,0.0F);body.rotateAngleX=((float)Math.PI/2.0F);
        rightHindLeg=leg(40,0,-5.5F,15.0F,9.0F);
        leftHindLeg=leg(40,0,5.5F,15.0F,9.0F);
        rightFrontLeg=leg(40,0,-5.5F,15.0F,-9.0F);
        leftFrontLeg=leg(40,0,5.5F,15.0F,-9.0F);
    }

    private ModelRenderer leg(int u,int v,float x,float y,float z){ModelRenderer part=new ModelRenderer(this,u,v);
        part.addBox(-3.0F,0.0F,-3.0F,6,9,6);part.setRotationPoint(x,y,z);return part;}

    @Override public void setRotationAngles(float limb,float amount,float age,float yaw,float pitch,float scale,Entity raw){
        EntityPanda120 panda=(EntityPanda120)raw;
        float walk=MathHelper.cos(limb*.6662F)*1.4F*amount;
        rightHindLeg.rotateAngleX=leftFrontLeg.rotateAngleX=walk;
        leftHindLeg.rotateAngleX=rightFrontLeg.rotateAngleX=MathHelper.cos(limb*.6662F+(float)Math.PI)*1.4F*amount;
        head.rotateAngleY=yaw*.017453292F;head.rotateAngleX=pitch*.017453292F;
        head.rotateAngleZ=0;
        body.rotateAngleX=(float)Math.PI/2F;
        rightHindLeg.rotateAngleZ=leftHindLeg.rotateAngleZ=rightFrontLeg.rotateAngleZ=leftFrontLeg.rotateAngleZ=0;
        if(panda.getUnhappyCounter()>0) {
            head.rotateAngleY=head.rotateAngleZ=.35F*MathHelper.sin(.6F*age);
            rightFrontLeg.rotateAngleX=-.75F*MathHelper.sin(.3F*age);
            leftFrontLeg.rotateAngleX=.75F*MathHelper.sin(.3F*age);
        }
        if(panda.isSneezing()) {
            int ticks=panda.getSneezeCounter();
            if(ticks<15)head.rotateAngleX=-.7853982F*ticks/14F;
            // Preserve the integer division used by PandaModel 1.20.2.
            else if(ticks<20)head.rotateAngleX=-.7853982F+.7853982F*((ticks-15)/5);
        }
        float sit=sitAmount,back=onBackAmount;
        if(sit>0){
            body.rotateAngleX=lerp(body.rotateAngleX,1.7407963F,sit);
            head.rotateAngleX=lerp(head.rotateAngleX,1.5707964F,sit);
            rightFrontLeg.rotateAngleZ=-.27079642F;leftFrontLeg.rotateAngleZ=.27079642F;
            rightHindLeg.rotateAngleZ=.5707964F;leftHindLeg.rotateAngleZ=-.5707964F;
            if(panda.getEatCounter()>0) {
                float chew=.2F*MathHelper.sin(age*.6F);
                head.rotateAngleX=1.5707964F+chew;
                rightFrontLeg.rotateAngleX=leftFrontLeg.rotateAngleX=-.4F-chew;
            }
            if(panda.isScared()) {
                head.rotateAngleX=2.1707964F;
                rightFrontLeg.rotateAngleX=leftFrontLeg.rotateAngleX=-.9F;
            }
        }
        if(back>0){
            rightHindLeg.rotateAngleX=-.6F*MathHelper.sin(age*.15F);
            leftHindLeg.rotateAngleX=.6F*MathHelper.sin(age*.15F);
            rightFrontLeg.rotateAngleX=.3F*MathHelper.sin(age*.25F);
            leftFrontLeg.rotateAngleX=-.3F*MathHelper.sin(age*.25F);
            head.rotateAngleX=lerp(head.rotateAngleX,1.5707964F,back);
        }
        if(rollAmount>0) {
            head.rotateAngleX=lerp(head.rotateAngleX,2.0561945F,rollAmount);
            float kick=.5F*MathHelper.sin(age*.5F);
            rightHindLeg.rotateAngleX=leftFrontLeg.rotateAngleX=-kick;
            leftHindLeg.rotateAngleX=rightFrontLeg.rotateAngleX=kick;
        }
    }

    private static float lerp(float from,float to,float amount){
        float delta=to-from;
        while(delta<-(float)Math.PI)delta+=(float)Math.PI*2F;
        while(delta>=(float)Math.PI)delta-=(float)Math.PI*2F;
        return from+delta*amount;
    }
    private void renderBody(float scale){body.render(scale);rightHindLeg.render(scale);
        leftHindLeg.render(scale);rightFrontLeg.render(scale);leftFrontLeg.render(scale);}
    @Override public void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale){
        setRotationAngles(limb,amount,age,yaw,pitch,scale,entity);
        if(isChild){
            // AgeableListModel(true, 23, 4.8, 2.7, 3, 49) from PandaModel 1.20.2.
            GL11.glPushMatrix();
            try {float headScale=1.5F/2.7F;GL11.glScalef(headScale,headScale,headScale);
                GL11.glTranslatef(0,23F*scale,4.8F*scale);head.render(scale);}
            finally {GL11.glPopMatrix();}
            GL11.glPushMatrix();
            try {GL11.glScalef(1F/3F,1F/3F,1F/3F);GL11.glTranslatef(0,49F*scale,0);renderBody(scale);}
            finally {GL11.glPopMatrix();}
        } else {head.render(scale);renderBody(scale);}
    }
}
