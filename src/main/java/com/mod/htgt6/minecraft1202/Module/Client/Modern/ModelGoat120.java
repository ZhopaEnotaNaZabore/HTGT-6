package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityGoat120;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

/** Exact 1.20.2 goat pivots and UV boxes adapted to the 1.7 renderer. */
public final class ModelGoat120 extends ModelBase {
    public final ModelRenderer body,head,nose,leftHorn,rightHorn,leftFrontLeg,rightFrontLeg,leftHindLeg,rightHindLeg;

    public ModelGoat120(){
        textureWidth=64;textureHeight=64;
        head=new ModelRenderer(this,2,61);
        head.addBox(-6,-11,-10,3,2,1);
        head.mirror=true;head.addBox(2,-11,-10,3,2,1);head.mirror=false;
        head.setTextureOffset(23,52).addBox(-.5F,-3,-14,0,7,5);
        head.setRotationPoint(1,14,0);
        leftHorn=new ModelRenderer(this,12,55);leftHorn.addBox(-.01F,-16,-10,2,7,2);head.addChild(leftHorn);
        rightHorn=new ModelRenderer(this,12,55);rightHorn.addBox(-2.99F,-16,-10,2,7,2);head.addChild(rightHorn);
        nose=new ModelRenderer(this,34,46);nose.addBox(-3,-4,-8,5,7,10);nose.setRotationPoint(0,-8,-8);
        nose.rotateAngleX=.9599F;head.addChild(nose);
        body=new ModelRenderer(this,1,1);body.addBox(-4,-17,-7,9,11,16);
        body.setTextureOffset(0,28).addBox(-5,-18,-8,11,14,11);body.setRotationPoint(0,24,0);
        leftHindLeg=leg(36,29,1,14,4,4);rightHindLeg=leg(49,29,-3,14,4,4);
        leftFrontLeg=leg(49,2,1,14,-6,0);rightFrontLeg=leg(35,2,-3,14,-6,0);
    }

    private ModelRenderer leg(int u,int v,float x,float y,float z,float boxY){ModelRenderer part=new ModelRenderer(this,u,v);
        part.addBox(0,boxY,0,3,boxY==0?10:6,3);part.setRotationPoint(x,y,z);return part;}

    @Override public void setRotationAngles(float limb,float amount,float age,float yaw,float pitch,float scale,Entity raw){
        EntityGoat120 goat=(EntityGoat120)raw;
        head.rotateAngleY=yaw*.017453292F;
        head.rotateAngleX=(pitch+(goat.isRamming()?-20.0F:0.0F))*.017453292F;
        float walk=MathHelper.cos(limb*.6662F)*1.4F*amount;
        leftFrontLeg.rotateAngleX=rightHindLeg.rotateAngleX=walk;
        rightFrontLeg.rotateAngleX=leftHindLeg.rotateAngleX=-walk;
        leftHorn.showModel=goat.hasLeftHorn();rightHorn.showModel=goat.hasRightHorn();
    }

    private void renderAdult(float scale){head.render(scale);body.render(scale);leftFrontLeg.render(scale);
        rightFrontLeg.render(scale);leftHindLeg.render(scale);rightHindLeg.render(scale);}

    @Override public void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale){
        setRotationAngles(limb,amount,age,yaw,pitch,scale,entity);
        if(isChild){GL11.glPushMatrix();GL11.glScalef(.5F,.5F,.5F);GL11.glTranslatef(0,24.0F*scale,0);renderAdult(scale);GL11.glPopMatrix();}
        else renderAdult(scale);
    }
}
