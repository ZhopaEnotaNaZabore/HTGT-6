package com.mod.htgt6.minecraft1202.Module.Client.Modern;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/** Exact 1.20 SquidModel dimensions, including its slight body-cube inflation. */
public final class ModelGlowSquid120 extends ModelBase {
    public final ModelRenderer body;
    public final ModelRenderer[] tentacles=new ModelRenderer[8];
    public ModelGlowSquid120(){textureWidth=64;textureHeight=32;
        body=new ModelRenderer(this,0,0);body.addBox(-6,-8,-6,12,16,12,.02F);body.setRotationPoint(0,8,0);
        for(int i=0;i<tentacles.length;i++){double angle=(double)i*Math.PI*2D/tentacles.length;
            ModelRenderer part=new ModelRenderer(this,48,0);part.addBox(-1,0,-1,2,18,2);
            part.setRotationPoint((float)Math.cos(angle)*5F,15F,(float)Math.sin(angle)*5F);
            part.rotateAngleY=(float)((double)i*Math.PI*-2D/tentacles.length+Math.PI/2D);tentacles[i]=part;}}
    @Override public void setRotationAngles(float limb,float amount,float age,float yaw,float pitch,float scale,Entity entity){for(ModelRenderer tentacle:tentacles)tentacle.rotateAngleX=age;}
    @Override public void render(Entity entity,float limb,float amount,float age,float yaw,float pitch,float scale){setRotationAngles(limb,amount,age,yaw,pitch,scale,entity);body.render(scale);for(ModelRenderer tentacle:tentacles)tentacle.render(scale);}
}
