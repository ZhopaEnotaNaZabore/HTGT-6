package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.SwimmingPose120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ElytraFlight;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public final class SwimmingPlayerRenderer120 {
    public static void rotate(EntityPlayer p,float partial){
        if(ElytraFlight.isFlying(p)||p.isPlayerSleeping())return;
        float a=SwimmingPose120.amount(p,partial);if(a<=0)return;
        float pitch=p.prevRotationPitch+(p.rotationPitch-p.prevRotationPitch)*partial;
        GL11.glRotatef(a*(-90F-(p.isInWater()?pitch:0F)),1,0,0);
        GL11.glTranslatef(0,-a,.3F*a);
    }
    public static void model(ModelBiped m,Entity entity,float limbSwing,float age){
        if(!(entity instanceof EntityPlayer))return;EntityPlayer p=(EntityPlayer)entity;
        if(ElytraFlight.isFlying(p)||p.isPlayerSleeping())return;
        if(SwimmingPose120.kind(p)==1&&!m.isSneak){m.bipedBody.rotateAngleX=.5F;m.bipedRightArm.rotateAngleX+=.4F;m.bipedLeftArm.rotateAngleX+=.4F;m.bipedRightLeg.rotationPointY=m.bipedLeftLeg.rotationPointY=9F;m.bipedRightLeg.rotationPointZ=m.bipedLeftLeg.rotationPointZ=4F;m.bipedHead.rotationPointY=m.bipedHeadwear.rotationPointY=1F;}
        float a=SwimmingPose120.amount(p,MathHelper.clamp_float(age-p.ticksExisted,0,1));if(a<=0)return;
        m.bipedBody.rotateAngleX*=1-a;m.bipedHead.rotationPointY*=1-a;m.bipedHeadwear.rotationPointY=m.bipedHead.rotationPointY;
        m.bipedLeftLeg.rotationPointY=lerp(a,m.bipedLeftLeg.rotationPointY,12F);m.bipedRightLeg.rotationPointY=lerp(a,m.bipedRightLeg.rotationPointY,12F);
        m.bipedLeftLeg.rotationPointZ=lerp(a,m.bipedLeftLeg.rotationPointZ,.1F);m.bipedRightLeg.rotationPointZ=lerp(a,m.bipedRightLeg.rotationPointZ,.1F);
        float t=limbSwing%26F,x,leftZ,rightZ;
        if(t<14){x=0;float q=(t*t-65*t)/(14F*14F-65F*14F);leftZ=(float)Math.PI+1.8707964F*q;rightZ=(float)Math.PI-1.8707964F*q;}
        else if(t<22){float q=(t-14)/8F;x=(float)Math.PI/2*q;leftZ=5.012389F-1.8707964F*q;rightZ=1.2707963F+1.8707964F*q;}
        else{float q=(t-22)/4F;x=(float)Math.PI/2*(1-q);leftZ=rightZ=(float)Math.PI;}
        if(!p.isUsingItem()){
            arm(m.bipedLeftArm,a,x,leftZ);
            if(m.onGround<=0)arm(m.bipedRightArm,a,x,rightZ);
        }
        m.bipedLeftLeg.rotateAngleX=lerp(a,m.bipedLeftLeg.rotateAngleX,.3F*MathHelper.cos(limbSwing*.33333334F+(float)Math.PI));
        m.bipedRightLeg.rotateAngleX=lerp(a,m.bipedRightLeg.rotateAngleX,.3F*MathHelper.cos(limbSwing*.33333334F));
        if(p.isInWater())m.bipedHead.rotateAngleX=angle(a,m.bipedHead.rotateAngleX,-(float)Math.PI/4);
        m.bipedHeadwear.rotateAngleX=m.bipedHead.rotateAngleX;
    }
    private static void arm(ModelRenderer arm,float a,float x,float z){arm.rotateAngleX=angle(a,arm.rotateAngleX,x);arm.rotateAngleY=angle(a,arm.rotateAngleY,(float)Math.PI);arm.rotateAngleZ=angle(a,arm.rotateAngleZ,z);}
    private static float lerp(float a,float from,float to){return from+(to-from)*a;}
    private static float angle(float a,float from,float to){float d=(to-from)%(2*(float)Math.PI);if(d<-(float)Math.PI)d+=2*(float)Math.PI;if(d>=(float)Math.PI)d-=2*(float)Math.PI;return from+a*d;}
}
