package com.mod.htgt6.minecraft1202.Module.Client.Modern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.*;
@SideOnly(Side.CLIENT)
public final class CryingObsidianParticle116 extends EntityFX {
    private int hanging=40;
    private CryingObsidianParticle116(World w,double x,double y,double z){super(w,x,y,z);motionX=motionY=motionZ=0;particleRed=.5117F;particleGreen=.03125F;particleBlue=.890625F;particleScale=.5F;particleMaxAge=100;setSize(.01F,.01F);setParticleTextureIndex(113);}
    public static void spawn(World w,double x,double y,double z){if(Minecraft.getMinecraft().effectRenderer!=null)Minecraft.getMinecraft().effectRenderer.addEffect(new CryingObsidianParticle116(w,x,y,z));}
    @Override public int getBrightnessForRender(float partial){return 240;}
    @Override public void onUpdate(){prevPosX=posX;prevPosY=posY;prevPosZ=posZ;if(particleAge++>=particleMaxAge){setDead();return;}if(hanging-->0)return;motionY-=.01;moveEntity(0,motionY,0);if(onGround)setDead();}
}
