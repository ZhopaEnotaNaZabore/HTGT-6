package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import net.minecraft.client.model.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.HoglinCombat116;

/** Official 128x64 mesh and age/attack transforms. */
public final class ModelHoglin116 extends ModelBase {
    public final ModelRenderer body,mane,head,rightEar,leftEar,rightFront,leftFront,rightHind,leftHind;
    public ModelHoglin116(){
        textureWidth=128;textureHeight=64;
        body=part(1,1,-8,-7,-13,16,14,26,0,7,0);
        mane=new ModelRenderer(this,90,33);mane.addBox(0,0,-9,0,10,19,.001F);mane.setRotationPoint(0,-14,-5);body.addChild(mane);
        head=part(61,1,-7,-3,-19,14,6,19,0,2,-12);
        rightEar=part(1,1,-6,-1,-2,6,1,4,-6,-2,-3);leftEar=part(1,6,0,-1,-2,6,1,4,6,-2,-3);head.addChild(rightEar);head.addChild(leftEar);
        head.addChild(part(10,13,-1,-11,-1,2,11,2,-7,2,-12));head.addChild(part(1,13,-1,-11,-1,2,11,2,7,2,-12));
        rightFront=part(66,42,-3,0,-3,6,14,6,-4,10,-8.5F);leftFront=part(41,42,-3,0,-3,6,14,6,4,10,-8.5F);
        rightHind=part(21,45,-2.5F,0,-2.5F,5,11,5,-5,13,10);leftHind=part(0,45,-2.5F,0,-2.5F,5,11,5,5,13,10);
    }
    private ModelRenderer part(int u,int v,float x,float y,float z,int a,int b,int c,float px,float py,float pz){ModelRenderer p=new ModelRenderer(this,u,v);p.addBox(x,y,z,a,b,c);p.setRotationPoint(px,py,pz);return p;}
    @Override public void setRotationAngles(float walk,float amount,float age,float yaw,float pitch,float scale,Entity e){
        rightEar.rotateAngleZ=-.6981317F-amount*MathHelper.sin(walk);leftEar.rotateAngleZ=.6981317F+amount*MathHelper.sin(walk);head.rotateAngleY=yaw*(float)Math.PI/180;
        int ticks=e instanceof HoglinCombat116.Animated?((HoglinCombat116.Animated)e).attackAnimationTicks():0;float attack=1-Math.abs(10-2*ticks)/10F;
        head.rotateAngleX=.87266463F+attack*(-.34906584F-.87266463F);head.rotationPointY=isChild?2+attack*3:2;mane.rotationPointZ=isChild?-3:-7;
        rightFront.rotateAngleX=leftHind.rotateAngleX=MathHelper.cos(walk)*1.2F*amount;
        leftFront.rotateAngleX=rightHind.rotateAngleX=MathHelper.cos(walk+(float)Math.PI)*1.2F*amount;
    }
    @Override public void render(Entity e,float walk,float amount,float age,float yaw,float pitch,float scale){
        setRotationAngles(walk,amount,age,yaw,pitch,scale,e);
        GL11.glPushMatrix();try{if(isChild){float factor=1.5F/1.9F;GL11.glScalef(factor,factor,factor);GL11.glTranslatef(0,8*scale,6*scale);}head.render(scale);}finally{GL11.glPopMatrix();}
        GL11.glPushMatrix();try{if(isChild){GL11.glScalef(.5F,.5F,.5F);GL11.glTranslatef(0,24*scale,0);}body.render(scale);rightFront.render(scale);leftFront.render(scale);rightHind.render(scale);leftHind.render(scale);}finally{GL11.glPopMatrix();}
    }
}
