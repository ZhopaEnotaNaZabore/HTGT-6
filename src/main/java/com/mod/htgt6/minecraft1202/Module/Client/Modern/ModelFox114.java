package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import net.minecraft.client.model.*;import net.minecraft.entity.Entity;import net.minecraft.util.MathHelper;import org.lwjgl.opengl.GL11;import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityFox114;
/** Official 48x32 fox part dimensions and pivots adapted to ModelRenderer. */
public final class ModelFox114 extends ModelBase {
 public final ModelRenderer head,body,tail;public final ModelRenderer[] legs=new ModelRenderer[4];
 public ModelFox114(){textureWidth=48;textureHeight=32;head=part(1,5,-3,-2,-5,8,6,6);head.addChild(part(8,1,-3,-4,-4,2,2,1));head.addChild(part(15,1,3,-4,-4,2,2,1));head.addChild(part(6,18,-1,2.01F,-8,4,2,3));body=part(24,15,-3,3.999F,-3.5F,6,11,6);tail=part(30,0,2,0,-1,4,9,5);tail.setRotationPoint(-4,15,-1);body.addChild(tail);for(int i=0;i<4;i++)legs[i]=part(i%2==0?13:4,24,2,.5F,-1,2,6,2);}
 private ModelRenderer part(int u,int v,float x,float y,float z,int w,int h,int d){ModelRenderer p=new ModelRenderer(this,u,v);p.addBox(x,y,z,w,h,d);return p;}
 public void setRotationAngles(float walk,float amount,float age,float yaw,float pitch,float scale,Entity entity){EntityFox114 fox=(EntityFox114)entity;head.setRotationPoint(-1,16.5F,-3);head.rotateAngleX=pitch*.017453292F;head.rotateAngleY=yaw*.017453292F;head.rotateAngleZ=fox.headRoll(MathHelper.clamp_float(age-fox.ticksExisted,0,1));body.setRotationPoint(0,16,-6);body.rotateAngleX=1.5707964F;body.rotateAngleZ=0;body.rotateAngleY=0;tail.setRotationPoint(-4,15,-1);tail.rotateAngleX=-.05235988F;
  for(int i=0;i<4;i++){legs[i].showModel=true;legs[i].rotateAngleY=legs[i].rotateAngleZ=0;legs[i].setRotationPoint(i%2==0?-5:-1,17.5F,i<2?7:0);legs[i].rotateAngleX=MathHelper.cos(walk*.6662F+(i==1||i==2?(float)Math.PI:0))*1.4F*amount;}
  if(fox.crouching()){
   float crouch=fox.crouchAmount(MathHelper.clamp_float(age-fox.ticksExisted,0,1));
   body.rotateAngleX=1.6755161F;body.rotationPointY+=crouch;head.rotationPointY+=crouch;head.rotateAngleY=0;
   body.rotateAngleY=MathHelper.cos(age)*.01F;for(int i=0;i<4;i++)legs[i].rotateAngleZ=body.rotateAngleY*(i<2?1:.5F);
  }else if(fox.sitting()){
   body.rotateAngleX=.5235988F;body.setRotationPoint(0,9,-3);tail.rotateAngleX=.7853982F;tail.rotationPointZ=-2;
   head.setRotationPoint(-1,isChild?13:10,isChild?-3.75F:-.25F);head.rotateAngleX=head.rotateAngleY=0;
   for(int i=0;i<2;i++){legs[i].rotateAngleX=-1.3089969F;legs[i].setRotationPoint(i==0?-5:-1,21.5F,6.75F);}
   legs[2].rotateAngleX=legs[3].rotateAngleX=-.2617994F;
  }
  if(fox.faceplanted()){head.rotateAngleX=head.rotateAngleY=0;for(int i=0;i<4;i++)legs[i].rotateAngleX=MathHelper.cos(age*.67F*.4662F+(i==1||i==2?(float)Math.PI:0))*.1F;}
  if(fox.sleeping()){body.rotateAngleZ=-1.5707964F;body.setRotationPoint(0,21,isChild?-2:-6);tail.rotateAngleX=isChild?-2.1816616F:-2.6179938F;head.setRotationPoint(1,19.49F,-3);head.rotateAngleX=0;head.rotateAngleY=-2.0943952F;head.rotateAngleZ=MathHelper.cos(age*.027F)/22;for(ModelRenderer leg:legs)leg.showModel=false;}
 }
 public void render(Entity e,float walk,float amount,float age,float yaw,float pitch,float scale){setRotationAngles(walk,amount,age,yaw,pitch,scale,e);GL11.glPushMatrix();if(isChild){GL11.glScalef(.75F,.75F,.75F);GL11.glTranslatef(0,8*scale,3.35F*scale);}head.render(scale);GL11.glPopMatrix();GL11.glPushMatrix();if(isChild){GL11.glScalef(.5F,.5F,.5F);GL11.glTranslatef(0,24*scale,0);}body.render(scale);for(ModelRenderer leg:legs)leg.render(scale);GL11.glPopMatrix();}
}
