package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import net.minecraft.client.model.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
public final class ModelStrider116 extends ModelBase {
    public final ModelRenderer rightLeg,leftLeg,body;
    public final ModelRenderer[] bristles=new ModelRenderer[6];
    private static final float[] REST={-1.2217305F,-1.134464F,-.87266463F,.87266463F,1.134464F,1.2217305F};
    private static final float[] AMPLITUDE={1.3F,1.2F,.6F,.6F,1.2F,1.3F},IDLE={.05F,.1F,.1F,.1F,.1F,.05F},FREQUENCY={-.4F,.2F,.4F,.4F,.2F,-.4F};
    public ModelStrider116(){
        textureWidth=64;textureHeight=128;
        rightLeg=part(0,32,-2,0,-2,4,16,4,-4,8,0);leftLeg=part(0,55,-2,0,-2,4,16,4,4,8,0);body=part(0,0,-8,-6,-8,16,14,16,0,1,0);
        int[] v={65,49,33,33,49,65};float[] y={4,-1,-5,-6,-2,3};
        for(int i=0;i<6;i++){ModelRenderer p=new ModelRenderer(this,16,v[i]);p.mirror=i<3;p.addBox(i<3?-12:0,0,0,12,0,16);p.setRotationPoint(i<3?-8:8,y[i],-8);body.addChild(p);bristles[i]=p;}
    }
    private ModelRenderer part(int u,int v,float x,float y,float z,int a,int b,int c,float px,float py,float pz){ModelRenderer p=new ModelRenderer(this,u,v);p.addBox(x,y,z,a,b,c);p.setRotationPoint(px,py,pz);return p;}
    @Override public void setRotationAngles(float walk,float amount,float age,float yaw,float pitch,float scale,Entity e){
        amount=Math.min(.25F,amount);body.rotateAngleX=e.riddenByEntity==null?pitch*(float)Math.PI/180:0;body.rotateAngleY=e.riddenByEntity==null?yaw*(float)Math.PI/180:0;
        body.rotateAngleZ=.4F*MathHelper.sin(walk*1.5F)*amount;body.rotationPointY=2-4*MathHelper.cos(walk*1.5F)*amount;
        leftLeg.rotateAngleX=MathHelper.sin(walk*.75F)*2*amount;rightLeg.rotateAngleX=MathHelper.sin(walk*.75F+(float)Math.PI)*2*amount;
        leftLeg.rotateAngleZ=.17453292F*MathHelper.cos(walk*.75F)*amount;rightLeg.rotateAngleZ=.17453292F*MathHelper.cos(walk*.75F+(float)Math.PI)*amount;
        leftLeg.rotationPointY=8+4*MathHelper.sin(walk*.75F+(float)Math.PI)*amount;rightLeg.rotationPointY=8+4*MathHelper.sin(walk*.75F)*amount;
        float sway=MathHelper.cos(walk*1.5F+(float)Math.PI)*amount;
        for(int i=0;i<6;i++)bristles[i].rotateAngleZ=REST[i]+sway*AMPLITUDE[i]+IDLE[i]*MathHelper.sin(age*FREQUENCY[i]);
    }
    @Override public void render(Entity e,float walk,float amount,float age,float yaw,float pitch,float scale){setRotationAngles(walk,amount,age,yaw,pitch,scale,e);GL11.glPushMatrix();try{if(isChild){GL11.glScalef(.5F,.5F,.5F);GL11.glTranslatef(0,24*scale,0);}body.render(scale);rightLeg.render(scale);leftLeg.render(scale);}finally{GL11.glPopMatrix();}}
}
