package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityParrot120;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

/** Minecraft 1.20.2 parrot geometry translated one part at a time to ModelRenderer. */
public final class ModelParrot120 extends ModelBase {
    public final ModelRenderer body,tail,leftWing,rightWing,head,feather,leftLeg,rightLeg;

    public ModelParrot120(){
        textureWidth=32;textureHeight=32;
        body=new ModelRenderer(this,2,8);body.addBox(-1.5F,0.0F,-1.5F,3,6,3);body.setRotationPoint(0,16.5F,-3.0F);
        tail=new ModelRenderer(this,22,1);tail.addBox(-1.5F,-1.0F,-1.0F,3,4,1);tail.setRotationPoint(0,21.07F,1.16F);
        leftWing=new ModelRenderer(this,19,8);leftWing.addBox(-.5F,0,-1.5F,1,5,3);leftWing.setRotationPoint(1.5F,16.94F,-2.76F);
        rightWing=new ModelRenderer(this,19,8);rightWing.mirror=true;rightWing.addBox(-.5F,0,-1.5F,1,5,3);rightWing.setRotationPoint(-1.5F,16.94F,-2.76F);
        head=new ModelRenderer(this,2,2);head.addBox(-1,-1.5F,-1,2,3,2);head.setRotationPoint(0,15.69F,-2.76F);
        ModelRenderer headTop=new ModelRenderer(this,10,0);headTop.addBox(-1,-.5F,-2,2,1,4);headTop.setRotationPoint(0,-2,-1);head.addChild(headTop);
        ModelRenderer upperBeak=new ModelRenderer(this,11,7);upperBeak.addBox(-.5F,-1,-.5F,1,2,1);upperBeak.setRotationPoint(0,-.5F,-1.5F);head.addChild(upperBeak);
        ModelRenderer lowerBeak=new ModelRenderer(this,16,7);lowerBeak.addBox(-.5F,0,-.5F,1,2,1);lowerBeak.setRotationPoint(0,-1.75F,-2.45F);head.addChild(lowerBeak);
        feather=new ModelRenderer(this,2,18);feather.addBox(0,-4,-2,0,5,4);feather.setRotationPoint(0,-2.15F,.15F);head.addChild(feather);
        leftLeg=leg(1.0F);rightLeg=leg(-1.0F);
    }

    private ModelRenderer leg(float x){ModelRenderer part=new ModelRenderer(this,14,18);
        part.addBox(-.5F,0,-.5F,1,2,1);part.setRotationPoint(x,22.0F,-1.05F);return part;}

    @Override public void setRotationAngles(float limb,float amount,float age,float yaw,float pitch,float scale,Entity raw){
        EntityParrot120 parrot=(EntityParrot120)raw;
        body.rotationPointX=head.rotationPointX=tail.rotationPointX=0;
        leftWing.rotationPointX=1.5F;rightWing.rotationPointX=-1.5F;
        head.rotateAngleY=yaw*.017453292F;head.rotateAngleX=pitch*.017453292F;head.rotateAngleZ=0;
        feather.rotateAngleX=-.2214F;body.rotateAngleX=.4937F;
        leftWing.rotateAngleX=rightWing.rotateAngleX=-.6981F;
        leftWing.rotateAngleY=rightWing.rotateAngleY=-(float)Math.PI;
        leftLeg.rotateAngleX=rightLeg.rotateAngleX=-.0299F;
        // RenderParrot120 supplies the interpolated flap amount as the age parameter.
        float bob=age*.3F;
        body.rotationPointY=16.5F+bob;head.rotationPointY=15.69F+bob;
        tail.rotationPointY=21.07F+bob;leftWing.rotationPointY=rightWing.rotationPointY=16.94F+bob;
        leftLeg.rotationPointY=rightLeg.rotationPointY=22.0F+bob;
        float walk=parrot.onGround?MathHelper.cos(limb*.6662F)*1.4F*amount:0;
        leftLeg.rotateAngleX+=walk;rightLeg.rotateAngleX-=walk;
        if(!parrot.onGround)leftLeg.rotateAngleX=rightLeg.rotateAngleX=-.0299F+.6981317F;
        tail.rotateAngleX=1.015F+MathHelper.cos(limb*.6662F)*.3F*amount;
        float flap=.0873F+age;
        leftWing.rotateAngleZ=-flap;rightWing.rotateAngleZ=flap;
        leftLeg.rotateAngleZ=rightLeg.rotateAngleZ=0;
        if(parrot.isSitting()&&!parrot.isPartyParrot()){
            head.rotationPointY=17.59F;body.rotationPointY=18.4F;tail.rotationPointY=22.97F;
            tail.rotateAngleX=1.5388988F;
            leftWing.rotationPointY=rightWing.rotationPointY=18.84F;
            leftWing.rotateAngleZ=-.0873F;rightWing.rotateAngleZ=.0873F;
            leftLeg.rotationPointY=rightLeg.rotationPointY=23.9F;
            leftLeg.rotateAngleX=rightLeg.rotateAngleX=-.0299F+(float)Math.PI/2;
        }
        if(parrot.isPartyParrot()){
            float sway=MathHelper.cos(parrot.ticksExisted),bounce=MathHelper.sin(parrot.ticksExisted);
            head.rotationPointX=body.rotationPointX=tail.rotationPointX=sway;
            head.rotationPointY=15.69F+bounce;body.rotationPointY=16.5F+bounce;tail.rotationPointY=21.07F+bounce;
            head.rotateAngleX=head.rotateAngleY=0;head.rotateAngleZ=bounce*.4F;
            leftWing.rotationPointX=1.5F+sway;rightWing.rotationPointX=-1.5F+sway;
            leftWing.rotationPointY=rightWing.rotationPointY=16.94F+bounce;
            leftLeg.rotationPointY=rightLeg.rotationPointY=22;
            leftLeg.rotateAngleX=rightLeg.rotateAngleX=-.0299F;
            leftLeg.rotateAngleZ=-(float)Math.PI/9;rightLeg.rotateAngleZ=(float)Math.PI/9;
        }
    }

    @Override public void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale){
        setRotationAngles(limb,amount,age,yaw,pitch,scale,entity);
        body.render(scale);tail.render(scale);leftWing.render(scale);rightWing.render(scale);
        head.render(scale);leftLeg.render(scale);rightLeg.render(scale);
    }
}
