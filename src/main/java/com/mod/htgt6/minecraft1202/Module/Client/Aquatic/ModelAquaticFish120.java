package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/** 1.13 fish geometry rebuilt as a connected legacy ModelRenderer hierarchy. */
public final class ModelAquaticFish120 extends ModelBase {
    private final ModelRenderer root;
    private final ModelRenderer tail;
    public ModelAquaticFish120(int kind) {
        textureWidth=32;textureHeight=32;root=new ModelRenderer(this,0,0);root.setRotationPoint(0,22,0);
        if(kind==0)tail=cod(); else if(kind==1)tail=salmon(); else if(kind==2)tail=puffer(); else tail=tropical();
    }
    private ModelRenderer part(int u,int v,float x,float y,float z,int w,int h,int d){ModelRenderer p=new ModelRenderer(this,u,v);p.addBox(x,y,z,w,h,d);root.addChild(p);return p;}
    private ModelRenderer cod(){
        root.addBox(-1,-2,0,2,4,7);part(11,0,-1,-2,-3,2,4,3);part(0,0,-1,-2,-4,2,3,1);part(20,0,0,-3,1,0,1,6);
        ModelRenderer left=part(22,1,0,0,-1,2,0,2);left.setRotationPoint(1,1,1);left.rotateAngleZ=.7853982F;
        ModelRenderer right=part(22,1,-2,0,-1,2,0,2);right.setRotationPoint(-1,1,1);right.rotateAngleZ=-.7853982F;
        return part(22,3,0,-2,7,0,4,4);
    }
    private ModelRenderer salmon(){
        root.addBox(-1.5F,-2.5F,0,3,5,8);ModelRenderer back=part(0,13,-1.5F,-2.5F,0,3,5,8);back.setRotationPoint(0,0,8);
        part(22,0,-1,-2,-3,2,4,3);part(2,1,0,-3,2,0,3,6);
        ModelRenderer left=part(2,0,0,0,-1,2,0,2);left.setRotationPoint(1.5F,1,3);left.rotateAngleZ=.7853982F;
        ModelRenderer right=part(2,0,-2,0,-1,2,0,2);right.setRotationPoint(-1.5F,1,3);right.rotateAngleZ=-.7853982F;
        ModelRenderer result=new ModelRenderer(this,20,10);result.addBox(0,-2.5F,0,0,5,6);result.setRotationPoint(0,0,8);back.addChild(result);return result;
    }
    private ModelRenderer puffer(){
        root.addBox(-4,-4,-4,8,8,8);part(24,0,-4.5F,-2.5F,-5,2,2,1);part(24,0,2.5F,-2.5F,-5,2,2,1);
        ModelRenderer left=part(0,16,0,0,-2,3,0,4);left.setRotationPoint(4,1,0);left.rotateAngleZ=.35F;
        ModelRenderer right=part(0,16,-3,0,-2,3,0,4);right.setRotationPoint(-4,1,0);right.rotateAngleZ=-.35F;
        for(int i=-3;i<=3;i+=2){part(8,16,i,-6,-2,0,2,2);part(8,16,i,4,-2,0,2,2);}return part(16,16,0,-3,4,0,6,5);
    }
    private ModelRenderer tropical(){
        root.addBox(-1.5F,-2,-3,3,4,6);part(12,0,-1,-1.5F,-4,2,3,1);part(0,10,0,-4,-2,0,2,5);part(0,16,0,2,-2,0,2,5);
        ModelRenderer left=part(14,10,0,0,-1,2,0,2);left.setRotationPoint(1.5F,1,0);left.rotateAngleZ=.6F;
        ModelRenderer right=part(14,10,-2,0,-1,2,0,2);right.setRotationPoint(-1.5F,1,0);right.rotateAngleZ=-.6F;return part(20,10,0,-2,3,0,4,4);
    }
    @Override public void setRotationAngles(float limb,float amount,float age,float yaw,float pitch,float scale,Entity entity){root.rotateAngleX=pitch*.017453292F;float speed=entity.isInWater()?.6F:1.7F;tail.rotateAngleY=(float)Math.sin(age*speed)*.45F;}
    @Override public void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale){setRotationAngles(limb,amount,age,yaw,pitch,scale,entity);root.render(scale);}
}
