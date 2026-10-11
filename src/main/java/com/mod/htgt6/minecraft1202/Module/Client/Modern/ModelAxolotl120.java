package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAxolotl120;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

/** Minecraft 1.20 axolotl geometry and articulated animation adapted to ModelRenderer 1.7. */
public final class ModelAxolotl120 extends ModelBase {
    public final ModelRenderer body,head,tail,leftFrontLeg,rightFrontLeg,leftHindLeg,rightHindLeg;
    public final ModelRenderer topGills,leftGills,rightGills;

    public ModelAxolotl120(){
        textureWidth=64;textureHeight=64;
        body=new ModelRenderer(this,0,11);
        body.addBox(-4.0F,-2.0F,-9.0F,8,4,10);
        body.setTextureOffset(2,17).addBox(0.0F,-3.0F,-8.0F,0,5,9);
        body.setRotationPoint(0.0F,20.0F,5.0F);
        head=new ModelRenderer(this,0,1);head.addBox(-4.0F,-3.0F,-5.0F,8,5,5);
        head.setRotationPoint(0.0F,0.0F,-9.0F);body.addChild(head);
        topGills=new ModelRenderer(this,3,37);topGills.addBox(-4.0F,-3.0F,0.0F,8,3,0);
        topGills.setRotationPoint(0.0F,-3.0F,-1.0F);head.addChild(topGills);
        leftGills=new ModelRenderer(this,0,40);leftGills.addBox(-3.0F,-5.0F,0.0F,3,7,0);
        leftGills.setRotationPoint(-4.0F,0.0F,-1.0F);head.addChild(leftGills);
        rightGills=new ModelRenderer(this,11,40);rightGills.addBox(0.0F,-5.0F,0.0F,3,7,0);
        rightGills.setRotationPoint(4.0F,0.0F,-1.0F);head.addChild(rightGills);
        rightHindLeg=rightLeg(-3.5F,-1.0F);leftHindLeg=leftLeg(3.5F,-1.0F);
        rightFrontLeg=rightLeg(-3.5F,-8.0F);leftFrontLeg=leftLeg(3.5F,-8.0F);
        body.addChild(rightHindLeg);body.addChild(leftHindLeg);body.addChild(rightFrontLeg);body.addChild(leftFrontLeg);
        tail=new ModelRenderer(this,2,19);tail.addBox(0.0F,-3.0F,0.0F,0,5,12);
        tail.setRotationPoint(0.0F,0.0F,1.0F);body.addChild(tail);
    }
    private ModelRenderer leftLeg(float x,float z){ModelRenderer p=new ModelRenderer(this,2,13);p.addBox(-1,0,0,3,5,0);p.setRotationPoint(x,1,z);return p;}
    private ModelRenderer rightLeg(float x,float z){ModelRenderer p=new ModelRenderer(this,2,13);p.addBox(-2,0,0,3,5,0);p.setRotationPoint(x,1,z);return p;}

