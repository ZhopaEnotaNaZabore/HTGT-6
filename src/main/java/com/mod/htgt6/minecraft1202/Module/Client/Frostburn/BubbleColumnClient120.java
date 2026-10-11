package com.mod.htgt6.minecraft1202.Module.Client.Frostburn;

import com.mod.htgt6.minecraft1202.Module.World.Frostburn.FuturaBubbleColumn120;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.block.material.Material;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Local ambient particles from the same column query used by server physics. */
public final class BubbleColumnClient120 {
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc=Minecraft.getMinecraft();
        if(event.phase!=TickEvent.Phase.END||mc.theWorld==null||mc.thePlayer==null||mc.isGamePaused())return;
        World world=mc.theWorld;
        int px=MathHelper.floor_double(mc.thePlayer.posX),py=MathHelper.floor_double(mc.thePlayer.posY),pz=MathHelper.floor_double(mc.thePlayer.posZ);
        int samples=mc.gameSettings.particleSetting==2?0:mc.gameSettings.particleSetting==1?32:64;
        for(int i=0;i<samples;i++) {
            int x=px+world.rand.nextInt(17)-8,y=py+world.rand.nextInt(17)-8,z=pz+world.rand.nextInt(17)-8;
            int direction=FuturaBubbleColumn120.directionAt(world,x,y,z);
            if(direction!=0)mc.effectRenderer.addEffect(new Bubble(world,x+world.rand.nextDouble(),y+world.rand.nextDouble(),z+world.rand.nextDouble(),direction<0));
        }
    }
    private static final class Bubble extends EntityFX {
        private final boolean down;
        private float angle;
        Bubble(World world,double x,double y,double z,boolean down) {
            super(world,x,y,z,0,0,0);this.down=down;
            setParticleTextureIndex(32);setSize(.02F,.02F);
            particleScale*=rand.nextFloat()*.6F+.2F;
            if(down) {
                particleMaxAge=rand.nextInt(60)+30;noClip=true;
                motionX=motionZ=0;motionY=-.05D;
            } else {
                particleMaxAge=(int)(40D/(rand.nextDouble()*.8D+.2D));
                motionX=(rand.nextDouble()*2D-1D)*.02D;
                motionY=(rand.nextDouble()*2D-1D)*.02D;
                motionZ=(rand.nextDouble()*2D-1D)*.02D;
            }
        }
        @Override public void onUpdate() {
            prevPosX=posX;prevPosY=posY;prevPosZ=posZ;
            if(particleAge++>=particleMaxAge){setDead();return;}
            if(down) {
                motionX=(motionX+.6F*MathHelper.cos(angle))*.07D;
                motionZ=(motionZ+.6F*MathHelper.sin(angle))*.07D;
            } else motionY+=.005D;
            moveEntity(motionX,motionY,motionZ);
            if(!down) {
                motionX*=.85F;motionY*=.85F;motionZ*=.85F;
                if(onGround){motionX*=.7D;motionZ*=.7D;}
            } else angle+=.08F;
            int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(posY),z=MathHelper.floor_double(posZ);
            if(!worldObj.blockExists(x,y,z)||worldObj.getBlock(x,y,z).getMaterial()!=Material.water||(down&&onGround))setDead();
        }
    }
}
