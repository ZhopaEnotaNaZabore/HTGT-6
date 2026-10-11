package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import cpw.mods.fml.relauncher.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;
@SideOnly(Side.CLIENT)
public final class ReversePortal116 extends EntityFX {
    private final float scale;
    public static void spawn(World w,double x,double y,double z,double vy){if(Minecraft.getMinecraft().effectRenderer!=null)Minecraft.getMinecraft().effectRenderer.addEffect(new ReversePortal116(w,x,y,z,vy));}
    private ReversePortal116(World w,double x,double y,double z,double vy){super(w,x,y,z);motionX=motionZ=0;motionY=vy;float color=rand.nextFloat()*.6F+.4F;particleRed=color*.9F;particleGreen=color*.3F;particleBlue=color;scale=particleScale=(rand.nextFloat()*.2F+.5F)*1.5F;particleMaxAge=60+rand.nextInt(2);noClip=true;setParticleTextureIndex(rand.nextInt(8));}
    @Override public void onUpdate(){prevPosX=posX;prevPosY=posY;prevPosZ=posZ;if(particleAge++>=particleMaxAge){setDead();return;}double step=(double)particleAge/particleMaxAge;setPosition(posX+motionX*step,posY+motionY*step,posZ+motionZ*step);}
    @Override public void renderParticle(Tessellator t,float partial,float rx,float rz,float ryz,float rxy,float rxz){particleScale=scale*(1-(particleAge+partial)/(particleMaxAge*1.5F));super.renderParticle(t,partial,rx,rz,ryz,rxy,rxz);}
    @Override public int getBrightnessForRender(float partial){int light=super.getBrightnessForRender(partial);float age=(float)particleAge/particleMaxAge;age*=age;age*=age;return (light&255)|(Math.min(240,(light>>16&255)+(int)(age*240))<<16);}
}
