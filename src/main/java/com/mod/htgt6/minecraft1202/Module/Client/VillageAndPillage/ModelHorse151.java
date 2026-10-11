package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;
import net.minecraft.client.model.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
/** Official 1.20.2 horse geometry/UVs and poses, adapted to ModelRenderer and 1.7 horse state. */
public final class ModelHorse151 extends ModelBase {
 public final ModelRenderer body,headParts,head,tail;
 public final ModelRenderer[] adultLegs=new ModelRenderer[4],babyLegs=new ModelRenderer[4],saddleParts=new ModelRenderer[5],reins=new ModelRenderer[2];
 public ModelHorse151(float deform){
  textureWidth=64;textureHeight=64;
  body=part(0,32,-5,-8,-17,10,10,22,.05F,0,11,5,false);
  headParts=part(0,35,-2.05F,-6,-2,4,12,7,0,0,4,-12,false);headParts.rotateAngleX=.5235988F;
  head=child(headParts,part(0,13,-3,-11,-2,6,5,7,deform,0,0,0,false));
  child(headParts,part(56,36,-1,-11,5.01F,2,16,2,deform,0,0,0,false));
  child(headParts,part(0,25,-2,-11,-7,4,5,5,deform,0,0,0,false));
  for(int i=0;i<4;i++){
   boolean left=(i&1)==1,front=i>=2;float x=left?-3:-1,z=front?-1.9F:-1;
   adultLegs[i]=part(48,21,x,-1.01F,z,4,11,4,deform,left?4:-4,14,front?-12:7,left);
   ModelRenderer baby=new ModelRenderer(this,48,21);baby.mirror=left;baby.setRotationPoint(left?4:-4,14,front?-12:7);baby.cubeList.add(new ModernHorseBox151(baby,48,21,x,-1.01F,z,4,11,4,deform,deform+5.5F,deform));babyLegs[i]=baby;
  }
  tail=child(body,part(42,36,-1.5F,0,0,3,14,4,deform,0,-5,2,false));tail.rotateAngleX=.5235988F;
  saddleParts[0]=child(body,part(26,0,-5,-8,-9,10,9,9,.5F,0,0,0,false));
  saddleParts[1]=child(headParts,part(29,5,2,-9,-6,1,2,2,deform,0,0,0,false));
  saddleParts[2]=child(headParts,part(29,5,-3,-9,-6,1,2,2,deform,0,0,0,false));
  reins[0]=child(headParts,part(32,2,3.1F,-6,-8,0,3,16,0,0,0,0,false));reins[0].rotateAngleX=-.5235988F;
  reins[1]=child(headParts,part(32,2,-3.1F,-6,-8,0,3,16,0,0,0,0,false));reins[1].rotateAngleX=-.5235988F;
  saddleParts[3]=child(headParts,part(1,1,-3,-11,-1.9F,6,5,6,.22F,0,0,0,false));
  saddleParts[4]=child(headParts,part(19,0,-2,-11,-4,4,5,2,.2F,0,0,0,false));
  child(head,part(19,16,.55F,-13,4,2,3,1,-.001F,0,0,0,false));
  child(head,part(19,16,-2.55F,-13,4,2,3,1,-.001F,0,0,0,false));
 }
 private ModelRenderer part(int u,int v,float x,float y,float z,int w,int h,int d,float def,float px,float py,float pz,boolean mirror){ModelRenderer p=new ModelRenderer(this,u,v);p.mirror=mirror;p.addBox(x,y,z,w,h,d,def);p.setRotationPoint(px,py,pz);return p;}
 private ModelRenderer child(ModelRenderer parent,ModelRenderer child){parent.addChild(child);return child;}
 @Override public void setRotationAngles(float limb,float amount,float age,float yaw,float pitch,float scale,Entity entity){EntityHorse h=(EntityHorse)entity;for(ModelRenderer p:saddleParts)p.showModel=h.isHorseSaddled();for(ModelRenderer p:reins)p.showModel=h.isHorseSaddled()&&h.riddenByEntity!=null;body.rotationPointY=11;}
 private static float rot(float previous,float current,float partial){return previous+MathHelper.wrapAngleTo180_float(current-previous)*partial;}
 @Override public void setLivingAnimations(EntityLivingBase living,float limb,float amount,float partial){
  EntityHorse h=(EntityHorse)living;float bodyYaw=rot(h.prevRenderYawOffset,h.renderYawOffset,partial),headYaw=rot(h.prevRotationYawHead,h.rotationYawHead,partial);
  float pitch=(h.prevRotationPitch+(h.rotationPitch-h.prevRotationPitch)*partial)*(float)Math.PI/180;
  float yaw=MathHelper.clamp_float(headYaw-bodyYaw,-20,20)*(float)Math.PI/180;
  if(amount>.2F)pitch+=MathHelper.cos(limb*.8F)*.15F*amount;
  animate(h.getGrassEatingAmount(partial),h.getRearingAmount(partial),h.func_110201_q(partial),h.field_110278_bp!=0,h.ticksExisted+partial,limb,amount,pitch,yaw,h.isInWater(),h.isChild());
 }
 public void animate(float eat,float rear,float mouth,boolean tailSwing,float age,float limb,float amount,float pitch,float yaw,boolean water,boolean child){
  float normal=1-Math.max(rear,eat),unrear=1-rear;
  headParts.rotateAngleX=rear*(.2617994F+pitch)+eat*(2.1816616F+MathHelper.sin(age)*.05F)+normal*(.5235988F+pitch+mouth*MathHelper.sin(age)*.05F);
  headParts.rotateAngleY=(rear+normal)*yaw;headParts.rotationPointY=rear*-4+eat*11+normal*4;headParts.rotationPointZ=rear*-4+eat*-12+normal*-12;
  body.rotateAngleX=rear*-.7853982F;
  float gait=MathHelper.cos((water?.2F:1)*limb*.6662F+(float)Math.PI),swing=gait*.8F*amount,rearSwing=MathHelper.cos(age*.6F+(float)Math.PI);
  adultLegs[3].rotationPointY=adultLegs[2].rotationPointY=2*rear+14*unrear;
  adultLegs[3].rotationPointZ=adultLegs[2].rotationPointZ=-6*rear-10*unrear;
  adultLegs[1].rotateAngleX=.2617994F*rear-gait*.5F*amount*unrear;
  adultLegs[0].rotateAngleX=.2617994F*rear+gait*.5F*amount*unrear;
  adultLegs[3].rotateAngleX=(-1.0471976F+rearSwing)*rear+swing*unrear;
  adultLegs[2].rotateAngleX=(-1.0471976F-rearSwing)*rear-swing*unrear;
  tail.rotateAngleX=.5235988F+amount*.75F;tail.rotationPointY=-5+amount;tail.rotationPointZ=2+amount*2;tail.rotateAngleY=tailSwing?MathHelper.cos(age*.7F):0;
  for(int i=0;i<4;i++){ModelRenderer a=adultLegs[i],b=babyLegs[i];b.rotationPointY=a.rotationPointY;b.rotationPointZ=a.rotationPointZ;b.rotateAngleX=a.rotateAngleX;a.showModel=!child;b.showModel=child;}
  body.rotationPointY=child?10.8F:0;
 }
 @Override public void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale){
  setRotationAngles(limb,amount,age,yaw,pitch,scale,entity);
  if(isChild){GL11.glPushMatrix();float headScale=1.5F/2.7272F;GL11.glScalef(headScale,headScale,headScale);GL11.glTranslatef(0,16.2F*scale,1.36F*scale);headParts.render(scale);GL11.glPopMatrix();GL11.glPushMatrix();GL11.glScalef(.5F,.5F,.5F);GL11.glTranslatef(0,20*scale,0);renderBody(scale);GL11.glPopMatrix();}
  else{headParts.render(scale);renderBody(scale);}
 }
 private void renderBody(float scale){body.render(scale);for(ModelRenderer p:adultLegs)p.render(scale);for(ModelRenderer p:babyLegs)p.render(scale);}
}
