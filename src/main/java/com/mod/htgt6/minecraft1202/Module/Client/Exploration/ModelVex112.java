package com.mod.htgt6.minecraft1202.Module.Client.Exploration;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVex112;import net.minecraft.client.model.ModelBiped;import net.minecraft.client.model.ModelRenderer;import net.minecraft.entity.Entity;import net.minecraft.util.MathHelper;
/** Exact 1.12 Vex body and 20x12 wing animation. */
public final class ModelVex112 extends ModelBiped{
 private final ModelRenderer leftWing,rightWing;
 public ModelVex112(){super(0,0,64,64);bipedLeftLeg.showModel=false;bipedHeadwear.showModel=false;bipedRightLeg=new ModelRenderer(this,32,0);bipedRightLeg.addBox(-1,-1,-2,6,10,4);bipedRightLeg.setRotationPoint(-1.9F,12,0);rightWing=new ModelRenderer(this,0,32);rightWing.addBox(-20,0,0,20,12,1);leftWing=new ModelRenderer(this,0,32);leftWing.mirror=true;leftWing.addBox(0,0,0,20,12,1);}
 @Override public void setRotationAngles(float l,float a,float age,float yaw,float pitch,float s,Entity e){super.setRotationAngles(l,a,age,yaw,pitch,s,e);if(((EntityVex112)e).isCharging())bipedRightArm.rotateAngleX=3.7699115F;bipedRightLeg.rotateAngleX+=.62831855F;rightWing.rotationPointZ=leftWing.rotationPointZ=2;rightWing.rotationPointY=leftWing.rotationPointY=1;rightWing.rotateAngleY=.47123894F+MathHelper.cos(age*.8F)*(float)Math.PI*.05F;leftWing.rotateAngleY=-rightWing.rotateAngleY;leftWing.rotateAngleZ=-.47123894F;leftWing.rotateAngleX=.47123894F;rightWing.rotateAngleX=.47123894F;rightWing.rotateAngleZ=.47123894F;}
 @Override public void render(Entity e,float l,float a,float age,float yaw,float pitch,float s){super.render(e,l,a,age,yaw,pitch,s);rightWing.render(s);leftWing.render(s);}
}