    @Override public void setRotationAngles(float limb,float amount,float age,float yaw,float pitch,float scale,Entity raw){
        resetPose();EntityAxolotl120 axolotl=(EntityAxolotl120)raw;
        if(axolotl.isPlayingDead()){playDead(yaw);return;}
        boolean moving=amount>0.01F||Math.abs(raw.motionX)+Math.abs(raw.motionZ)>0.015D;
        if(raw.isInWater()){if(moving)swimming(age,pitch);else hovering(age);}
        else if(moving)crawling(limb,amount,yaw);else layStill(age,yaw);
    }
    private void resetPose(){body.rotationPointX=0;body.rotationPointY=20;body.rotationPointZ=5;
        zero(body);zero(head);zero(tail);zero(topGills);zero(leftGills);zero(rightGills);
        zero(leftFrontLeg);zero(rightFrontLeg);zero(leftHindLeg);zero(rightHindLeg);}
    private static void zero(ModelRenderer p){p.rotateAngleX=0;p.rotateAngleY=0;p.rotateAngleZ=0;}
    private void layStill(float age,float yaw){float t=age*.09F,s=MathHelper.sin(t),c=MathHelper.cos(t);
        head.rotateAngleX=-.09F*(s*s-2*s);head.rotateAngleZ=-.2F;body.rotateAngleX=-.15F;body.rotateAngleY=yaw*.017453292F;
        tail.rotateAngleY=-.1F+.1F*(s*s-2*s);topGills.rotateAngleX=.6F+.05F*(c*c-3*s);
        leftGills.rotateAngleY=-topGills.rotateAngleX;rightGills.rotateAngleY=topGills.rotateAngleX;
        setLeg(leftHindLeg,1.1F,1,0);setLeg(leftFrontLeg,.8F,2.3F,-.5F);mirrorLegs();}
    private void crawling(float limb,float amount,float yaw){float t=limb*.11F,c=MathHelper.cos(t),step=(c*c-2*c)/5F,swing=.7F*c*amount;
        head.rotateAngleY=.09F*c;tail.rotateAngleY=head.rotateAngleY;topGills.rotateAngleX=.6F-.08F*(c*c+2*MathHelper.sin(t));
        leftGills.rotateAngleY=-topGills.rotateAngleX;rightGills.rotateAngleY=topGills.rotateAngleX;
        setLeg(leftHindLeg,.9424779F,1.5F-step,-.1F);setLeg(leftFrontLeg,1.0995574F,1.5707964F-swing,0);mirrorLegs();body.rotateAngleY=yaw*.017453292F;}
    private void hovering(float age){float t=age*.075F,c=MathHelper.cos(t),s=MathHelper.sin(t)*.15F;
        body.rotateAngleX=-.15F+.075F*c;body.rotationPointY+=s;tail.rotateAngleY=.5F*c;topGills.rotateAngleX=.2F*c;
        leftGills.rotateAngleY=-topGills.rotateAngleX;rightGills.rotateAngleY=topGills.rotateAngleX;
        setLeg(leftHindLeg,2.3561945F-.11F*c,.47123894F,1.7278761F);setLeg(leftFrontLeg,.7853982F-.2F*c,2.042035F,0);mirrorLegs();}
    private void swimming(float age,float pitch){float t=age*.33F,s=MathHelper.sin(t),c=MathHelper.cos(t),wave=.13F*s;
        body.rotateAngleX=pitch*.017453292F+wave;head.rotateAngleX=-wave*1.8F;body.rotationPointY-=.45F*c;
        topGills.rotateAngleX=-.8F-.5F*s;leftGills.rotateAngleY=.9F+.3F*s;rightGills.rotateAngleY=-leftGills.rotateAngleY;
        tail.rotateAngleY=.3F*MathHelper.cos(t*.9F)*s;setLeg(leftHindLeg,1.8849558F,-.4F*s,1.5707964F);
        setLeg(leftFrontLeg,1.8849558F,-.2F*c-.1F,1.5707964F);mirrorLegs();}
    private void playDead(float yaw){body.rotateAngleX=-.15F;body.rotateAngleY=yaw*.017453292F;body.rotateAngleZ=.35F;
        setLeg(leftHindLeg,1.4137167F,1.0995574F,.7853982F);setLeg(leftFrontLeg,.7853982F,2.042035F,0);mirrorLegs();}
    private void setLeg(ModelRenderer leg,float x,float y,float z){leg.rotateAngleX=x;leg.rotateAngleY=y;leg.rotateAngleZ=z;}
    private void mirrorLegs(){setLeg(rightHindLeg,leftHindLeg.rotateAngleX,-leftHindLeg.rotateAngleY,-leftHindLeg.rotateAngleZ);
        setLeg(rightFrontLeg,leftFrontLeg.rotateAngleX,-leftFrontLeg.rotateAngleY,-leftFrontLeg.rotateAngleZ);}
    @Override public void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale){setRotationAngles(limb,amount,age,yaw,pitch,scale,entity);body.render(scale);}
}
