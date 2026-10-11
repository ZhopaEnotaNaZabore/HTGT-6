package com.mod.htgt6.minecraft1202.Module.Client.Exploration;
import net.minecraft.client.model.ModelQuadruped;import net.minecraft.client.model.ModelRenderer;
/** Exact adult 1.12 Llama geometry; ModelQuadruped supplies the vanilla gait and child transform. */
public final class ModelLlama112 extends ModelQuadruped{
 private ModelRenderer rightChest,leftChest;
 public ModelLlama112(){super(15,0);textureWidth=128;textureHeight=64;head=new ModelRenderer(this,0,0);head.addBox(-2,-14,-10,4,4,9);head.setRotationPoint(0,7,-6);head.setTextureOffset(0,14).addBox(-4,-16,-6,8,18,6);head.setTextureOffset(17,0).addBox(-4,-19,-4,3,3,2);head.setTextureOffset(17,0).addBox(1,-19,-4,3,3,2);body=new ModelRenderer(this,29,0);body.addBox(-6,-10,-7,12,18,10);body.setRotationPoint(0,5,2);leg1=leg(-3.5F,6);leg2=leg(3.5F,6);leg3=leg(-3.5F,-5);leg4=leg(3.5F,-5);field_78151_h+=2;}
 private ModelRenderer leg(float x,float z){ModelRenderer p=new ModelRenderer(this,29,29);p.addBox(-2,0,-2,4,14,4);p.setRotationPoint(x,10,z);return p;}
 @Override public void render(net.minecraft.entity.Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale){
  super.render(entity,limb,amount,age,yaw,pitch,scale);
  if(!isChild&&entity instanceof com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112
      &&((com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityLlama112)entity).hasChest()){
   if(rightChest==null){rightChest=chest(28,-8.5F);leftChest=chest(41,5.5F);}
   rightChest.render(scale);leftChest.render(scale);
  }
 }
 private ModelRenderer chest(int v,float x){ModelRenderer p=new ModelRenderer(this,45,v);p.addBox(-3,0,0,8,8,3);p.setRotationPoint(x,3,3);p.rotateAngleY=(float)Math.PI/2;return p;}
}
