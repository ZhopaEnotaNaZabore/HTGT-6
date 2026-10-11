package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;
import net.minecraft.client.model.ModelBase;import net.minecraft.client.model.ModelRenderer;import net.minecraft.entity.Entity;
/** Connected phantom model with articulated wing tips, head and two tail segments. */
public final class ModelPhantom120 extends ModelBase {
 private final ModelRenderer root,leftWing,rightWing,leftTip,rightTip,tailBase,tailTip;
 public ModelPhantom120(){textureWidth=64;textureHeight=64;root=new ModelRenderer(this,0,8);root.setRotationPoint(0,22,0);root.addBox(-3,-2,-8,5,3,9);ModelRenderer head=new ModelRenderer(this,0,0);head.setRotationPoint(0,1,-7);head.addBox(-4,-2,-5,7,3,5);head.rotateAngleX=.2F;root.addChild(head);
  leftWing=new ModelRenderer(this,23,12);leftWing.setRotationPoint(2,-2,-8);leftWing.addBox(0,0,0,6,2,9);leftWing.rotateAngleZ=.1F;root.addChild(leftWing);leftTip=new ModelRenderer(this,16,24);leftTip.setRotationPoint(6,0,0);leftTip.addBox(0,0,0,13,1,9);leftTip.rotateAngleZ=.1F;leftWing.addChild(leftTip);
  rightWing=new ModelRenderer(this,23,12);rightWing.mirror=true;rightWing.setRotationPoint(-3,-2,-8);rightWing.addBox(-6,0,0,6,2,9);rightWing.rotateAngleZ=-.1F;root.addChild(rightWing);rightTip=new ModelRenderer(this,16,24);rightTip.mirror=true;rightTip.setRotationPoint(-6,0,0);rightTip.addBox(-13,0,0,13,1,9);rightTip.rotateAngleZ=-.1F;rightWing.addChild(rightTip);
  tailBase=new ModelRenderer(this,3,20);tailBase.setRotationPoint(0,-2,1);tailBase.addBox(-2,0,0,3,2,6);root.addChild(tailBase);tailTip=new ModelRenderer(this,4,29);tailTip.setRotationPoint(0,.5F,6);tailTip.addBox(-1,0,0,1,1,6);tailBase.addChild(tailTip);}
 @Override public void setRotationAngles(float a,float b,float age,float yaw,float pitch,float s,Entity e){float flap=(float)Math.cos(age*.35F)*.35F;leftWing.rotateAngleZ=.1F+flap;leftTip.rotateAngleZ=.1F+flap*.5F;rightWing.rotateAngleZ=-leftWing.rotateAngleZ;rightTip.rotateAngleZ=-leftTip.rotateAngleZ;float tail=(float)Math.cos(age*.2F)*.15F;tailBase.rotateAngleX=tail;tailTip.rotateAngleX=tail;}
 @Override public void render(Entity e,float a,float b,float age,float yaw,float pitch,float s){setRotationAngles(a,b,age,yaw,pitch,s,e);root.render(s);}
}
