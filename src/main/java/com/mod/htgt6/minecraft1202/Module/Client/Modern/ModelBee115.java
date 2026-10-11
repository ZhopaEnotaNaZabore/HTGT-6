package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import net.minecraft.client.model.*;import net.minecraft.entity.Entity;import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityBee115;
/** Official 64x64 bee mesh, with independent wings, antennae, legs and stinger. */
public final class ModelBee115 extends ModelBase {
 private final ModelRenderer root,body,sting,left,right;private final ModelRenderer[] legs=new ModelRenderer[3];
 public ModelBee115(){textureWidth=64;textureHeight=64;root=new ModelRenderer(this);root.setRotationPoint(0,19,0);body=part(0,0,-3.5F,-4,-5,7,7,10);root.addChild(body);sting=part(26,7,0,-1,5,0,1,2);body.addChild(sting);
  ModelRenderer a=part(2,0,1.5F,-2,-3,1,2,3),b=part(2,3,-2.5F,-2,-3,1,2,3);a.setRotationPoint(0,-2,-5);b.setRotationPoint(0,-2,-5);body.addChild(a);body.addChild(b);
  right=part(0,18,-9,0,0,9,0,6);right.setRotationPoint(-1.5F,-4,-3);root.addChild(right);left=part(0,18,0,0,0,9,0,6);left.mirror=true;left.setRotationPoint(1.5F,-4,-3);root.addChild(left);
  for(int i=0;i<3;i++){legs[i]=part(26,1+i*2,-5,0,0,7,2,0);legs[i].setRotationPoint(1.5F,3,-2+i*2);root.addChild(legs[i]);}
 }
 private ModelRenderer part(int u,int v,float x,float y,float z,int w,int h,int d){ModelRenderer p=new ModelRenderer(this,u,v);p.addBox(x,y,z,w,h,d);return p;}
 @Override public void render(Entity entity,float walk,float amount,float age,float yaw,float pitch,float scale){EntityBee115 bee=(EntityBee115)entity;boolean rest=bee.onGround&&bee.motionX*bee.motionX+bee.motionZ*bee.motionZ<1E-7;right.rotateAngleY=rest?-.2618F:0;left.rotateAngleY=-right.rotateAngleY;right.rotateAngleZ=rest?0:(float)Math.cos(age*2.1F)*.4712389F;left.rotateAngleZ=-right.rotateAngleZ;sting.showModel=!bee.stung();root.rotateAngleX=bee.angry()?.35F:rest?0:.1F+(float)Math.cos(age*.18F)*.07854F;root.rotationPointY=rest?19:19-(float)Math.cos(age*.18F)*.9F;for(ModelRenderer leg:legs)leg.rotateAngleX=rest?0:.7854F;org.lwjgl.opengl.GL11.glPushMatrix();if(bee.isChild()){org.lwjgl.opengl.GL11.glScalef(.5F,.5F,.5F);org.lwjgl.opengl.GL11.glTranslatef(0,24*scale,0);}root.render(scale);org.lwjgl.opengl.GL11.glPopMatrix();}
}
